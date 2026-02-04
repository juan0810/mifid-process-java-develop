package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Questionnaire question with answer options")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDTO {

  @Schema(description = "Unique question identifier", example = "1")
  private long id;

  @Schema(description = "Associated product family code", example = "E")
  private String familyCode;

  @Schema(description = "Question text", example = "What is your level of education?")
  private String text;

  @Schema(description = "List of available answer options")
  private List<OptionDTO> options;

  @Schema(description = "List of sub-questions (for hierarchical question structure)")
  private List<QuestionDTO> subQuestions;
}