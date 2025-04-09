package com.project.withus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;

@Configuration
public class ThymeleafConfig {

    // 🔥 이 Bean만 있으면 #httpServletRequest 사용 가능!
    @Bean
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }
}
