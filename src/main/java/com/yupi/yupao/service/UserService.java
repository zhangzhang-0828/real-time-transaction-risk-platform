package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import com.yupi.yupao.model.request.UserRegisterRequest;

/**
 * 用户服务
 *
  
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册（支持完整信息）
     *
     * @param registerRequest 注册请求体
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * 获取推荐用户（带缓存）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户分页数据
     */
    Page<User> getRecommendUsers(long userId, long pageNum, long pageSize);

    /**
     * 更新用户标签（带校验）
     * @param userId 用户id
     * @param tagNames 标签名称列表
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean updateUserTags(Long userId, List<String> tagNames, User loginUser);

    /**
     * 获取用户的标签列表
     * @param userId 用户id
     * @return 标签名称列表
     */
    List<String> getUserTags(Long userId);
}
