package com.keshe.edumanage.vo;


import lombok.Data;


/**
 * 登录返回对象
 */
@Data
public class LoginVO {


    /**
     * JWT Token
     */
    private String token;


    /**
     * 用户信息
     */
    private UserVO user;


}