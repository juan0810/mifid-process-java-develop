package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Questionnaire version information")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionDTO {

  @Schema(description = "Unique version identifier", example = "1")
  private int id;

  @Schema(description = "Release date and time", example = "2025-10-10T15:22:58.425065")
  private LocalDateTime releaseDate;
}