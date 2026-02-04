package com.singularbank.mifid.entity;

public record HealthResult(
    boolean isHealthy,
    String message
) {
  public static HealthResult healthy() {
    return new HealthResult(true, "OK");
  }

  public static HealthResult unhealthy(String message) {
    return new HealthResult(false, message);
  }
}
