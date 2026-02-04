package com.singularbank.mifid.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StatusTestResult {

  Integer testId;
  StateTest status;
  LocalDateTime signatureDate;
  LocalDateTime cancellationDate;
  LocalDate caducityDate;
}