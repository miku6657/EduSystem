package com.keshe.edumanage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "cas")
public class CasProperties {

    /**
     * CAS服务器地址
     */
    private String serverUrlPrefix;


    /**
     * CAS回调后端地址
     */
    private String clientServiceUrl;


    /**
     * CAS认证完成后跳转前端地址
     */
    private String frontendRedirectUrl;

}
