package com.singularbank.mifid.controller.helpers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud con las respuestas del test MIFID (sostenibilidad, conveniencia, idoneidad)")
public class AnswersTestRequestDTO {

  @NotNull
  @Schema(description = "Descripción de la aplicación propietaria de la versión del test", example = "ONBOARDING")
  private String service;

  @NotNull
  @Schema(description = "ID de la versión del test", example = "1")
  private Short version;

  @NotEmpty
  @Valid
  @Schema(description = "Lista de respuestas a las preguntas")
  private List<QuestionResponseDTO> questionResponses;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Respuesta individual a una pregunta")
  public static class QuestionResponseDTO {

    @NotNull
    @Schema(description = "ID de la pregunta", example = "10")
    private Integer questionId;

    @Schema(description = "ID de la opción seleccionada", example = "89")
    private Integer selectedOptionId;
  }
}