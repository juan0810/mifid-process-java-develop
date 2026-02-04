package com.singularbank.mifid.controller.history;

import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.controller.history.mapper.HistoryTestResponseMapper;
import com.singularbank.mifid.controller.history.response.HistoryTestResponseDTO;
import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.history.HistoryTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
@Tag(name = "Test History", description = "MiFID test history operations")
public class HistoryTestController {

  private final HistoryTestService historyTestService;
  private final HistoryTestResponseMapper mapper;

  @Operation(
      summary = "Get MiFID test history",
      description = "Returns paginated history of MiFID tests for a customer",
      responses = {
          @ApiResponse(responseCode = "200", description = "Success",
              content = @Content(schema = @Schema(implementation = HistoryTestResponseDTO.class))),
          @ApiResponse(responseCode = "400", description = "Invalid request",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(responseCode = "404", description = "Customer not found",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      }
  )
  @GetMapping(value = "/{document-number}/tests", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HistoryTestResponseDTO> getTestHistory(
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber,
      @RequestParam(required = false) TypeTest type,
      @RequestParam(required = false) StateTest state,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info("Getting test history for: {}", documentNumber);

    var filter = HistoryTestFilter.builder()
        .type(type)
        .state(state)
        .from(from)
        .to(to)
        .page(page)
        .size(size)
        .build();

    return ResponseEntity.ok(
        mapper.toResponse(historyTestService.getTestHistory(documentNumber, filter)));
  }
}

