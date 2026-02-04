package com.singularbank.mifid.controller.helpers.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
  ONBOARDING("ONBOARDING"),
  ADVISE("ADVISE"),
  WEB("WEB");

  @JsonValue
  private final String value;
}