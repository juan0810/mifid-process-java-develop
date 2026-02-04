package com.singularbank.mifid.entity;

import java.util.List;

public record SuitabilityResult(
    SuitabilityProfile profile,
    List<String> alerts,
    Integer combinationId
) {

  public SuitabilityResult {
    if (alerts == null) {
      alerts = List.of();
    }
  }

  public String getResult() {
    return profile != null ? profile.getDisplayName() : "No definido";
  }

  public boolean hasAlerts() {
    return alerts != null && !alerts.isEmpty();
  }
}