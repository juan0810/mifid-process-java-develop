package com.singularbank.mifid.config.tracing;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class CorrelationFilter implements Filter {

  private static final String HEADER_NAME = "X-Correlation-ID";

  private static final ThreadLocal<String> correlationIdHolder = new ThreadLocal<>();

  public static String getCorrelationId() {
    return correlationIdHolder.get();
  }

  public static void clearCorrelationId() {
    correlationIdHolder.remove();
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    String correlationId =
        Optional.ofNullable(request.getHeader(HEADER_NAME))
            .filter(h -> !h.isBlank())
            .orElse(UUID.randomUUID().toString());

    try {
      correlationIdHolder.set(correlationId);
      MDC.put("traceId", correlationId);

      response.setHeader(HEADER_NAME, correlationId);

      chain.doFilter(request, response);

    } finally {
      MDC.remove("traceId");
      correlationIdHolder.remove();
    }
  }
}
