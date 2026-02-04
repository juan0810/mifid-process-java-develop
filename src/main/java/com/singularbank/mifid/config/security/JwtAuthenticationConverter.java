package com.singularbank.mifid.config.security;

import java.util.Collection;
import java.util.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = false)
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String EMAIL_CLAIM = "email";
  private static final String SUB_CLAIM = "sub";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    String principal = extractPrincipal(jwt);
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

    return new JwtAuthenticationToken(jwt, authorities, principal);
  }

  private String extractPrincipal(Jwt jwt) {
    String email = jwt.getClaimAsString(EMAIL_CLAIM);
    if (email != null && !email.isEmpty()) {
      return email;
    }

    String subject = jwt.getClaimAsString(SUB_CLAIM);
    return subject != null ? subject : "unknown";
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    // TODO: Implementar extracción de roles cuando se definan los requisitos
    // Por ahora, retornamos una lista vacía (solo autenticación, sin autorización por roles)
    return Collections.emptyList();
  }
}
