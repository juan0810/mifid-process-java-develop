package com.singularbank.mifid.service.helpers;

import com.singularbank.mifid.entity.ProductFamily;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResultTestDescriptionBuilder {

  private static final String NO_FAMILIES_MESSAGE = "No hay familias convenientes";
  private static final String CONVENIENT_PREFIX = "Conveniente para ";

  public String buildConvenienceDescription(String familyCodes) {
    if (familyCodes == null || familyCodes.isBlank()) {
      return NO_FAMILIES_MESSAGE;
    }

    var families = parseConvenienceFamilies(familyCodes);

    if (families.isEmpty()) {
      return NO_FAMILIES_MESSAGE;
    }

    return CONVENIENT_PREFIX + families.stream()
        .map(ProductFamily::getDescription)
        .reduce((a, b) -> a + ", " + b)
        .orElse(NO_FAMILIES_MESSAGE);
  }

  public String buildSuitabilityDescription(String profile) {
    if (profile == null || profile.isBlank()) {
      return "Sin perfil asignado";
    }
    return "Perfil de riesgo: " + profile;
  }

  public List<ProductFamily> parseConvenienceFamilies(String convenienceResult) {
    if (convenienceResult == null || convenienceResult.isBlank()) {
      return List.of();
    }

    return Arrays.stream(convenienceResult.split(","))
        .map(String::trim)
        .filter(code -> !code.isEmpty())
        .map(ProductFamily::fromCode)
        .filter(Objects::nonNull)
        .toList();
  }
}
