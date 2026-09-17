package com.keshe.edumanage.vo;

import lombok.Data;

/**
 * 当前登录用户返回对象
 */
@Data
public class UserVO {

    /**
     * sys_user.id
     */
    private Long id;

    /**
     * CAS / 系统用户名
     *
     * 这里只代表登录账号，
     * 不再代表学号或工号。
     */
    private String username;

    /**
     * 用户角色
     *
     * ADMIN
     * STUDENT
     * TEACHER
     */
    private String role;

    /**
     * 业务档案ID
     *
     * STUDENT：
     * base_student.id
     *
     * TEACHER：
     * base_teacher.id
     *
     * ADMIN：
     * null
     *
     * 使用String返回，
     * 防止Snowflake ID在JavaScript中精度丢失。
     */
    private String businessId;
}
