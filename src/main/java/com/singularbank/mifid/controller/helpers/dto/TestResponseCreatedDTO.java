package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response with created test information and results")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestResponseCreatedDTO(

    @Schema(description = "ID of the created test", example = "123")
    Integer responseClientId,

    @Schema(description = "Test results by type")
    TestResults results
) {

  @Schema(description = "Test results grouped by type")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record TestResults(

      @Schema(description = "Convenience test result", nullable = true)
      ConvenienceResult convenience,

      @Schema(description = "Suitability test result", nullable = true)
      SuitabilityResult suitability,

      @Schema(description = "Sustainability test result", nullable = true)
      SustainabilityResult sustainability
  ) {

  }

  @Schema(description = "Convenience test result with families")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ConvenienceResult(

      @Schema(description = "Family codes", example = "A,B,C,D")
      String result,

      @Schema(description = "Human-readable description")
      String description,

      @Schema(description = "List of product families")
      List<FamilyDTO> families
  ) {

  }

  @Schema(description = "Suitability test result")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record SuitabilityResult(

      @Schema(description = "Risk profile", example = "Conservador")
      String result,

      @Schema(description = "Human-readable description")
      String description
  ) {

  }

  @Schema(description = "Sustainability test result")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record SustainabilityResult(

      @Schema(description = "Sustainability result text")
      String result,

      @Schema(description = "Human-readable description")
      String description
  ) {

  }

  @Schema(description = "Product family information")
  public record FamilyDTO(

      @Schema(description = "Family code", example = "A")
      String code,

      @Schema(description = "Family description")
      String description
  ) {

  }
}