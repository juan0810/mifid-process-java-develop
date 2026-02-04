package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Answer option for a question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionDTO {

  @Schema(description = "Unique option identifier", example = "1")
  private long id;

  @Schema(description = "Descriptive text of the answer option",
      example = "University studies with technical component")
  private String text;

  @Schema(description = "Indicates whether this option is the correct answer", example = "true")
  private boolean correct;
}