package com.singularbank.mifid.controller.suitability;

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
public class SuitabilityTestSaveAnswersController {

  private final AnswersTestRequestMapper mapper;

  @Tag(name = "Suitability Test", description = "Operations to manage suitability test answers")
  @Operation(
      summary = "Save suitability test answers",
      description = "Persists client answers for the suitability test",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "Suitability test answers saved successfully",
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
  @PostMapping(value = "/suitability/{document-number}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TestResponseCreatedDTO> saveAnswersSuitability(
      @Parameter(
          description = "Client identity document number",
          required = true,
          example = "12345678A"
      )
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber,
      @Valid @RequestBody AnswersTestRequestDTO request
  ) {
    log.info("Processing suitability test save request");

    return ResponseEntity.status(HttpStatus.CREATED).body(null);
  }
}