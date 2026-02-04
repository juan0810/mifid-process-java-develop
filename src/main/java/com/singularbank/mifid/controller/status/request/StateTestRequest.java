package com.singularbank.mifid.controller.status.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Allowed status values for manual transitions",
    enumAsRef = true
)
public enum StateTestRequest {

  @Schema(description = "Test in draft state")
  DRAFT,

  @Schema(description = "Test pending signature")
  PENDING,

  @Schema(description = "Test signed by customer")
  SIGNED,

  @Schema(description = "Test cancelled")
  CANCELLED
}