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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("EnsureTestId Validator Tests")
class EnsureTestIdValidatorTest {

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
  @ValueSource(ints = {1, 999999})
  @DisplayName("Should pass validation when testId is positive")
  void validate_whenTestIdIsPositive_shouldPass(Integer testId) {
    TestDto dto = new TestDto(testId);

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  @DisplayName("Should fail validation when testId is not positive")
  void validate_whenTestIdIsNotPositive_shouldFail(Integer testId) {
    TestDto dto = new TestDto(testId);

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Test ID must be a positive number, but was " + testId);
  }

  private record TestDto(@EnsureTestId Integer testId) {

  }
}
