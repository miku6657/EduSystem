package com.keshe.edumanage.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * 系统用户
 */
@Data
@TableName("sys_user")
public class User extends BaseEntity {


    /**
     * 用户ID
     */
    @TableId
    private Long id;


    /**
     * 用户名
     */
    private String username;


    /**
     * 密码
     */
    private String password;


    /**
     * 角色
     *
     * ADMIN
     * TEACHER
     * STUDENT
     */
    private String role;


    /**
     * 状态
     *
     * 1 正常
     * 0 禁用
     */
    private Integer status;

}