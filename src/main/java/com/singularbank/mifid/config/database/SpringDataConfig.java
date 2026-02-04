package com.singularbank.mifid.config.database;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(
    basePackages = "com.singularbank.mifid.repository.database.model")
@EnableJpaRepositories(
    basePackages = "com.singularbank.mifid.repository.database")
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class SpringDataConfig {
}