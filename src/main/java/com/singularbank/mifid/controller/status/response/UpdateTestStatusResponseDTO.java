package com.singularbank.mifid.controller.status.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response after updating test status")
public record UpdateTestStatusResponseDTO(
    @Schema(description = "Test ID", example = "18")
    Integer testId,

    @Schema(description = "Current status", example = "SIGNED")
    String status,

    @Schema(description = "Signature date")
    LocalDateTime signatureDate,

    @Schema(description = "Cancellation date")
    LocalDateTime cancellationDate
) {

}