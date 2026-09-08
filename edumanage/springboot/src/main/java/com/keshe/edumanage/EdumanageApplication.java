package com.keshe.edumanage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.keshe.edumanage.mapper")
@SpringBootApplication
public class EdumanageApplication {
    public static void main(String[] args) {
        SpringApplication.run(
                EdumanageApplication.class,
                args
        );
    }
}
