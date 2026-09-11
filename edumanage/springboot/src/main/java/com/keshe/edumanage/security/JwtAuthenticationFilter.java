package com.keshe.edumanage.security;


import com.keshe.edumanage.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    private final JWTUtil jwtUtil;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        /**
         * 1. 获取请求头中的Token
         */
        String header =
                request.getHeader("Authorization");


        /**
         * 2. 判断是否携带Bearer Token
         */
        if(header != null
                && header.startsWith("Bearer ")) {


            String token =
                    header.substring(7);


            /**
             * 3. 校验Token
             */
            if(jwtUtil.validateToken(token)) {


                /**
                 * 4. 从Token中获取用户信息
                 */
                String username =
                        jwtUtil.getUsername(token);


                String role =
                        jwtUtil.getRole(token);



                /**
                 * 5. 构造用户权限
                 *
                 * Spring Security角色规范:
                 * ROLE_ADMIN
                 * ROLE_TEACHER
                 */
                List<GrantedAuthority> authorities =
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        );



                /**
                 * 6. 创建认证对象
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );



                /**
                 * 7. 保存认证信息到SecurityContext
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);


            }

        }


        /**
         * 8. 放行请求
         */
        filterChain.doFilter(
                request,
                response
        );

    }
}