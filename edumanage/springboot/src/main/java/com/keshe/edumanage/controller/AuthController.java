package com.keshe.edumanage.controller;


import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.LoginDTO;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.UserService;
import com.keshe.edumanage.util.JWTUtil;
import com.keshe.edumanage.vo.LoginVO;
import com.keshe.edumanage.vo.UserVO;


import lombok.RequiredArgsConstructor;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JWTUtil jwtUtil;
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
        String token =
                jwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole()
                );
        UserVO userVO = new UserVO();
        userVO.setId(
                user.getId()
        );
        userVO.setUsername(
                user.getUsername()
        );
        userVO.setRole(
                user.getRole()
        );
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);
        return Result.success(loginVO);
    }
}
