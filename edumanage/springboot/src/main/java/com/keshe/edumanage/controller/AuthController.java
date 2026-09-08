package com.keshe.edumanage.controller;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.LoginDTO;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.UserService;
import com.keshe.edumanage.util.JWTUtil;
import com.keshe.edumanage.vo.LoginVO;
import com.keshe.edumanage.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JWTUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(
            @RequestBody LoginDTO loginDTO
    ){
        //1. 根据用户名查询用户
        User user =
                userService.findByUsername(
                        loginDTO.getUsername()
                );

        //2. 用户不存在
        if(user == null){
            return Result.fail("用户不存在");
        }

        //3. 校验密码
        if(!user.getPassword()
                .equals(loginDTO.getPassword())){
            return Result.fail("密码错误");
        }

        //4. 生成JWT

        String token =
                jwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole()
                );

        //5. 封装返回用户信息

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());

        userVO.setUsername(
                user.getUsername()
        );

        userVO.setRole(
                user.getRole()
        );

        //6. 封装返回结果

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);
        return Result.success(loginVO);
    }
}
