package com.singularbank.mifid.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {
    @Value("${app.environment}")
    private String environment;

    @Value("${app.api.timeout}")
    private Integer apiTimeout;

    
} 