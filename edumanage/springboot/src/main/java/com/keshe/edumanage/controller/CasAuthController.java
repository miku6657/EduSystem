package com.keshe.edumanage.controller;

import com.keshe.edumanage.config.CasProperties;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.CasService;
import com.keshe.edumanage.service.UserService;
import com.keshe.edumanage.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/cas")
@RequiredArgsConstructor
public class CasAuthController {

    private final CasProperties casProperties;

    private final CasService casService;

    private final UserService userService;

    private final JWTUtil jwtUtil;

    /**
     * 发起CAS登录
     */
    @GetMapping("/login")
    public void login(
            HttpServletResponse response
    ) throws IOException {

        String redirectUrl =
                UriComponentsBuilder
                        .fromHttpUrl(
                                casProperties.getServerUrlPrefix()
                                        + "/login"
                        )
                        .queryParam(
                                "service",
                                casProperties.getClientServiceUrl()
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(
                redirectUrl
        );
    }

    /**
     * CAS登录回调
     */
    @GetMapping("/callback")
    public void callback(
            @RequestParam("ticket") String ticket,
            HttpServletResponse response
    ) throws IOException {

        /**
         * 1. 校验CAS ticket
         */
        String username =
                casService.validateTicket(ticket);

        if (username == null) {

            response.sendRedirect(
                    casProperties.getFrontendRedirectUrl()
                            + "?error=CAS认证失败"
            );

            return;
        }

        /**
         * 2. 查询业务系统用户
         */
        User user =
                userService.findByUsername(
                        username
                );

        if (user == null) {

            response.sendRedirect(
                    casProperties.getFrontendRedirectUrl()
                            + "?error=用户不存在"
            );

            return;
        }

        /**
         * 打印当前CAS用户和角色，
         * 方便测试角色分流。
         */
        System.out.println(
                "CAS username = "
                        + user.getUsername()
                        + ", role = "
                        + user.getRole()
        );

        /**
         * 3. 生成业务系统JWT
         */
        String token =
                jwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole()
                );

        /**
         * 4. 根据角色决定进入哪个前端
         */
        String role =
                user.getRole();

        String frontendRedirectUrl;

        /**
         * 管理员进入管理端
         */
        if (
                "ADMIN".equalsIgnoreCase(
                        role
                )
        ) {

            frontendRedirectUrl =
                    "http://localhost:5173/cas/callback";

        }

        /**
         * 学生、教师进入师生端
         */
        else if (
                "STUDENT".equalsIgnoreCase(
                        role
                )
                        || "TEACHER".equalsIgnoreCase(
                        role
                )
        ) {

            frontendRedirectUrl =
                    "http://localhost:5174/cas/callback";

        }

        /**
         * 未知角色
         */
        else {

            String errorUrl =
                    UriComponentsBuilder
                            .fromHttpUrl(
                                    "http://localhost:5173/cas/callback"
                            )
                            .queryParam(
                                    "error",
                                    "未知用户角色：" + role
                            )
                            .build()
                            .encode()
                            .toUriString();

            response.sendRedirect(
                    errorUrl
            );

            return;
        }

        /**
         * 5. 携带JWT跳转到对应前端
         */
        String frontendUrl =
                UriComponentsBuilder
                        .fromHttpUrl(
                                frontendRedirectUrl
                        )
                        .queryParam(
                                "token",
                                token
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(
                frontendUrl
        );
    }
}
