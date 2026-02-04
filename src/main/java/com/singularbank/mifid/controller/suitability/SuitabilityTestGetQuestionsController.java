package com.singularbank.mifid.controller.suitability;

import com.singularbank.mifid.annotation.validation.EnsureVersion;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.controller.helpers.dto.QuestionsTest;
import com.singularbank.mifid.controller.helpers.enums.ApplicationType;
import com.singularbank.mifid.controller.helpers.mapper.QuestionsTestMapper;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.helpers.GetTestQuestionsService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test-mifid")
@Validated
@Slf4j
public class SuitabilityTestGetQuestionsController {

  private final GetTestQuestionsService getTestQuestionsService;
  private final QuestionsTestMapper mapper;

  @Tag(name = "Suitability Test", description = "Operations to manage suitability test questions")
  @Operation(
      summary = "Get suitability test questions",
      description = "Retrieves the suitability test questions organized by product families for a specific application",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Suitability test retrieved successfully",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = QuestionsTest.class)
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
  @GetMapping(value = "/suitability/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<QuestionsTest> getSuitabilityTest(
      @Parameter(
          description = "Application owner of the test version. Supported values: ONBOARDING, ADVISE, WEB (case-insensitive)",
          required = true,
          example = "ONBOARDING",
          schema = @Schema(
              type = "string",
              allowableValues = {"ONBOARDING", "ADVISE", "WEB"}
          )
      )
      @PathVariable("application")
      ApplicationType application,
      @Parameter(
          name = "version",
          description = "Version number of the test. Must be a positive number. If not provided, the latest version will be used",
          example = "1",
          schema = @Schema(type = "Short", minimum = "1")
      )
      @RequestParam(required = false)
      @EnsureVersion
      Short version) {

    log.info("Retrieving suitability test (application={}, version={})",
        application.getValue(), version != null ? version : "latest");

    var response = getTestQuestionsService
        .getTest(application.getValue(), TypeTest.SUITABILITY, version);

    return ResponseEntity.ok(mapper.toDto(response));
  }
}