package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.constant.UserConstant;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.request.UserRegisterRequest;
import com.yupi.yupao.service.TagService;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTagService;
import com.yupi.yupao.mapper.UserMapper;
import com.yupi.yupao.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
  
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TagService tagService;

    @Resource
    private UserTagService userTagService;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    // 缓存空值的有效期（分钟）
    private static final long CACHE_NULL_TTL = 2L;

    // 缓存基础有效期（分钟）
    private static final long CACHE_TTL = 30L;

    // 缓存有效期随机范围（分钟）
    private static final long CACHE_TTL_RANDOM_RANGE = 10L;

    @Override
    public long userRegister(UserRegisterRequest registerRequest) {
        // 1. 基础参数校验
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        String planetCode = registerRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "基础参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }

        // 2. 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        // 3. 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        // 4. 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 5. 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }

        // 6. 标签校验（如果提供了标签）
        List<String> tags = registerRequest.getTags();
        if (tags != null && !tags.isEmpty()) {
            // 标签数量限制
            if (tags.size() > 10) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签数量不能超过10个");
            }
            // 标签合法性校验
            boolean valid = tagService.validateTags(tags);
            if (!valid) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "包含非法标签，请从系统提供的标签中选择");
            }
        }

        // 7. 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 8. 插入用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        user.setUsername(registerRequest.getUsername());
        user.setGender(registerRequest.getGender());
        user.setPhone(registerRequest.getPhone());
        user.setEmail(registerRequest.getEmail());

        // 将标签转为JSON存储（兼容旧逻辑）
        if (tags != null && !tags.isEmpty()) {
            Gson gson = new Gson();
            user.setTags(gson.toJson(tags));
        }

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }

        // 9. 保存用户标签关联
        if (tags != null && !tags.isEmpty()) {
            userTagService.updateUserTags(user.getId(), tags);
        }

        return user.getId();
    }

    // [加入编程导航](https://www.code-nav.cn/) 入门捷径+交流答疑+项目实战+求职指导，帮你自学编程不走弯路

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户（内存过滤）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }



    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    @Override
    public Page<User> getRecommendUsers(long userId, long pageNum, long pageSize) {
        String redisKey = String.format("yupao:user:recommend:%s", userId);
        return queryWithMutex(redisKey, pageNum, pageSize);
    }

    /**
     * 使用互斥锁解决缓存击穿问题
     *
     * @param key      缓存key
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户分页数据
     */
    private Page<User> queryWithMutex(String key, long pageNum, long pageSize) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 1. 从 Redis 查询缓存
        Page<User> userPage = null;
        try {
            userPage = (Page<User>) valueOperations.get(key);
        } catch (Exception e) {
            log.error("Redis query error, key: {}", key, e);
        }

        // 2. 判断缓存是否存在（包括空值占位符）
        if (userPage != null) {
            // 如果是空值占位符，直接返回空结果
            if (isNullValue(userPage)) {
                log.info("Cache hit null value, key: {}", key);
                return new Page<>();
            }
            log.info("Cache hit, key: {}", key);
            return userPage;
        }

        // 3. 缓存未命中，尝试获取互斥锁
        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);
        Page<User> resultPage = null;

        try {
            // 尝试获取锁，等待时间 100ms，租期 10s（看门狗自动续期）
            boolean isLock = lock.tryLock(100, 10000, TimeUnit.MILLISECONDS);

            if (!isLock) {
                // 获取锁失败，短暂等待后重试
                log.warn("Failed to acquire lock, retrying... key: {}", key);
                Thread.sleep(50);
                return queryWithMutex(key, pageNum, pageSize);
            }

            try {
                // 4. 获取锁成功，再次检查缓存（双重检查）
                userPage = (Page<User>) valueOperations.get(key);
                if (userPage != null) {
                    if (isNullValue(userPage)) {
                        return new Page<>();
                    }
                    return userPage;
                }

                // 5. 查询数据库
                log.info("Querying database, key: {}", key);
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                resultPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

                // 6. 写入缓存
                if (resultPage == null || resultPage.getRecords().isEmpty()) {
                    // 数据库不存在，写入空值（解决缓存穿透）
                    log.info("Database result is empty, caching null value, key: {}", key);
                    valueOperations.set(key, createNullValuePage(), CACHE_NULL_TTL, TimeUnit.MINUTES);
                } else {
                    // 数据库存在，写入缓存（随机过期时间，解决缓存雪崩）
                    long ttl = getRandomTtl();
                    log.info("Caching data, key: {}, ttl: {} minutes", key, ttl);
                    valueOperations.set(key, resultPage, ttl, TimeUnit.MINUTES);
                }

            } finally {
                // 7. 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        } catch (InterruptedException e) {
            log.error("Lock acquisition interrupted, key: {}", key, e);
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
        } catch (Exception e) {
            log.error("Cache operation error, key: {}", key, e);
            // 降级处理：Redis 异常时直接查询数据库
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            resultPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        }

        return resultPage != null ? resultPage : new Page<>();
    }

    /**
     * 生成随机过期时间（解决缓存雪崩）
     * 基础时间 + 随机时间，范围：30~40 分钟
     */
    private long getRandomTtl() {
        Random random = new Random();
        return CACHE_TTL + random.nextInt((int) CACHE_TTL_RANDOM_RANGE);
    }

    /**
     * 创建空值占位符 Page 对象（解决缓存穿透）
     */
    private Page<User> createNullValuePage() {
        Page<User> nullPage = new Page<>();
        nullPage.setRecords(null);
        return nullPage;
    }

    /**
     * 判断是否为缓存空值占位符
     */
    private boolean isNullValue(Page<User> page) {
        return page != null && page.getRecords() == null;
    }

    /**
     * 根据标签搜索用户（SQL 查询版）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public boolean updateUserTags(Long userId, List<String> tagNames, User loginUser) {
        // 1. 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID错误");
        }

        // 2. 权限校验（只能修改自己的标签，管理员除外）
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限修改该用户标签");
        }

        // 3. 标签数量校验
        if (tagNames != null && tagNames.size() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签数量不能超过10个");
        }

        // 4. 标签合法性校验
        if (tagNames != null && !tagNames.isEmpty()) {
            boolean valid = tagService.validateTags(tagNames);
            if (!valid) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "包含非法标签，请从系统提供的标签中选择");
            }
        }

        // 5. 更新用户标签关联
        boolean result = userTagService.updateUserTags(userId, tagNames);

        // 6. 同步更新 user 表的 tags 字段（JSON 格式，兼容旧逻辑）
        if (result) {
            User user = new User();
            user.setId(userId);
            Gson gson = new Gson();
            user.setTags(gson.toJson(tagNames != null ? tagNames : new ArrayList<>()));
            userMapper.updateById(user);
        }

        return result;
    }

    @Override
    public List<String> getUserTags(Long userId) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }
        return userTagService.getUserTagNames(userId);
    }

}




