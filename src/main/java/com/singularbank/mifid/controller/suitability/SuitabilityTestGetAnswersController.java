package com.singularbank.mifid.controller.suitability;

import com.singularbank.mifid.annotation.validation.EnsureTestId;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestResponseDTO;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.controller.helpers.mapper.AnswersTestResponseMapper;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.helpers.GetAnswersTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test-mifid")
@Validated
@Slf4j
public class SuitabilityTestGetAnswersController {

  private final GetAnswersTestService getAnswersTestService;
  private final AnswersTestResponseMapper mapper;

  @Tag(name = "Suitability Test", description = "Operations to manage suitability test answers")
  @Operation(
      summary = "Get suitability test answers",
      description = "Retrieves the complete set of client answers for the suitability test",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Suitability test answers retrieved successfully",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = AnswersTestResponseDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request",
              content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Test not found",
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
  @GetMapping(value = "/suitability-responses/{test-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AnswersTestResponseDTO> getSuitabilityAnswers(
      @Parameter(
          description = "Test evaluation reference identifier",
          example = "1",
          required = true
      )
      @PathVariable("test-id") @EnsureTestId Integer testId
  ) {
    log.info("Suitability test answers retrieved (testId: {})", testId);

    var response = getAnswersTestService.get(testId, TypeTest.SUITABILITY);

    return ResponseEntity.ok(mapper.toDto(response));
  }
}