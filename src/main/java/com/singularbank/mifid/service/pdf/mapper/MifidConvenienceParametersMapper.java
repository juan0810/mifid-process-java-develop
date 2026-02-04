package com.singularbank.mifid.service.pdf.mapper;

import com.singularbank.mifid.entity.MifidConvenienceQuestionMapping;
import com.singularbank.mifid.entity.MifidConvenienceQuestionMapping.AnswerType;
import com.singularbank.mifid.entity.ProductFamily;
import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MifidConvenienceParametersMapper {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy/MM/dd HH:mm");
  private static final String TIMEZONE_SUFFIX = " GMT+02:00";
  private static final String NO_FAMILIES_MESSAGE = "No hay familias convenientes";
  private static final String NOT_AVAILABLE = "N/A";

  private static final Map<String, Object> DEFAULT_ANSWERS = Map.ofEntries(
      // Conocimiento
      Map.entry("nivel_estudios", "C"),
      Map.entry("experiencia_financiera", "D"),
      Map.entry("conocimiento_indice", "D"),
      Map.entry("riesgo_bonos", "C"),
      Map.entry("riesgo_insolvencia", "B"),
      Map.entry("riesgo_participaciones", "B"),
      Map.entry("inversion_inmuebles", "D"),
      Map.entry("fondo_capital_riesgo", "B"),
      Map.entry("derivados_otc", "C"),
      // Experiencia
      Map.entry("depositos_bancarios", 0),
      Map.entry("renta_fija_publica", 0),
      Map.entry("renta_variable_cotizada", 0),
      Map.entry("fondos_inversion_ucits", 0),
      Map.entry("renta_fija_compleja", 0),
      Map.entry("productos_estructurados", 0),
      Map.entry("bonos_convertibles", 0),
      Map.entry("fondos_inmobiliarios_1", 0),
      Map.entry("productos_capital_riesgo", 0),
      Map.entry("fondos_inmobiliarios_2", 0),
      Map.entry("derivados_cotizados", 0),
      Map.entry("cfds_derivados_otc", 0),
      Map.entry("experiencia_otra_entidad", false)
  );

  public Map<String, Object> mapToMifidParameters(RespuestaCliente respuestaCliente) {
    log.debug("Mapping RespuestaCliente to PDF parameters for client: {}",
        respuestaCliente.getClienteDni());

    Map<String, Object> params = new HashMap<>();

    mapBasicClientData(params, respuestaCliente);
    mapTestResults(params, respuestaCliente);
    mapSignatureDates(params, respuestaCliente.getFechaFirma());
    mapIndividualAnswers(params, respuestaCliente.getDetalles());
    mapResultDescription(params, respuestaCliente.getResultadoConveniencia());

    log.debug("Mapped {} parameters for PDF generation", params.size());
    return params;
  }

  private void mapBasicClientData(Map<String, Object> params, RespuestaCliente respuesta) {
    String dni = respuesta.getClienteDni();
    params.put("nombre_denominacion", dni);
    params.put("nif", dni);
    params.put("firma_nombre", dni);
    params.put("digitally_signed_by", dni);
  }

  private void mapTestResults(Map<String, Object> params, RespuestaCliente respuesta) {
    params.put("perfil_obtenido", valueOrDefault(respuesta.getResultadoIdoneidad()));
    params.put("perfil_conveniencia", valueOrDefault(respuesta.getResultadoConveniencia()));
  }

  private void mapSignatureDates(Map<String, Object> params, LocalDateTime fechaFirma) {
    if (fechaFirma == null) {
      params.put("fecha_firma", NOT_AVAILABLE);
      params.put("signature_date", NOT_AVAILABLE);
      return;
    }

    String formattedDate = fechaFirma.format(DATE_FORMATTER);
    params.put("fecha_firma", formattedDate);
    params.put("signature_date", formattedDate + TIMEZONE_SUFFIX);
  }

  private void mapIndividualAnswers(Map<String, Object> params, List<RespuestaDetalle> detalles) {
    if (detalles == null || detalles.isEmpty()) {
      log.warn("No answers found to map");
      params.putAll(DEFAULT_ANSWERS);
      return;
    }

    detalles.stream()
        .filter(this::isValidDetalle)
        .forEach(detalle -> mapDetalle(params, detalle));

    DEFAULT_ANSWERS.forEach(params::putIfAbsent);
  }

  private boolean isValidDetalle(RespuestaDetalle detalle) {
    if (detalle.getAnswer() == null || detalle.getAnswer().getQuestion() == null) {
      log.warn("Skipping detail with null respuesta or pregunta");
      return false;
    }

    if (isBlank(detalle.getValor())) {
      log.warn("Skipping question {} with null or blank value",
          detalle.getAnswer().getQuestion().getId());
      return false;
    }

    return true;
  }

  private void mapDetalle(Map<String, Object> params, RespuestaDetalle detalle) {
    Integer preguntaId = detalle.getAnswer().getQuestion().getId();
    String valor = detalle.getValor();

    MifidConvenienceQuestionMapping.fromPreguntaId(preguntaId)
        .ifPresent(mapping -> {
          Object converted = convertValue(valor, mapping.getAnswerType());
          params.put(mapping.getPdfFieldName(), converted);
          log.trace("Mapped question {} ({}) = {}", preguntaId, mapping.getPdfFieldName(),
              converted);
        });
  }

  private Object convertValue(String valor, AnswerType type) {
    return switch (type) {
      case STRING -> valor;
      case INTEGER -> parseIntOrDefault(valor);
      case BOOLEAN -> Boolean.parseBoolean(valor);
    };
  }

  private int parseIntOrDefault(String valor) {
    try {
      return Integer.parseInt(valor);
    } catch (NumberFormatException e) {
      log.error("Error converting value '{}' to INTEGER", valor);
      return 0;
    }
  }

  private void mapResultDescription(Map<String, Object> params, String convenienceResult) {
    params.put("resultado_test", buildConvenienceResultDescription(convenienceResult));
  }

  private String buildConvenienceResultDescription(String convenienceResult) {
    if (isBlank(convenienceResult)) {
      return NO_FAMILIES_MESSAGE;
    }

    String familiesDescription = Arrays.stream(convenienceResult.split(","))
        .map(String::trim)
        .filter(code -> !code.isBlank())
        .map(this::parseProductFamily)
        .flatMap(Optional::stream)
        .map(ProductFamily::getDescription)
        .collect(Collectors.joining(", "));

    return familiesDescription.isEmpty()
        ? NO_FAMILIES_MESSAGE
        : "Conveniente para " + familiesDescription;
  }

  private Optional<ProductFamily> parseProductFamily(String code) {
    try {
      return Optional.of(ProductFamily.valueOf(code));
    } catch (IllegalArgumentException e) {
      log.warn("Invalid product family code: {}", code);
      return Optional.empty();
    }
  }

  private String valueOrDefault(String value) {
    return isBlank(value) ? NOT_AVAILABLE : value;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}