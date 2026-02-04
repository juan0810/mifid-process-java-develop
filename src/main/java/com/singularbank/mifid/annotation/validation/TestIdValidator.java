package com.singularbank.mifid.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TestIdValidator implements ConstraintValidator<EnsureTestId, Integer> {

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    return switch (value) {
      case null -> {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Test ID cannot be null"
        ).addConstraintViolation();
        yield false;
      }
      case Integer v when v > 0 -> true;
      case Integer v -> {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Test ID must be a positive number, but was %d".formatted(v)
        ).addConstraintViolation();
        yield false;
      }
    };
  }
}
