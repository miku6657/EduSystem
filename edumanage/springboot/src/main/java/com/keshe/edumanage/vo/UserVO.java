package com.keshe.edumanage.vo;


import lombok.Data;


/**
 * 用户返回对象
 */
@Data
public class UserVO {


    /**
     * 用户ID
     */
    private Long id;


    /**
     * 用户名
     */
    private String username;


    /**
     * 角色
     */
    private String role;


}