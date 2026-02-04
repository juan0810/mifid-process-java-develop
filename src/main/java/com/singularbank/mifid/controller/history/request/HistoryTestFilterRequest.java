package com.singularbank.mifid.controller.history.request;

import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import java.time.LocalDate;

public record HistoryTestFilterRequest(
    TypeTest type,
    StateTest state,
    LocalDate from,
    LocalDate to,
    int page,
    int size
) {

  public static HistoryTestFilterRequest of(
      TypeTest type, StateTest state,
      LocalDate from, LocalDate to,
      Integer page, Integer size) {
    return new HistoryTestFilterRequest(
        type, state, from, to,
        page != null ? page : 0,
        size != null ? size : 20
    );
  }
}
