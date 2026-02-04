package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Complete assessment questionnaire with version and questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionsTest {

  @Schema(description = "Questionnaire version information")
  private VersionDTO version;

  @Schema(description = "List of questionnaire questions")
  private List<QuestionDTO> questions;
}