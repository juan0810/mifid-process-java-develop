package com.singularbank.mifid.controller.status;

import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.controller.status.mapper.StateTestMapper;
import com.singularbank.mifid.controller.status.request.UpdateTestStatusRequestDTO;
import com.singularbank.mifid.controller.status.response.UpdateTestStatusResponseDTO;
import com.singularbank.mifid.service.status.StatusTestService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test-mifid")
@Validated
@Slf4j
@Tag(name = "MiFID Test Status", description = "MiFID test status operations")
public class StatusTestController {

  private final StatusTestService statusTestService;
  private final StateTestMapper mapper;

  @Operation(
      summary = "Update MiFID test status",
      description = "Changes the status of a MiFID test response",
      responses = {
          @ApiResponse(responseCode = "200", description = "Status updated successfully",
              content = @Content(schema = @Schema(implementation = UpdateTestStatusResponseDTO.class))),
          @ApiResponse(responseCode = "400", description = "Invalid transition",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(responseCode = "404", description = "Test not found",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      }
  )
  @PutMapping(
      value = "/responses/{test-id}/status",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<UpdateTestStatusResponseDTO> updateTestStatus(
      @PathVariable("test-id") Integer testId,
      @Valid @RequestBody UpdateTestStatusRequestDTO request
  ) {
    log.info("Updating status for test {} to {}", testId, request.status());
    var result = statusTestService.updateStatus(testId, mapper.toDomain(request.status()));
    return ResponseEntity.ok(mapper.toResponse(result));
  }
}