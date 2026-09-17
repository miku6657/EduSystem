package com.keshe.edumanage.entity.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keshe.edumanage.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    /**
     * CAS / 系统登录用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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
     */
    private Long businessId;

    /**
     * 用户状态
     */
    private Integer status;
}
