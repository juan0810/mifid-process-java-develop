package com.singularbank.mifid;

import com.singularbank.lib.rest.adapter.config.ExternalServiceProperties;
import com.singularbank.lib.rest.adapter.config.RestAdapterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties({RestAdapterProperties.class, ExternalServiceProperties.class})
@ComponentScan(
    basePackages = {
        "com.singularbank.mifid",
        "com.singularbank.lib.rest.adapter"
    })
public class SvcMifidJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(SvcMifidJavaApplication.class, args);
  }
}