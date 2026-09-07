package com.keshe.edumanage.service;


import com.keshe.edumanage.entity.User;


/**
 * 用户业务接口
 */
public interface UserService {


    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);


}