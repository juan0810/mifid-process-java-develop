package com.singularbank.mifid.config.database;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

  private static final String SYSTEM_USER = "SYSTEM";

  @Override
  public Optional<String> getCurrentAuditor() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null || !authentication.isAuthenticated()) {
        log.debug("No authentication found, using default auditor: {}", SYSTEM_USER);
        return Optional.of(SYSTEM_USER);
      }

      String username = authentication.getName();

      if ("anonymousUser".equals(username)) {
        log.debug("Anonymous user detected, using default auditor: {}", SYSTEM_USER);
        return Optional.of(SYSTEM_USER);
      }

      log.debug("Current auditor: {}", username);
      return Optional.of(username);
      
    } catch (Exception e) {
      log.warn("Error getting current auditor from security context, using default: {}", 
          SYSTEM_USER, e);
      return Optional.of(SYSTEM_USER);
    }
  }
}
