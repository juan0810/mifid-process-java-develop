package com.singularbank.mifid.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdentificationClientValidator implements
    ConstraintValidator<EnsureIdentificationClient, String> {

  private static final int REQUIRED_LENGTH = 9;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value != null && value.length() == REQUIRED_LENGTH;
  }
}
