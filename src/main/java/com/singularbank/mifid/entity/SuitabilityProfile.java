package com.singularbank.mifid.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuitabilityProfile {

  UNDEFINED(0, "No definido"),
  CONSERVATIVE(1, "Conservador"),
  MODERATE(2, "Moderado"),
  FLEXIBLE(3, "Flexible"),
  DETERMINED(4, "Decidido"),
  AGGRESSIVE(5, "Agresivo");

  private final int score;
  private final String displayName;

  public static SuitabilityProfile fromScore(int score) {
    return switch (score) {
      case 1 -> CONSERVATIVE;
      case 2 -> MODERATE;
      case 3 -> FLEXIBLE;
      case 4 -> DETERMINED;
      case 5 -> AGGRESSIVE;
      default -> UNDEFINED;
    };
  }

  public static SuitabilityProfile fromDisplayName(String displayName) {
    if (displayName == null) {
      return UNDEFINED;
    }
    for (SuitabilityProfile profile : values()) {
      if (profile.displayName.equalsIgnoreCase(displayName)) {
        return profile;
      }
    }
    return UNDEFINED;
  }

  @Override
  public String toString() {
    return displayName;
  }
}