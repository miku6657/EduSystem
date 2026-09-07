package com.keshe.edumanage.security;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.keshe.edumanage.filter.AuthFilter;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class ShiroConfig {

    @Bean
    ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();

        Map<String, Filter> filter = new HashMap<>();
        filter.put("user", new AuthFilter());
        bean.setFilters(filter);

        bean.setSecurityManager(securityManager());
        bean.setLoginUrl("/unauthorized/noLogin");
        bean.setUnauthorizedUrl("/unauthorized/noPerm");

        Map<String, String> map = new LinkedHashMap<>();
        // TODO: 在此配置各 URL 的访问权限(anon / user / 角色等)
        map.put("/**", "anon");
        bean.setFilterChainDefinitionMap(map);
        return bean;
    }

    @Bean
    DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // TODO: 注入自定义 Realm(实现 doGetAuthenticationInfo / doGetAuthorizationInfo)
        // manager.setRealm(myRealm());
        return manager;
    }

    /**
     * 下面的代码是添加注解支持
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
