package com.singularbank.mifid.controller.customer;

import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.annotation.validation.EnsureTestId;
import com.singularbank.mifid.controller.customer.dto.CurrentCustomerTestsResponseDTO;
import com.singularbank.mifid.controller.customer.mapper.CustomerActiveTestsMapper;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.service.customer.CurrentCustomerTestsService;
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
public class CurrentCustomerTestsController {

  private final CurrentCustomerTestsService currentCustomerTestsService;
  private final CustomerActiveTestsMapper mapper;

  @Tag(
      name = "Current Customer Tests",
      description = "Operations to retrieve active MIFID tests for Advisory integration"
  )
  @Operation(
      summary = "Get active MIFID tests by customer identification",
      description = "Returns all active MIFID tests for a customer, including results, signature date and expiration date",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Active tests successfully retrieved",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = CurrentCustomerTestsResponseDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request (validation errors)",
              content =
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Customer not found in the system",
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

  @GetMapping(value = "/current-customer-tests/{document-number}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CurrentCustomerTestsResponseDTO> getCurrentCustomerTestsByIdentification(
      @Parameter(
          description = "Client identity document number",
          required = true,
          example = "12345678A"
      )
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber
  ) {
    log.info("Retrieving customer active tests for document: {}", documentNumber);

      CurrentCustomerTestsResponseDTO currentCustomerTestsResponseDTO = mapper.toDto(
        currentCustomerTestsService.getCurrentCustomerTestsByIdClient(documentNumber));

    return ResponseEntity.ok(currentCustomerTestsResponseDTO);
  }


    @Tag(
            name = "Current Customer Tests",
            description = "Operations to retrieve MIFID tests for Advisory integration"
    )
    @Operation(
            summary = "Get MIFID tests by id",
            description = "Returns all MIFID tests for a customer, including results, signature date and expiration date",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Tests successfully retrieved",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CurrentCustomerTestsResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request (validation errors)",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found in the system",
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
    @GetMapping(value = "/current-customer-tests/by-id/{testId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrentCustomerTestsResponseDTO> getCurrentCustomerTestsById(
            @PathVariable @Parameter(
                    description = "Test Id number",
                    required = true,
                    example = "280"
            )
            @EnsureTestId Integer testId
    ) {
        log.info("Retrieving customer tests for id: {}", testId);

        CurrentCustomerTestsResponseDTO currentCustomerTestsResponseDTO = mapper.toDto(
                currentCustomerTestsService.getCurrentCustomerTestsById(testId));

        return ResponseEntity.ok(currentCustomerTestsResponseDTO);
    }
}