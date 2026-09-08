package com.keshe.edumanage.security;


import com.keshe.edumanage.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Collections;



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



        //获取请求头
        String header =
                request.getHeader("Authorization");



        //判断是否携带token

        if(header != null
                && header.startsWith("Bearer ")) {


            String token =
                    header.substring(7);



            //验证token

            if(jwtUtil.validateToken(token)){


                String username =
                        jwtUtil.getUsername(token);


                String role =
                        jwtUtil.getRole(token);



                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.emptyList()
                        );


                //保存认证信息

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

            }

        }



        filterChain.doFilter(
                request,
                response
        );

    }
}