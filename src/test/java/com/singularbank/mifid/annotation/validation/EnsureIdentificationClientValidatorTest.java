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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("EnsureIdentificationClient Validator Tests")
class EnsureIdentificationClientValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    try (ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
        .configure()
        .buildValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  @DisplayName("Should pass validation when document number has 9 characters")
  void validate_whenDocumentNumberHas9Characters_shouldPass() {
    TestDto dto = new TestDto("12345678A");

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "1234567A", "123456789A"})
  @DisplayName("Should fail validation when document number is invalid")
  void validate_whenDocumentNumberIsInvalid_shouldFail(String documentNumber) {
    TestDto dto = new TestDto(documentNumber);

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).hasSize(1);
  }

  @Test
  @DisplayName("Should pass validation when document number has exactly 9 characters with special chars")
  void validate_whenDocumentNumberHas9CharactersWithSpecialChars_shouldPass() {
    TestDto dto = new TestDto("X1234567L");

    Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  private record TestDto(@EnsureIdentificationClient String documentNumber) {
  }
}
