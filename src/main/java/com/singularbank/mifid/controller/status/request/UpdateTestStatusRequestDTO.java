package com.singularbank.mifid.controller.status.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update test status")
public record UpdateTestStatusRequestDTO(
    @Schema(description = "New status", example = "SIGNED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    StateTestRequest status
) {}