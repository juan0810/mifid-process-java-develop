package com.singularbank.mifid.controller.helpers;

import com.singularbank.mifid.config.AppConfig;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@Slf4j
public class ConfigController {

  private final AppConfig appConfig;

  @Value("${spring.profiles.active:default}")
  private String activeProfile;

  @Autowired
  public ConfigController(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getConfig() {
    log.info("[ConfigController] Solicitando configuración de la aplicación");
    try {
      Map<String, Object> config = new HashMap<>();
      config.put("activeProfile", activeProfile);
      config.put("environment", appConfig.getEnvironment());
      config.put("apiTimeout", appConfig.getApiTimeout());

      log.info("[ConfigController] Configuración obtenida exitosamente - Perfil: {}, Entorno: {}",
          activeProfile, appConfig.getEnvironment());
      log.debug("[ConfigController] Detalles de configuración: timeout={}",
          appConfig.getApiTimeout());

      return ResponseEntity.ok(config);
    } catch (Exception e) {
      log.error("[ConfigController] Error al obtener la configuración: {}", e.getMessage(), e);
      throw e;
    }
  }
}