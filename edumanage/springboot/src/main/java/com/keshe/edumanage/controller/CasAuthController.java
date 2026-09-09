package com.keshe.edumanage.controller;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.config.CasProperties;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.CasService;
import com.keshe.edumanage.service.UserService;
import com.keshe.edumanage.util.JWTUtil;
import com.keshe.edumanage.vo.LoginVO;
import com.keshe.edumanage.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * CAS 单点登录控制器
 */
@RestController
@RequestMapping("/api/auth/cas")
@RequiredArgsConstructor
public class CasAuthController {
    private final CasProperties casProperties;
    private final CasService casService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    /**
     * 发起 CAS 登录：重定向到 CAS Server 登录页
     */
    @GetMapping("/login")
    public void login(
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl = casProperties.getServerUrlPrefix()
                + "/login?service="
                + casProperties.getClientServiceUrl();
        response.sendRedirect(redirectUrl);
    }

    /**
     * CAS 登录回调：校验 ticket，签发 JWT
     */
    @GetMapping("/callback")
    public Result<LoginVO> callback(
            @RequestParam("ticket") String ticket
    ) {
        String username = casService.validateTicket(ticket);
        if (username == null) {
            return Result.fail("CAS 票据校验失败");
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        String token = jwtUtil.generateToken(
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
