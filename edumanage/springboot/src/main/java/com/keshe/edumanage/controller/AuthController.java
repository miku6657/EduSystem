package com.keshe.edumanage.controller;

import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.dto.LoginDTO;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.LoginService;
import com.keshe.edumanage.service.UserService;
import com.keshe.edumanage.vo.LoginVO;
import com.keshe.edumanage.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final LoginService loginService;

    private final PasswordEncoder passwordEncoder;

    /**
     * 原账号密码登录。
     *
     * 目前管理端如果还需要，
     * 可以继续保留。
     *
     * 师生端以后统一使用CAS，
     * 不再使用自己的用户名密码登录。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(
            @RequestBody LoginDTO loginDTO
    ) {

        User user =
                userService.findByUsername(
                        loginDTO.getUsername()
                );

        if (user == null) {
            return Result.fail(
                    "用户不存在"
            );
        }

        if (!passwordEncoder.matches(
                loginDTO.getPassword(),
                user.getPassword()
        )) {
            return Result.fail(
                    "密码错误"
            );
        }

        LoginVO loginVO =
                loginService.createLoginVO(
                        user
                );

        return Result.success(
                loginVO
        );
    }

    /**
     * 获取当前登录用户。
     *
     * JWT中保存username，
     * JwtAuthenticationFilter解析JWT后，
     * username会进入SecurityContext。
     *
     * 前端以后直接使用：
     *
     * role
     * +
     * businessId
     *
     * 确认当前登录用户对应的
     * 学生/教师业务档案。
     */
    @GetMapping("/userinfo")
    public Result<UserVO> userInfo(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            return Result.error(
                    401,
                    "未登录"
            );
        }

        String username =
                authentication.getName();

        User user =
                userService.findByUsername(
                        username
                );

        if (user == null) {
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

        /**
         * Snowflake ID不能直接作为
         * JavaScript number使用。
         *
         * 所以这里转成String。
         */
        if (
                user.getBusinessId()
                        != null
        ) {
            userVO.setBusinessId(
                    String.valueOf(
                            user.getBusinessId()
                    )
            );
        }

        return Result.success(
                userVO
        );
    }
}
