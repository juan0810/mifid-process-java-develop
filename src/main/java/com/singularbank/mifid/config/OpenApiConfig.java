package com.singularbank.mifid.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Value("${app.environment:local}")
  private String environment;

  @Value("${server.port:8080}")
  private String serverPort;

  // TODO  LLEVAR COMO VARIABLE DE ENTORNO
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("SingularBank - API de Productos Bancarios")
            .description("API REST para la gestión de productos bancarios de SingularBank")
            .version("v1.0.0")
            .contact(new Contact()
                .name("SingularBank Team")
                .email("api-support@singularbank.com"))
            .license(new License()
                .name("Proprietary")
                .url("https://singularbank.com")))
        .servers(List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor " + environment)
        ));
  }
}