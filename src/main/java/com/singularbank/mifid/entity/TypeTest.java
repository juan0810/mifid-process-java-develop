package com.singularbank.mifid.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TypeTest {

  CONVENIENCE("CO"),
  SUITABILITY("ID"),
  SUSTAINABILITY("SO");

  private final String code;

  @JsonValue
  public String toJson() {
    return this.name();
  }

  public static TypeTest fromCode(String code) {
    return switch (code) {
      case "CO" -> CONVENIENCE;
      case "ID" -> SUITABILITY;
      case "SO" -> SUSTAINABILITY;
      case null -> throw new IllegalArgumentException("Test type code cannot be null");
      default -> throw new IllegalArgumentException("Invalid test type code: " + code);
    };
  }
}