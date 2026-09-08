package com.keshe.edumanage.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger接口文档配置
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()

                .info(
                        new Info()

                                .title("教学过程管理系统接口文档")

                                .description(
                                        "Spring Boot3 + MyBatis-Plus + Security + JWT + CAS"
                                )

                                .version("1.0.0")
                );
    }
}
