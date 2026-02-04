package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Response containing the convenience test result and generated alerts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertsTestResponseDTO {

  @Schema(
      description = "Convenience test evaluation result",
      example = "Conveniente",
      allowableValues = {"Conveniente", "No conveniente"}
  )
  private String result;

  @Schema(description = "List of non-convenient product families with details")
  @Builder.Default
  private List<AlertDTO> alerts = Collections.emptyList();

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AlertDTO {

    @Schema(description = "Family code", example = "A")
    private String code;

    @Schema(
        description = "Family description",
        example = "Depósitos bancarios e imposiciones a plazo fijo"
    )
    private String description;
  }
}