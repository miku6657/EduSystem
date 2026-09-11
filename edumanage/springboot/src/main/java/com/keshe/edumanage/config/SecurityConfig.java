package com.keshe.edumanage.config;
import com.keshe.edumanage.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;



@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /**
     * BCrypt密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();

    }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                /**
                 * JWT项目关闭CSRF
                 */
                .csrf(csrf ->
                        csrf.disable()
                )
                /**
                 * JWT无状态认证
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                /**
                 * 认证异常处理
                 */
                .exceptionHandling(exception -> exception
                        /**
                         * 未登录
                         */
                        .authenticationEntryPoint(
                                (request,response,authException)->{
                                    response.setContentType(
                                            "application/json;charset=UTF-8"
                                    );
                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );
                                    response.getWriter()
                                            .write(
                                                    "{\"message\":\"未登录，请先登录\"}"
                                            );

                                }
                        )
                        /**
                         * 无权限
                         */
                        .accessDeniedHandler(
                                (request,response,accessDeniedException)->{
                                    response.setContentType(
                                            "application/json;charset=UTF-8"
                                    );

                                    response.setStatus(
                                            HttpServletResponse.SC_FORBIDDEN
                                    );
                                    response.getWriter()
                                            .write(
                                                    "{\"message\":\"没有权限访问\"}"
                                            );

                                }
                        )

                )
                /**
                 * 接口权限控制
                 */
                .authorizeHttpRequests(auth -> auth

                        /**
                         * 前后端分离跨域预检请求
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /**
                         * 普通登录
                         */
                        .requestMatchers(
                                "/api/auth/login"
                        )
                        .permitAll()

                        /**
                         * CAS登录
                         */
                        .requestMatchers(
                                "/api/auth/cas/login",
                                "/api/auth/cas/callback"
                        )
                        .permitAll()

                        /**
                         * Swagger
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        /**
                         * 其他接口必须登录
                         */
                        .anyRequest()
                        .authenticated()

                )
                /**
                 * JWT过滤器
                 *
                 * 在UsernamePasswordAuthenticationFilter之前执行
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }
}
