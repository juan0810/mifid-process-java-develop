package com.singularbank.mifid.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTestInfo {
  private Integer testId;
  private String customerIdentity;
  private String convenienceResult;
  private String suitabilityResult;
  private LocalDateTime creationDate;
  private LocalDateTime signatureDate;
  private LocalDateTime modificationDate;
  private LocalDate expirationDate;
  private Short versionId;
  private String versionDescription;
  private LocalDate versionExpirationDate;
  private Boolean versionActive;
  private String serviceName;
  private String serviceDescription;
  private TypeTest typeTest;
}
