package com.singularbank.mifid.controller.convenience;

import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
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
public class ConvenienceTestSaveAnswersController {

  private final AnswersTestRequestMapper mapper;

  @Tag(name = "Convenience Test", description = "Operations to manage convenience test answers")
  @Operation(
      summary = "Save convenience test answers",
      description = "Persists client answers for the convenience test",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "Convenience test answers saved successfully",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = TestResponseCreatedDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request (validation errors)",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Client identifier not found",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          )
      }
  )
  @PostMapping(value = "/convenience/{document-number}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TestResponseCreatedDTO> saveAnswersConvenience(
      @Parameter(
          description = "Client identity document number",
          required = true,
          example = "12345678A"
      )
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber,
      @Valid @RequestBody AnswersTestRequestDTO request
  ) {
    log.info("Processing convenience test save request");

    return ResponseEntity.status(HttpStatus.CREATED).body(null);
  }
}