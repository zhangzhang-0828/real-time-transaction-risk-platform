package com.yupi.yupao.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yupi.yupao.config.GenderDeserializer;

/**
 * 用户注册请求体
 *
  
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 性别 0-女 1-男
     */
    @JsonDeserialize(using = GenderDeserializer.class)
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 标签列表
     */
    private List<String> tags;
}
