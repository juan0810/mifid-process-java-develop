package com.singularbank.mifid.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerActiveTests {
  private String customerIdentity;
  private List<ActiveTest> activeTests;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ActiveTest {
    private Integer testId;
    private String serviceName;
    private TypeTest testType;
    private String testResult;
    private String testResultDescription;
    private SustainabilityPreferences sustainabilityPreferences;
    private List<ProductFamily> families;
    private LocalDateTime creationDate;
    private LocalDateTime signatureDate;
    private LocalDate expirationDate;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SustainabilityPreferences {

    private Integer percentInPortfolio;
    private Integer percentSustainableInvestment;
    private Integer percentEUTaxonomyAlignment;
    private Boolean greenhouseGasPAI;
    private Boolean environmentalPAI;
    private Boolean socialPAI;
    private Boolean generalPAI;
  }
}
