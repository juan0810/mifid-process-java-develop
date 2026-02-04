package com.singularbank.mifid.controller.answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.controller.answer.mapper.SaveAnswersMapper;
import com.singularbank.mifid.controller.answer.request.SaveAnswersRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.service.answer.SaveAnswersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test-mifid")
@RequiredArgsConstructor
@Tag(name = "MiFID Test Answers", description = "Operations to manage MiFID test answers")
public class SaveAnswersController {

  private final SaveAnswersService saveAnswersService;
  private final SaveAnswersMapper mapper;
  private final ObjectMapper objectMapper; // 1. Inyectamos ObjectMapper

  @PostMapping("/answers/{document-number}")
  @Operation(
      summary = "Save MiFID test answers",
      description = "Persists client answers for one, two or all three MiFID tests in a single call"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test answers saved successfully",
          content = @Content(schema = @Schema(implementation = TestResponseCreatedDTO.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request (validation errors)"),
      @ApiResponse(responseCode = "404", description = "Client identifier not found"),
      @ApiResponse(responseCode = "422", description = "Question does not match declared test type"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TestResponseCreatedDTO> saveTestAnswers(
      @Parameter(description = "Client identity document number", required = true, example = "12345678A")
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber,
      @Valid @RequestBody SaveAnswersRequestDTO request) {

    // 2. LOG MEJORADO: Serializamos a JSON para ver EXACTAMENTE qué interpretó Java
    try {
      log.info("ENTRADA DEV - Document: {} - Raw JSON Payload: {}",
          documentNumber,
          objectMapper.writeValueAsString(request));

      // Verificación rápida de listas vacías (causa común del perfil Conservador)
      if (request.tests() == null || request.tests().isEmpty()) {
        log.warn(
            "ALERTA DEV: La lista de 'tests' ha llegado VACÍA o NULA. Revisar mapeo Mulesoft.");
      }
    } catch (JsonProcessingException e) {
      log.error("Error serializando log de entrada", e);
    }

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(documentNumber,
        mapper.toDomain(request));

    // 3. LOG DE SALIDA: Ver qué estamos devolviendo
    try {
      log.info("SALIDA DEV - Response JSON: {}", objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      log.error("Error serializando log de salida", e);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}