package com.singularbank.mifid.service.helpers.impl;

import com.singularbank.mifid.entity.HealthResult;
import com.singularbank.mifid.service.helpers.CheckDatabaseHealthService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckDatabaseHealthServiceImpl implements CheckDatabaseHealthService {

  private final DataSource dataSource;

  @Value("${health.readiness.database.timeout:5}")
  private int timeoutSeconds;

  @Override
  public HealthResult check() {
    CompletableFuture<HealthResult> future = null;
    try {
      future = CompletableFuture.supplyAsync(this::testConnection);
      return future.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      log.warn("Database health check timeout after {} seconds", timeoutSeconds);
      return HealthResult.unhealthy("error: timeout");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Database health check was interrupted", e);
      return HealthResult.unhealthy("error: interrupted");
    } catch (ExecutionException e) {
      log.error("Database health check failed during execution", e);
      return HealthResult.unhealthy("error: " + e.getCause().getMessage());
    }
  }

  private HealthResult testConnection() {
    try (Connection connection = dataSource.getConnection()) {
      return connection.isValid(1)
          ? HealthResult.healthy()
          : HealthResult.unhealthy("error: invalid connection");
    } catch (SQLException e) {
      log.error("Failed to connect to database", e);
      return HealthResult.unhealthy("error: " + e.getMessage());
    }
  }
}