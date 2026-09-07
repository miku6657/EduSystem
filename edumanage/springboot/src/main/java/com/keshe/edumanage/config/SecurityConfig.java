package com.keshe.edumanage.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Spring Security配置
 */
@Configuration
public class SecurityConfig {


    /**
     * Security过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http

                //关闭csrf
                .csrf(csrf -> csrf.disable())


                //请求权限配置
                .authorizeHttpRequests(auth -> auth

                        //所有请求暂时放行
                        .anyRequest()
                        .permitAll()
                );


        return http.build();
    }

}