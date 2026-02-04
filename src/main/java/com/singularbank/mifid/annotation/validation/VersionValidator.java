package com.singularbank.mifid.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VersionValidator implements ConstraintValidator<EnsureVersion, Short> {

  @Override
  public boolean isValid(Short value, ConstraintValidatorContext context) {
    return switch (value) {
      case null -> true;
      case Short v when v > 0 -> true;
      case Short v -> {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Version must be a positive number, but was %d".formatted(v)
        ).addConstraintViolation();
        yield false;
      }
    };
  }
}