package com.singularbank.mifid.controller.helpers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Respuesta con la información del test y sus preguntas evaluadas")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswersTestResponseDTO {

  @Schema(description = "ID del test", example = "1")
  private Integer testId;

  @Schema(description = "Información de versión del test")
  private Short version;

  @Schema(description = "Lista de preguntas con sus opciones seleccionadas")
  private List<QuestionDTO> questions;

  @Schema(description = "Pregunta con su opción seleccionada")
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuestionDTO {

    @Schema(description = "ID de la pregunta", example = "1")
    private Integer id;

    @Schema(description = "ID de la pregunta", example = "1")
    private String text;

    @Schema(description = "Opción seleccionada para la pregunta")
    private OptionDTO option;
  }

  @Schema(description = "Opción de respuesta con su puntuación")
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OptionDTO {

    @Schema(description = "ID de la opción", example = "1")
    private Integer id;

    @Schema(description = "Puntuación de la opción", example = "100")
    private Integer score;
  }
}