package com.keshe.edumanage.service;

import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.util.JWTUtil;
import com.keshe.edumanage.vo.LoginVO;
import com.keshe.edumanage.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {


    private final JWTUtil jwtUtil;


    public LoginVO createLoginVO(User user){

        String token =
                jwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole()
                );


        UserVO userVO = new UserVO();

        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRole(user.getRole());


        LoginVO loginVO = new LoginVO();

        loginVO.setToken(token);
        loginVO.setUser(userVO);


        return loginVO;
    }
}