package com.singularbank.mifid.controller.sustainability;

import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.controller.helpers.mapper.AnswersTestRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class SustainabilityTestSaveAnswersController {

  private final AnswersTestRequestMapper mapper;

  @Tag(name = "Sustainability Test", description = "Operations to manage sustainability test answers")
  @Operation(
      summary = "Save sustainability test answers",
      description = "Persists client answers for the sustainability test",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "Sustainability test answers saved successfully",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = TestResponseCreatedDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request (validation errors)"
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Client identifier not found in Salesforce"
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error"
          )
      }
  )
  @PostMapping(value = "/sustainability/{document-number}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TestResponseCreatedDTO> saveAnswersSustainability(
      @Parameter(
          description = "Client identity document number",
          required = true,
          example = "12345678A"
      )
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber,
      @Valid @RequestBody AnswersTestRequestDTO request
  ) {
    log.info("Processing sustainability test save request");

    return ResponseEntity.status(HttpStatus.CREATED).body(null);
  }
}