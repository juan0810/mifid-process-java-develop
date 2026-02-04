package com.singularbank.mifid.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StateTest {
  DRAFT(0),
  PENDING(1),
  SIGNED(2),
  CANCELLED(3),
  EXPIRED(4);

  private final int code;

  public static StateTest fromCode(int code) {
    for (StateTest state : values()) {
      if (state.code == code) {
        return state;
      }
    }
    return DRAFT;
  }
}
