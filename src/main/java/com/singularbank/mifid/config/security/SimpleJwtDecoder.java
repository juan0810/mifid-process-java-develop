package com.singularbank.mifid.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = false)
public class SimpleJwtDecoder implements JwtDecoder {

  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

  private final ObjectMapper objectMapper;

  public SimpleJwtDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Jwt decode(String token) throws JwtException {
    String[] parts = token.split("\\.", 3);

    if (parts.length != 3) {
      throw new JwtException("Invalid JWT format: expected 3 parts, got " + parts.length);
    }

    try {
      byte[] payloadBytes = DECODER.decode(parts[1]);
      Map<String, Object> claims = objectMapper.readValue(payloadBytes, MAP_TYPE);
      Instant issuedAt = extractInstant(claims, "iat");
      Instant expiresAt = extractInstant(claims, "exp");

      return Jwt.withTokenValue(token)
          .headers(headers -> headers.put("alg", "none"))
          .claims(c -> c.putAll(claims))
          .issuedAt(issuedAt)
          .expiresAt(expiresAt)
          .build();

    } catch (Exception e) {
      throw new JwtException("Failed to decode JWT: " + e.getMessage(), e);
    }
  }

  private Instant extractInstant(Map<String, Object> claims, String claim) {
    return switch (claims.get(claim)) {
      case Long timestamp -> Instant.ofEpochSecond(timestamp);
      case Integer timestamp -> Instant.ofEpochSecond(timestamp.longValue());
      case null, default -> null;
    };
  }
}
