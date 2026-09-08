package com.keshe.edumanage.config;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置
 */
@Configuration
public class MybatisPlusConfig {
    /**
     * MyBatis-Plus插件配置
     *
     * 目前添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor =
                new MybatisPlusInterceptor();

        /**
         * MySQL分页支持
         */
        interceptor.addInnerInterceptor(
                new PaginationInnerInterceptor(
                        DbType.MYSQL
                )
        );
        return interceptor;
    }
}
