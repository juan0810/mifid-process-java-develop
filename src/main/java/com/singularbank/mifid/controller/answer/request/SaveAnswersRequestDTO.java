package com.singularbank.mifid.controller.answer.request;

import com.singularbank.mifid.entity.TypeTest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Request with MiFID test answers (Suitability, Convenience, Sustainability)")
public record SaveAnswersRequestDTO(

    @NotNull
    @Schema(description = "Application owner of the test version", example = "ONBOARDING")
    String service,

    @NotNull
    @Schema(description = "Test version ID", example = "1")
    Short version,

    @NotEmpty
    @Valid
    @Schema(description = "List of tests with their answers")
    List<BlockTest> tests
) {

  @Schema(description = "Test block with type and answers")
  public record BlockTest(

      @NotNull
      @Schema(description = "Test type", example = "SUITABILITY",
          allowableValues = {"SUITABILITY", "CONVENIENCE", "SUSTAINABILITY"})
      TypeTest type,

      @NotEmpty
      @Valid
      @Schema(description = "Test answers")
      List<QuestionResponseDTO> questionResponses
  ) {

  }

  @Schema(description = "Individual answer to a question")
  public record QuestionResponseDTO(

      @NotNull
      @Schema(description = "Question ID", example = "10")
      Integer questionId,

      @Schema(description = "Selected option ID", example = "89")
      Integer selectedOptionId
  ) {

  }
}
