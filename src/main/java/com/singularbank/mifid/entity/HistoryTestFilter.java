package com.singularbank.mifid.entity;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryTestFilter {
  private TypeTest type;
  private StateTest state;
  private LocalDate from;
  private LocalDate to;

  @Builder.Default
  private int page = 0;

  @Builder.Default
  private int size = 20;
}