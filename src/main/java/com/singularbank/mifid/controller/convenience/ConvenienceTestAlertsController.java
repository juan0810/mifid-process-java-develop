package com.singularbank.mifid.controller.convenience;

import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.controller.helpers.dto.AlertsTestResponseDTO;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.controller.helpers.mapper.AnswersTestRequestMapper;
import com.singularbank.mifid.controller.helpers.mapper.ConvenienceResultMapper;
import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.service.convenience.ConvenienceAlertCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test-mifid")
@Validated
@Slf4j
public class ConvenienceTestAlertsController {

  private final ConvenienceAlertCalculationService convenienceAlertCalculationService;
  private final AnswersTestRequestMapper answersTestRequestMapper;
  private final ConvenienceResultMapper convenienceResultMapper;

  @Tag(name = "Convenience Test", description = "Operations to manage convenience test")
  @Operation(
      summary = "Calculate convenience test alerts",
      description = "Processes test responses and calculates alerts for the convenience test without persisting the results",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Alerts calculated successfully",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = AlertsTestResponseDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request or validation error",
              content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Client not found",
              content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
          )
      }
  )
  @PostMapping(value = "/convenience/alerts/{document-number}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AlertsTestResponseDTO> calculateConvenienceAlerts(
      @Parameter(
          description = "Client identity document number",
          required = true,
          example = "12345678A"
      )
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber,

      @Parameter(
          description = "Test responses to evaluate",
          required = true
      )
      @Valid @RequestBody AnswersTestRequestDTO request
  ) {
    log.info("Calculating convenience test alerts for document: {}", documentNumber);

    ConvenienceResult convenienceResult = convenienceAlertCalculationService.calculateAlerts(
        documentNumber,
        answersTestRequestMapper.toDomain(request)
    );

    AlertsTestResponseDTO response = convenienceResultMapper.toDto(convenienceResult);

    log.info("Convenience alerts calculated for document: {} - Result: {}",
        documentNumber, response.getResult());

    return ResponseEntity.ok(response);
  }
}