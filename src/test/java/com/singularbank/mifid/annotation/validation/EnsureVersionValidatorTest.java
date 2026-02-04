package com.singularbank.mifid.annotation.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("EnsureVersion Validator Tests")
class EnsureVersionValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
        .configure()
        .buildValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @ParameterizedTest
  @ValueSource(shorts = {1, 999})
  @DisplayName("Should pass validation when version is positive")
  void validate_whenVersionIsPositive_shouldPass(Short version) {
    TestDto dto = new TestDto(version);

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should pass validation when version is null")
  void validate_whenVersionIsNull_shouldPass() {
    TestDto dto = new TestDto(null);

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(shorts = {0, -1})
  @DisplayName("Should fail validation when version is not positive")
  void validate_whenVersionIsNotPositive_shouldFail(Short version) {
    TestDto dto = new TestDto(version);

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Version must be a positive number, but was " + version);
  }

  private record TestDto(@EnsureVersion Short version) {
  }
}
