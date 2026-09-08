package com.keshe.edumanage.service;
import com.keshe.edumanage.entity.system.User;

public interface UserService {
    User findByUsername(String username);
}
