package com.keshe.edumanage.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


import java.util.List;


/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {


    @Bean
    public CorsFilter corsFilter(){


        CorsConfiguration config =
                new CorsConfiguration();


        /**
         * 允许访问的前端地址
         *
         * 开发阶段：
         * Vue默认5173
         */
        config.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );


        /**
         * 允许请求方式
         */
        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );


        /**
         * 允许请求头
         *
         * JWT需要Authorization
         */
        config.setAllowedHeaders(
                List.of("*")
        );


        /**
         * 允许携带cookie/token
         */
        config.setAllowCredentials(true);



        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                config
        );


        return new CorsFilter(source);

    }

}