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
         * 3. 生成业务系统JWT
         */
        String token =
                jwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole()
                );


        /**
         * 4. 跳回Vue
         */
        String frontendUrl =
                UriComponentsBuilder
                        .fromHttpUrl(
                                casProperties.getFrontendRedirectUrl()
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
