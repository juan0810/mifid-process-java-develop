package com.singularbank.mifid.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryTestItem {

  private Integer id;
  private TypeTest type;
  private StateTest state;
  private String profile;
  private LocalDateTime createdAt;
  private LocalDateTime signedAt;
  private LocalDateTime expiresAt;
}