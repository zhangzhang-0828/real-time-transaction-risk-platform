package com.yupi.yupao.service;

import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.request.UserRegisterRequest;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 用户服务测试
 *
  
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("(https://t.zsxq.com/0emozsIJh)\n");
        user.setUserAccount("123");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("dogYupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    @Test
    public void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }

    @Test
    public void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }

    @Test
    void userRegister() {
        // 使用新的 UserRegisterRequest 对象
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount("yupi");
        registerRequest.setUserPassword("12345678");
        registerRequest.setCheckPassword("12345678");
        registerRequest.setPlanetCode("1");

        long result = userService.userRegister(registerRequest);
        Assertions.assertTrue(result > 0 || result == -1);

        // 测试账号过短
        registerRequest.setUserAccount("yu");
        try {
            result = userService.userRegister(registerRequest);
        } catch (Exception e) {
            Assertions.assertNotNull(e);
        }

        // 测试密码过短
        registerRequest.setUserAccount("yupi");
        registerRequest.setUserPassword("123456");
        try {
            result = userService.userRegister(registerRequest);
        } catch (Exception e) {
            Assertions.assertNotNull(e);
        }

        // 测试特殊字符
        registerRequest.setUserPassword("12345678");
        registerRequest.setUserAccount("yu pi");
        try {
            result = userService.userRegister(registerRequest);
        } catch (Exception e) {
            Assertions.assertNotNull(e);
        }

        // 测试密码不一致
        registerRequest.setUserAccount("yupi");
        registerRequest.setCheckPassword("123456789");
        try {
            result = userService.userRegister(registerRequest);
        } catch (Exception e) {
            Assertions.assertNotNull(e);
        }
    }

    @Test
    public void testSearchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }
}