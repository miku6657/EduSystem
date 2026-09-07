package com.keshe.edumanage.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keshe.edumanage.entity.User;
import com.keshe.edumanage.mapper.UserMapper;
import com.keshe.edumanage.service.UserService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;



/**
 * 用户业务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;



    @Override
    public User getByUsername(String username) {


        QueryWrapper<User> wrapper =
                new QueryWrapper<>();


        wrapper.eq(
                "username",
                username
        );


        return userMapper.selectOne(wrapper);

    }

}