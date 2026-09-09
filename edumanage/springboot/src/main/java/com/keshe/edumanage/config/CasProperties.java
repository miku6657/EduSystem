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
     * 回调地址
     */
    private String clientServiceUrl;

}
