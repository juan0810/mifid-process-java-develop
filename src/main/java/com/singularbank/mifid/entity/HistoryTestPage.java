package com.singularbank.mifid.entity;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryTestPage {

  private long totalElements;
  private int totalPages;
  private int currentPage;
  private int pageSize;
  private List<HistoryTestItem> tests;
}
