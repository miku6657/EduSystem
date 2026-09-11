package com.keshe.edumanage.controller;


import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.LoginDTO;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.LoginService;
import com.keshe.edumanage.service.UserService;
import com.keshe.edumanage.vo.LoginVO;
import com.keshe.edumanage.vo.UserVO;
import org.springframework.security.core.Authentication;


import lombok.RequiredArgsConstructor;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final LoginService loginService;
    private final PasswordEncoder passwordEncoder;
    @PostMapping("/login")
    public Result<LoginVO> login(
            @RequestBody LoginDTO loginDTO
    ){
        User user =
                userService.findByUsername(
                        loginDTO.getUsername()
                );
        if(user == null){
            return Result.fail("用户不存在");

        }
        if(!passwordEncoder.matches(
                loginDTO.getPassword(),
                user.getPassword()
        )){
            return Result.fail("密码错误");
        }
        LoginVO loginVO =
                loginService.createLoginVO(user);
        return Result.success(loginVO);
    }
    /**
     * 获取当前登录用户
     */
    @GetMapping("/userinfo")
    public Result<UserVO> userInfo(
            Authentication authentication
    ){

        String username =
                authentication.getName();


        User user =
                userService.findByUsername(
                        username
                );


        if(user == null){
            return Result.fail(
                    "用户不存在"
            );
        }


        UserVO userVO =
                new UserVO();


        userVO.setId(
                user.getId()
        );


        userVO.setUsername(
                user.getUsername()
        );


        userVO.setRole(
                user.getRole()
        );


        return Result.success(
                userVO
        );
    }
}
