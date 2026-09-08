package com.keshe.edumanage.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.mapper.system.UserMapper;
import com.keshe.edumanage.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Override
    public User findByUsername(String username) {


        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();

    }

}