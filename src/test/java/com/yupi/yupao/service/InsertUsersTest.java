//package com.yupi.yupao.service;
//
//import com.yupi.yupao.model.domain.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.util.StopWatch;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//
///**
// * 导入用户测试
// *
// * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
// * @from <a href="https://yupi.icu">编程导航知识星球</a>
// */
//@SpringBootTest
//public class InsertUsersTest {
//
//    @Resource
//    private UserService userService;
//
//    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
//
//    /**
//     * 批量插入用户
//     */
//    @Test
//    public void doInsertUsers() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        final int INSERT_NUM = 100000;
//        List<User> userList = new ArrayList<>();
//        for (int i = 0; i < INSERT_NUM; i++) {
//            User user = new User();
//            user.setUsername("原_创 【鱼_皮】https://t.zsxq.com/0emozsIJh");
//            user.setUserAccount("fakeyupi");
//            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
//            user.setGender(0);
//            user.setUserPassword("12345678");
//            user.setPhone("123");
//            user.setEmail("123@qq.com");
//            user.setTags("[]");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("11111111");
//            userList.add(user);
//        }
//        // 20 秒 10 万条
//        userService.saveBatch(userList, 10000);
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//
//    /**
//     * 并发批量插入用户
//     */
//    @Test
//    public void doConcurrencyInsertUsers() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        // 分十组
//        int batchSize = 5000;
//        int j = 0;
//        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            List<User> userList = new ArrayList<>();
//            while (true) {
//                j++;
//                User user = new User();
//                user.setUsername("假鱼皮");
//                user.setUserAccount("fakeyupi");
//                user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
//                user.setGender(0);
//                user.setUserPassword("12345678");
//                user.setPhone("123");
//                user.setEmail("123@qq.com");
//                user.setTags("[]");
//                user.setUserStatus(0);
//                user.setUserRole(0);
//                user.setPlanetCode("11111111");
//                userList.add(user);
//                if (j % batchSize == 0) {
//                    break;
//                }
//            }
//            // 异步执行
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                System.out.println("threadName: " + Thread.currentThread().getName());
//                userService.saveBatch(userList, batchSize);
//            }, executorService);
//            futureList.add(future);
//        }
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
//        // 20 秒 10 万条
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//}
//
//

package com.yupi.yupao.service;

import com.yupi.yupao.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * 导入用户测试
 *
 */
@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    private static final ThreadLocal<Random> RANDOM_THREAD_LOCAL = ThreadLocal.withInitial(Random::new);

    private static final String SALT = "yupi";
    private static final String PLAIN_PASSWORD = "12345678";
    private static final String ENCRYPT_PASSWORD = DigestUtils.md5DigestAsHex((SALT + PLAIN_PASSWORD).getBytes());

    private final String[] techTags = {"Java", "C++", "Go", "前端", "Python", "算法"};
    private final String[] goalTags = {"春招", "秋招", "考研", "考公", "蓝桥杯", "转行", "跳槽"};
    private final String[] roleTags = {"大一", "大二", "大三", "大四", "研一", "研二", "研三", "已就业", "待业"};
    private final String[] levelTags = {"初级", "中级", "高级", "王者"};
    private final String[] genderTags = {"男", "女"};

    /**
     * 生成随机组合标签
     */
    private String getRandomTags() {
        Random random = RANDOM_THREAD_LOCAL.get();
        String tech = techTags[random.nextInt(techTags.length)];
        String goal = goalTags[random.nextInt(goalTags.length)];
        String role = roleTags[random.nextInt(roleTags.length)];
        String level = levelTags[random.nextInt(levelTags.length)];
        String gender = genderTags[random.nextInt(genderTags.length)];
        return "[\"" + tech + "\",\"" + goal + "\",\"" + role + "\",\"" + level + "\",\"" + gender + "\"]";
    }

    /**
     * 生成唯一手机号
     */
    private String getUniquePhone(int index) {
        return "138" + String.format("%08d", index % 100000000);
    }

    /**
     * 创建用户对象
     */
    private User createUser(int index) {
        Random random = RANDOM_THREAD_LOCAL.get();
        User user = new User();
        user.setUsername("用户" + index);
        user.setUserAccount("test_" + index);
        user.setAvatarUrl("https://picsum.photos/200/200?random=" + index);
        user.setGender(random.nextInt(2));
        user.setUserPassword(ENCRYPT_PASSWORD);
        user.setPhone(getUniquePhone(index));
        user.setEmail("test" + index + "@qq.com");
        user.setTags(getRandomTags());
        user.setUserStatus(0);
        user.setIsDelete(0);
        user.setUserRole(0);
        user.setPlanetCode("999999");
        return user;
    }

    /**
     * 快速插入少量测试数据（几十条）
     */
    @Test
    public void doInsertSmallBatchUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUM = 50;
        List<User> userList = new ArrayList<>();

        for (int i = 0; i < INSERT_NUM; i++) {
            userList.add(createUser(i));
        }

        userService.saveBatch(userList, 10000);
        stopWatch.stop();

        System.out.println("✅ 成功插入 " + INSERT_NUM + " 条用户数据");
        System.out.println("⏱️ 插入耗时：" + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("📝 登录账号示例：test_0 ~ test_" + (INSERT_NUM - 1));
        System.out.println("🔑 统一密码：" + PLAIN_PASSWORD);
    }

    /**
     * 批量插入用户（单线程）
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        List<User> userList = new ArrayList<>();

        for (int i = 0; i < INSERT_NUM; i++) {
            userList.add(createUser(i));
        }

        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println("成功插入 " + INSERT_NUM + " 条用户数据");
        System.out.println("插入耗时：" + stopWatch.getTotalTimeMillis() + "ms");
    }

    /**
     * 并发批量插入用户（大数据量用）
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int totalRecords = 100000;
        int batchSize = 5000;
        int batchCount = totalRecords / batchSize;

        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        for (int i = 0; i < batchCount; i++) {
            final int batchIndex = i;
            List<User> userList = new ArrayList<>();

            for (int j = 0; j < batchSize; j++) {
                int globalIndex = batchIndex * batchSize + j;
                userList.add(createUser(globalIndex));
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("线程 " + Thread.currentThread().getName() +
                        " 正在插入批次 " + (batchIndex + 1) + "/" + batchCount);
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();
        System.out.println("成功插入 " + totalRecords + " 条用户数据");
        System.out.println("并发插入耗时：" + stopWatch.getTotalTimeMillis() + "ms");
    }
}
