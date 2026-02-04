package com.singularbank.mifid.controller.history.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Paginated MiFID test history response")
public record HistoryTestResponseDTO(
    @Schema(description = "Total records", example = "45")
    long totalElements,

    @Schema(description = "Total pages", example = "15")
    int totalPages,

    @Schema(description = "Current page", example = "0")
    int currentPage,

    @Schema(description = "Records per page", example = "20")
    int pagination,

    @Schema(description = "Test list")
    List<TestItemDTO> tests
) {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record TestItemDTO(
      @Schema(description = "Test ID", example = "123")
      Integer id,

      @Schema(description = "Test type", example = "SUITABILITY")
      String type,

      @Schema(description = "Test state", example = "FIRMADO")
      String state,

      @Schema(description = "Profile (only SUITABILITY)", example = "MODERADO")
      String profile,

      @Schema(description = "Creation date")
      LocalDateTime createdAt,

      @Schema(description = "Signature date")
      LocalDateTime signedAt,

      @Schema(description = "Expiration date")
      LocalDateTime expiresAt
  ) {}
}
