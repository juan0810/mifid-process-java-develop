package com.singularbank.mifid.controller.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.singularbank.mifid.entity.TypeTest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Response with all active MIFID tests for the customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentCustomerTestsResponseDTO {

  @Schema(description = "Customer identity", example = "12345678A")
  private String customerIdentity;

  @Schema(description = "List of active tests")
  private List<ActiveTestDTO> activeTests;

  @Schema(description = "Active MIFID test information")
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ActiveTestDTO {

    @Schema(description = "Test ID", example = "1234")
    private Short testId;

    @Schema(description = "Service name", example = "ONBOARDING")
    private String serviceName;

    @Schema(description = "Test type", example = "CONVENIENCE", allowableValues = {"SUITABILITY",
        "SUSTAINABILITY", "CONVENIENCE"})
    private TypeTest testType;

    @Schema(description = "Test result. For CONVENIENCE: family codes (e.g., 'A,B,C,D'). For SUSTAINABILITY: preference description. For SUITABILITY: risk profile",
        example = "A,B,C,D")
    private String testResult;

    @Schema(description = "Test result description (only for CONVENIENCE tests)",
        example = "Conveniente para Depósitos bancarios e imposiciones a plazo fijo, Renta fija pública...")
    private String testResultDescription;

    @Schema(description = "Sustainability preferences detail (only for SUSTAINABILITY tests)")
    private SustainabilityPreferencesDTO sustainabilityPreferences;

    @Schema(description = "Product families (only for CONVENIENCE tests)")
    private List<FamilyDTO> families;

    @Schema(description = "Test creation date", example = "2025-10-16T16:44:50")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDate;

    @Schema(description = "Test signature date", example = "2025-10-21T17:15:13")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime signatureDate;

    @Schema(description = "Test expiration date", example = "2026-10-16")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;
  }

  @Schema(description = "Product family information for convenience tests")
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FamilyDTO {

    @Schema(description = "Family code", example = "A")
    private String code;

    @Schema(description = "Family description", example = "Depósitos bancarios e imposiciones a plazo fijo")
    private String description;
  }

  @Schema(description = "Sustainability preferences structure (only for SUSTAINABILITY tests)")
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SustainabilityPreferencesDTO {

    @Schema(description = "Percentage of sustainable products in portfolio", example = "20", nullable = true)
    private Integer percentInPortfolio;

    @Schema(description = "Minimum percentage of sustainable investment", example = "5", nullable = true)
    private Integer percentSustainableInvestment;

    @Schema(description = "Minimum percentage of EU Taxonomy alignment", example = "2", nullable = true)
    private Integer percentEUTaxonomyAlignment;

    @Schema(description = "Consider Principal Adverse Impacts on greenhouse gas emissions", example = "false")
    private Boolean greenhouseGasPAI;

    @Schema(description = "Consider Principal Adverse Impacts on environmental matters", example = "false")
    private Boolean environmentalPAI;

    @Schema(description = "Consider Principal Adverse Impacts on social and labour matters", example = "false")
    private Boolean socialPAI;

    @Schema(description = "Consider all Principal Adverse Impacts in general", example = "true")
    private Boolean generalPAI;
  }
}