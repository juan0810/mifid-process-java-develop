package com.singularbank.mifid.entity;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MifidConvenienceQuestionMapping {

  NIVEL_ESTUDIOS(1, "nivel_estudios", AnswerType.STRING),
  EXPERIENCIA_FINANCIERA(2, "experiencia_financiera", AnswerType.STRING),
  CONOCIMIENTO_INDICE(3, "conocimiento_indice", AnswerType.STRING),
  RIESGO_BONOS(4, "riesgo_bonos", AnswerType.STRING),
  RIESGO_INSOLVENCIA(5, "riesgo_insolvencia", AnswerType.STRING),
  RIESGO_PARTICIPACIONES(6, "riesgo_participaciones", AnswerType.STRING),
  INVERSION_INMUEBLES(7, "inversion_inmuebles", AnswerType.STRING),
  FONDO_CAPITAL_RIESGO(8, "fondo_capital_riesgo", AnswerType.STRING),
  DERIVADOS_OTC(9, "derivados_otc", AnswerType.STRING),

  DEPOSITOS_BANCARIOS(101, "depositos_bancarios", AnswerType.INTEGER),
  RENTA_FIJA_PUBLICA(102, "renta_fija_publica", AnswerType.INTEGER),
  RENTA_VARIABLE_COTIZADA(103, "renta_variable_cotizada", AnswerType.INTEGER),
  FONDOS_INVERSION_UCITS(104, "fondos_inversion_ucits", AnswerType.INTEGER),
  RENTA_FIJA_COMPLEJA(105, "renta_fija_compleja", AnswerType.INTEGER),
  PRODUCTOS_ESTRUCTURADOS(106, "productos_estructurados", AnswerType.INTEGER),
  BONOS_CONVERTIBLES(107, "bonos_convertibles", AnswerType.INTEGER),
  FONDOS_INMOBILIARIOS_1(108, "fondos_inmobiliarios_1", AnswerType.INTEGER),
  PRODUCTOS_CAPITAL_RIESGO(109, "productos_capital_riesgo", AnswerType.INTEGER),
  FONDOS_INMOBILIARIOS_2(110, "fondos_inmobiliarios_2", AnswerType.INTEGER),
  DERIVADOS_COTIZADOS(111, "derivados_cotizados", AnswerType.INTEGER);

  private final Integer preguntaId;
  private final String pdfFieldName;
  private final AnswerType answerType;

  public static Optional<MifidConvenienceQuestionMapping> fromPreguntaId(Integer preguntaId) {
    if (preguntaId == null) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(mapping -> mapping.preguntaId.equals(preguntaId))
        .findFirst();
  }

  public enum AnswerType {
    STRING,  // Valores: A, B, C, D
    INTEGER, // Valores: 0, 1, 2
    BOOLEAN  // Valores: true, false
  }
}