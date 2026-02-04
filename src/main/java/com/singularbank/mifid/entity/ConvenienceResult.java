package com.singularbank.mifid.entity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConvenienceResult {

  @Builder.Default
  private Set<String> convenientFamilies = new LinkedHashSet<>();

  @Builder.Default
  private Set<String> notConvenientFamilies = new LinkedHashSet<>();

  @Builder.Default
  private Set<String> familiesWithAlerts = new LinkedHashSet<>();

  @Builder.Default
  private List<String> alerts = new ArrayList<>();

  @Builder.Default
  private List<Integer> combinationIds = new ArrayList<>();

  public String formatConvenientFamiliesAsCommaSeparated() {
    if (convenientFamilies == null || convenientFamilies.isEmpty()) {
      return "";
    }

    return convenientFamilies.stream()
        .sorted()
        .collect(Collectors.joining(","));
  }

  public String formatResultText() {
    if (convenientFamilies != null && !convenientFamilies.isEmpty()) {
      return "Conveniente";
    }
    return "No conveniente";
  }
}