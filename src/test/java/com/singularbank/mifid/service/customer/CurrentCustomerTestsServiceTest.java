package com.singularbank.mifid.service.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.CompleteTestInfo;
import com.singularbank.mifid.entity.CustomerActiveTests.SustainabilityPreferences;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.customer.impl.CurrentCustomerTestsServiceImpl;
import com.singularbank.mifid.service.sustainability.SustainabilityCalculatorService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Current Customer Tests Service Tests")
class CurrentCustomerTestsServiceTest {

  @Mock
  private RespuestaClienteRepository respuestaClienteRepository;

  @Mock
  private RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;

  @Mock
  private SustainabilityCalculatorService sustainabilityCalculator;  // ← Clase concreta mockeada

  @InjectMocks
  private CurrentCustomerTestsServiceImpl service;

  private static final String CUSTOMER_ID = "12345678A";
  private static final Short VERSION_ID = 1;
  private static final String SERVICE_NAME = "ONBOARDING";
  private static final LocalDateTime CREATION_DATE = LocalDateTime.of(2025, 10, 16, 10, 0);
  private static final LocalDateTime SIGNATURE_DATE = LocalDateTime.of(2025, 10, 16, 10, 30);

  @Nested
  class CurrentCustomerTestsByIdClient {
    @Nested
    @DisplayName("Edge Cases - Invalid Input")
    class EdgeCases {

      @Test
      @DisplayName("Should throw NullPointerException when customerIdentity is null")
      void nullCustomerIdentity() {
        assertThatThrownBy(() -> service.getCurrentCustomerTestsByIdClient(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("customerIdentity is required");

        verifyNoInteractions(respuestaClienteRepository);
        verifyNoInteractions(respuestaClienteDetalleRepository);
        verifyNoInteractions(sustainabilityCalculator);
      }

      @Test
      @DisplayName("Should return empty list when no active tests found")
      void noActiveTests() {
        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of());

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerIdentity()).isEqualTo(CUSTOMER_ID);
        assertThat(result.getActiveTests()).isEmpty();

        verify(respuestaClienteRepository).findCurrentActiveTestsByIdentity(CUSTOMER_ID);
        verify(respuestaClienteDetalleRepository, never()).findAnswersByIdentityAndTestType(any(),
                any());
      }
    }

    @Nested
    @DisplayName("Convenience Test Scenarios")
    class ConvenienceTestScenarios {

      @ParameterizedTest
      @CsvSource(delimiter = '|', value = {
              "A|1",
              "A,B|2",
              "A,B,C|3",
              "A,B,C,D,E,F,G,H,I,J,K|11"
      })
      @DisplayName("Should return convenience test with families")
      void withFamilies(String familyCodes, int expectedSize) {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, familyCodes, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests()).hasSize(1);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestType()).isEqualTo(TypeTest.CONVENIENCE);
        assertThat(test.getTestResult()).isEqualTo(familyCodes);
        assertThat(test.getTestResultDescription()).startsWith("Conveniente para");
        assertThat(test.getFamilies()).hasSize(expectedSize);
      }

      @Test
      @DisplayName("Should return convenience test as pending when result is null")
      void pendingWhenNull() {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, null, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEqualTo("Pendiente");
        assertThat(test.getTestResultDescription()).isNull();
        assertThat(test.getFamilies()).isNull();
      }

      @ParameterizedTest
      @ValueSource(strings = {"", "  ", "\t", "\n"})
      @DisplayName("Should return empty families when result is blank")
      void emptyFamiliesWhenBlank(String result) {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, result, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var response = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        var test = response.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEmpty();
        assertThat(test.getTestResultDescription()).isEqualTo("No hay familias convenientes");
        assertThat(test.getFamilies()).isEmpty();
      }

      @ParameterizedTest
      @CsvSource(delimiter = '|', value = {
              " A , B , C |3",
              "A,Z,B,X,C|3",
              "A,,B,,C|3"
      })
      @DisplayName("Should handle edge cases in family parsing")
      void familyParsingEdgeCases(String input, int expectedSize) {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, input, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests().getFirst().getFamilies())
                .hasSize(expectedSize)
                .allMatch(family -> family.name().matches("[A-K]"));
      }
    }

    @Nested
    @DisplayName("Suitability Test Scenarios")
    class SuitabilityTestScenarios {

      @ParameterizedTest
      @ValueSource(strings = {"Conservador", "Moderado", "Agresivo", "Muy Agresivo"})
      @DisplayName("Should return suitability test with valid results")
      void withValidResults(String suitabilityResult) {
        var testInfo = createTestInfo(TypeTest.SUITABILITY, null, suitabilityResult);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestType()).isEqualTo(TypeTest.SUITABILITY);
        assertThat(test.getTestResult()).isEqualTo(suitabilityResult);
        assertThat(test.getFamilies()).isNull();
        assertThat(test.getSustainabilityPreferences()).isNull();
      }

      @ParameterizedTest
      @ValueSource(strings = {"", "  ", "\t"})
      @DisplayName("Should return suitability as pending when result is blank")
      void pendingWhenBlank(String suitabilityResult) {
        var testInfo = createTestInfo(TypeTest.SUITABILITY, null, suitabilityResult);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests().getFirst().getTestResult()).isEqualTo("Pendiente");
      }

      @Test
      @DisplayName("Should return suitability as pending when result is null")
      void pendingWhenNull() {
        var testInfo = createTestInfo(TypeTest.SUITABILITY, null, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests().getFirst().getTestResult()).isEqualTo("Pendiente");
      }
    }

    @Nested
    @DisplayName("Sustainability Test Scenarios")
    class SustainabilityTestScenarios {

      @Test
      @DisplayName("Should return sustainability test with calculated result and preferences")
      void withCalculatedResult() {
        var testInfo = createTestInfo(TypeTest.SUSTAINABILITY, null, null);
        var mockAnswers = createMockAnswersTest();
        var expectedResult = "El cliente ha manifestado su deseo de integrar un 10%...";
        var expectedPrefs = createMockSustainabilityPreferences();

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));
        when(respuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(
                CUSTOMER_ID, TypeTest.SUSTAINABILITY))
                .thenReturn(mockAnswers);
        when(sustainabilityCalculator.calculateSustainabilityResult(mockAnswers))
                .thenReturn(expectedResult);
        when(sustainabilityCalculator.extractSustainabilityPreferences(mockAnswers))
                .thenReturn(expectedPrefs);

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestType()).isEqualTo(TypeTest.SUSTAINABILITY);
        assertThat(test.getTestResult()).isEqualTo(expectedResult);
        assertThat(test.getSustainabilityPreferences()).isNotNull();
        assertThat(test.getSustainabilityPreferences().getPercentInPortfolio()).isEqualTo(10);
        assertThat(test.getFamilies()).isNull();

        verify(respuestaClienteDetalleRepository).findAnswersByIdentityAndTestType(
                CUSTOMER_ID, TypeTest.SUSTAINABILITY);
        verify(sustainabilityCalculator).calculateSustainabilityResult(mockAnswers);
        verify(sustainabilityCalculator).extractSustainabilityPreferences(mockAnswers);
      }

      @Test
      @DisplayName("Should return no data when sustainability answers not found")
      void noDataWhenAnswersNotFound() {
        var testInfo = createTestInfo(TypeTest.SUSTAINABILITY, null, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));
        when(respuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(
                CUSTOMER_ID, TypeTest.SUSTAINABILITY))
                .thenThrow(new ResourceNotFoundException("No answers found"));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEqualTo("Sin resultado disponible");
        assertThat(test.getSustainabilityPreferences()).isNull();

        verify(sustainabilityCalculator, never()).calculateSustainabilityResult(any());
        verify(sustainabilityCalculator, never()).extractSustainabilityPreferences(any());
      }

      @Test
      @DisplayName("Should return no data when sustainability calculation fails")
      void noDataWhenCalculationFails() {
        var testInfo = createTestInfo(TypeTest.SUSTAINABILITY, null, null);
        var mockAnswers = createMockAnswersTest();

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));
        when(respuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(
                CUSTOMER_ID, TypeTest.SUSTAINABILITY))
                .thenReturn(mockAnswers);
        when(sustainabilityCalculator.calculateSustainabilityResult(mockAnswers))
                .thenThrow(new RuntimeException("Calculation error"));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEqualTo("Sin resultado disponible");
        assertThat(test.getSustainabilityPreferences()).isNull();
      }
    }

    @Nested
    @DisplayName("Multiple Tests Scenarios")
    class MultipleTestsScenarios {

      @Test
      @DisplayName("Should return multiple tests of different types")
      void multipleTestsOfDifferentTypes() {
        var convenienceTest = createTestInfo(TypeTest.CONVENIENCE, "A,B", null);
        var suitabilityTest = createTestInfo(TypeTest.SUITABILITY, null, "Moderado");
        var sustainabilityTest = createTestInfo(TypeTest.SUSTAINABILITY, null, null);

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(convenienceTest, suitabilityTest, sustainabilityTest));
        when(respuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(
                CUSTOMER_ID, TypeTest.SUSTAINABILITY))
                .thenReturn(null);

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests()).hasSize(3);
        assertThat(result.getActiveTests())
                .extracting("testType")
                .containsExactly(TypeTest.CONVENIENCE, TypeTest.SUITABILITY, TypeTest.SUSTAINABILITY);
      }
    }

    @Nested
    @DisplayName("Expiration Date Calculation")
    class ExpirationDateCalculation {

      @ParameterizedTest
      @CsvSource({
              "CONVENIENCE, 2026-10-16",
              "SUITABILITY, 2026-10-16",
              "SUSTAINABILITY, 2027-10-16"
      })
      @DisplayName("Should calculate expiration date based on test type")
      void calculateBasedOnTestType(TypeTest typeTest, LocalDate expectedExpiration) {
        var testInfo = CompleteTestInfo.builder()
                .testId(1)
                .versionId(VERSION_ID)
                .serviceName(SERVICE_NAME)
                .typeTest(typeTest)
                .convenienceResult(typeTest == TypeTest.CONVENIENCE ? "A" : null)
                .suitabilityResult(typeTest == TypeTest.SUITABILITY ? "Conservador" : null)
                .creationDate(CREATION_DATE)
                .signatureDate(SIGNATURE_DATE)
                .expirationDate(expectedExpiration)
                .build();

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        if (typeTest == TypeTest.SUSTAINABILITY) {
          when(respuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(
                  CUSTOMER_ID, TypeTest.SUSTAINABILITY))
                  .thenReturn(null);
        }

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests().getFirst().getExpirationDate())
                .isEqualTo(expectedExpiration);
      }

      @Test
      @DisplayName("Should use version expiration date when available")
      void useVersionExpirationDate() {
        var expirationDate = LocalDate.of(2026, 12, 31);
        var testInfo = CompleteTestInfo.builder()
                .testId(1)
                .versionId(VERSION_ID)
                .serviceName(SERVICE_NAME)
                .typeTest(TypeTest.CONVENIENCE)
                .convenienceResult("A")
                .creationDate(CREATION_DATE)
                .signatureDate(SIGNATURE_DATE)
                .expirationDate(expirationDate)
                .build();

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests().getFirst().getExpirationDate())
                .isEqualTo(expirationDate);
      }

      @Test
      @DisplayName("Should return null expiration date when signature date is null")
      void nullExpirationWhenSignatureDateIsNull() {
        var testInfo = CompleteTestInfo.builder()
                .testId(1)
                .versionId(VERSION_ID)
                .serviceName(SERVICE_NAME)
                .typeTest(TypeTest.CONVENIENCE)
                .convenienceResult("A")
                .creationDate(CREATION_DATE)
                .signatureDate(null)
                .versionExpirationDate(null)
                .build();

        when(respuestaClienteRepository.findCurrentActiveTestsByIdentity(CUSTOMER_ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsByIdClient(CUSTOMER_ID);

        assertThat(result.getActiveTests().getFirst().getExpirationDate()).isNull();
      }
    }
  }

  @Nested
  class CurrentCustomerTestsById {
    private static final Integer ID = 123;

    @Nested
    @DisplayName("Edge Cases - Invalid Input")
    class EdgeCases {

      @Test
      @DisplayName("Should throw NullPointerException when id is null")
      void nullCustomerId() {
        assertThatThrownBy(() -> service.getCurrentCustomerTestsById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Id is required");

        verifyNoInteractions(respuestaClienteRepository);
        verifyNoInteractions(respuestaClienteDetalleRepository);
        verifyNoInteractions(sustainabilityCalculator);
      }

      @Test
      @DisplayName("Should return empty list when no active tests found")
      void noActiveTests() {
        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of());

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerIdentity()).isEqualTo("");
        assertThat(result.getActiveTests()).isEmpty();

        verify(respuestaClienteRepository).findCurrentTestsById(ID);
      }
    }

    @Nested
    @DisplayName("Convenience Test Scenarios")
    class ConvenienceTestScenarios {

      @ParameterizedTest
      @CsvSource(delimiter = '|', value = {
              "A|1",
              "A,B|2",
              "A,B,C|3",
              "A,B,C,D,E,F,G,H,I,J,K|11"
      })
      @DisplayName("Should return convenience test with families")
      void withFamilies(String familyCodes, int expectedSize) {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, familyCodes, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests()).hasSize(1);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestType()).isEqualTo(TypeTest.CONVENIENCE);
        assertThat(test.getTestResult()).isEqualTo(familyCodes);
        assertThat(test.getTestResultDescription()).startsWith("Conveniente para");
        assertThat(test.getFamilies()).hasSize(expectedSize);
      }

      @Test
      @DisplayName("Should return convenience test as pending when result is null")
      void pendingWhenNull() {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, null, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEqualTo("Pendiente");
        assertThat(test.getTestResultDescription()).isNull();
        assertThat(test.getFamilies()).isNull();
      }

      @ParameterizedTest
      @ValueSource(strings = {"", "  ", "\t", "\n"})
      @DisplayName("Should return empty families when result is blank")
      void emptyFamiliesWhenBlank(String result) {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, result, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var response = service.getCurrentCustomerTestsById(ID);

        var test = response.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEmpty();
        assertThat(test.getTestResultDescription()).isEqualTo("No hay familias convenientes");
        assertThat(test.getFamilies()).isEmpty();
      }

      @ParameterizedTest
      @CsvSource(delimiter = '|', value = {
              " A , B , C |3",
              "A,Z,B,X,C|3",
              "A,,B,,C|3"
      })
      @DisplayName("Should handle edge cases in family parsing")
      void familyParsingEdgeCases(String input, int expectedSize) {
        var testInfo = createTestInfo(TypeTest.CONVENIENCE, input, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests().getFirst().getFamilies())
                .hasSize(expectedSize)
                .allMatch(family -> family.name().matches("[A-K]"));
      }
    }

    @Nested
    @DisplayName("Suitability Test Scenarios")
    class SuitabilityTestScenarios {

      @ParameterizedTest
      @ValueSource(strings = {"Conservador", "Moderado", "Agresivo", "Muy Agresivo"})
      @DisplayName("Should return suitability test with valid results")
      void withValidResults(String suitabilityResult) {
        var testInfo = createTestInfo(TypeTest.SUITABILITY, null, suitabilityResult);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestType()).isEqualTo(TypeTest.SUITABILITY);
        assertThat(test.getTestResult()).isEqualTo(suitabilityResult);
        assertThat(test.getFamilies()).isNull();
        assertThat(test.getSustainabilityPreferences()).isNull();
      }

      @ParameterizedTest
      @ValueSource(strings = {"", "  ", "\t"})
      @DisplayName("Should return suitability as pending when result is blank")
      void pendingWhenBlank(String suitabilityResult) {
        var testInfo = createTestInfo(TypeTest.SUITABILITY, null, suitabilityResult);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests().getFirst().getTestResult()).isEqualTo("Pendiente");
      }

      @Test
      @DisplayName("Should return suitability as pending when result is null")
      void pendingWhenNull() {
        var testInfo = createTestInfo(TypeTest.SUITABILITY, null, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests().getFirst().getTestResult()).isEqualTo("Pendiente");
      }
    }

    @Nested
    @DisplayName("Sustainability Test Scenarios")
    class SustainabilityTestScenarios {

      @Test
      @DisplayName("Should return sustainability test with calculated result and preferences")
      void withCalculatedResult() {
        var testInfo = createTestInfo(TypeTest.SUSTAINABILITY, null, null);
        var mockAnswers = createMockAnswersTest();
        var expectedResult = "El cliente ha manifestado su deseo de integrar un 10%...";
        var expectedPrefs = createMockSustainabilityPreferences();

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));
        when(respuestaClienteDetalleRepository.findAnswersByTestId(ID, TypeTest.SUSTAINABILITY))
                .thenReturn(mockAnswers);
        when(sustainabilityCalculator.calculateSustainabilityResult(mockAnswers))
                .thenReturn(expectedResult);
        when(sustainabilityCalculator.extractSustainabilityPreferences(mockAnswers))
                .thenReturn(expectedPrefs);

        var result = service.getCurrentCustomerTestsById(ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestType()).isEqualTo(TypeTest.SUSTAINABILITY);
        assertThat(test.getTestResult()).isEqualTo(expectedResult);
        assertThat(test.getSustainabilityPreferences()).isNotNull();
        assertThat(test.getSustainabilityPreferences().getPercentInPortfolio()).isEqualTo(10);
        assertThat(test.getFamilies()).isNull();

        verify(respuestaClienteDetalleRepository).findAnswersByTestId(ID, TypeTest.SUSTAINABILITY);
        verify(sustainabilityCalculator).calculateSustainabilityResult(mockAnswers);
        verify(sustainabilityCalculator).extractSustainabilityPreferences(mockAnswers);
      }

      @Test
      @DisplayName("Should return no data when sustainability answers not found")
      void noDataWhenAnswersNotFound() {
        var testInfo = createTestInfo(TypeTest.SUSTAINABILITY, null, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));
        when(respuestaClienteDetalleRepository.findAnswersByTestId(ID, TypeTest.SUSTAINABILITY))
                .thenThrow(new ResourceNotFoundException("No answers found"));

        var result = service.getCurrentCustomerTestsById(ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEqualTo("Sin resultado disponible");
        assertThat(test.getSustainabilityPreferences()).isNull();

        verify(sustainabilityCalculator, never()).calculateSustainabilityResult(any());
        verify(sustainabilityCalculator, never()).extractSustainabilityPreferences(any());
      }

      @Test
      @DisplayName("Should return no data when sustainability calculation fails")
      void noDataWhenCalculationFails() {
        var testInfo = createTestInfo(TypeTest.SUSTAINABILITY, null, null);
        var mockAnswers = createMockAnswersTest();

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));
        when(respuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(
                CUSTOMER_ID, TypeTest.SUSTAINABILITY))
                .thenReturn(mockAnswers);
        when(sustainabilityCalculator.calculateSustainabilityResult(mockAnswers))
                .thenThrow(new RuntimeException("Calculation error"));

        var result = service.getCurrentCustomerTestsById(ID);

        var test = result.getActiveTests().getFirst();
        assertThat(test.getTestResult()).isEqualTo("Sin resultado disponible");
        assertThat(test.getSustainabilityPreferences()).isNull();
      }
    }

    @Nested
    @DisplayName("Multiple Tests Scenarios")
    class MultipleTestsScenarios {

      @Test
      @DisplayName("Should return multiple tests of different types")
      void multipleTestsOfDifferentTypes() {
        var convenienceTest = createTestInfo(TypeTest.CONVENIENCE, "A,B", null);
        var suitabilityTest = createTestInfo(TypeTest.SUITABILITY, null, "Moderado");
        var sustainabilityTest = createTestInfo(TypeTest.SUSTAINABILITY, null, null);

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(convenienceTest, suitabilityTest, sustainabilityTest));
        when(respuestaClienteDetalleRepository.findAnswersByTestId(ID, TypeTest.SUSTAINABILITY))
                .thenReturn(null);

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests()).hasSize(3);
        assertThat(result.getActiveTests())
                .extracting("testType")
                .containsExactly(TypeTest.CONVENIENCE, TypeTest.SUITABILITY, TypeTest.SUSTAINABILITY);
      }
    }

    @Nested
    @DisplayName("Expiration Date Calculation")
    class ExpirationDateCalculation {

      @ParameterizedTest
      @CsvSource({
              "CONVENIENCE, 2026-10-16",
              "SUITABILITY, 2026-10-16",
              "SUSTAINABILITY, 2027-10-16"
      })
      @DisplayName("Should calculate expiration date based on test type")
      void calculateBasedOnTestType(TypeTest typeTest, LocalDate expectedExpiration) {
        var testInfo = CompleteTestInfo.builder()
                .testId(1)
                .versionId(VERSION_ID)
                .serviceName(SERVICE_NAME)
                .typeTest(typeTest)
                .convenienceResult(typeTest == TypeTest.CONVENIENCE ? "A" : null)
                .suitabilityResult(typeTest == TypeTest.SUITABILITY ? "Conservador" : null)
                .creationDate(CREATION_DATE)
                .signatureDate(SIGNATURE_DATE)
                .expirationDate(expectedExpiration)
                .build();

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        if (typeTest == TypeTest.SUSTAINABILITY) {
          when(respuestaClienteDetalleRepository.findAnswersByTestId(ID, TypeTest.SUSTAINABILITY))
                  .thenReturn(null);
        }

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests().getFirst().getExpirationDate())
                .isEqualTo(expectedExpiration);
      }

      @Test
      @DisplayName("Should use version expiration date when available")
      void useVersionExpirationDate() {
        var expirationDate = LocalDate.of(2026, 12, 31);
        var testInfo = CompleteTestInfo.builder()
                .testId(1)
                .versionId(VERSION_ID)
                .serviceName(SERVICE_NAME)
                .typeTest(TypeTest.CONVENIENCE)
                .convenienceResult("A")
                .creationDate(CREATION_DATE)
                .signatureDate(SIGNATURE_DATE)
                .expirationDate(expirationDate)
                .build();

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests().getFirst().getExpirationDate())
                .isEqualTo(expirationDate);
      }

      @Test
      @DisplayName("Should return null expiration date when signature date is null")
      void nullExpirationWhenSignatureDateIsNull() {
        var testInfo = CompleteTestInfo.builder()
                .testId(1)
                .versionId(VERSION_ID)
                .serviceName(SERVICE_NAME)
                .typeTest(TypeTest.CONVENIENCE)
                .convenienceResult("A")
                .creationDate(CREATION_DATE)
                .signatureDate(null)
                .versionExpirationDate(null)
                .build();

        when(respuestaClienteRepository.findCurrentTestsById(ID))
                .thenReturn(List.of(testInfo));

        var result = service.getCurrentCustomerTestsById(ID);

        assertThat(result.getActiveTests().getFirst().getExpirationDate()).isNull();
      }
    }
  }
  private CompleteTestInfo createTestInfo(TypeTest typeTest, String convenienceResult,
      String suitabilityResult) {
    return CompleteTestInfo.builder()
        .testId(1)
        .versionId(VERSION_ID)
        .serviceName(SERVICE_NAME)
        .typeTest(typeTest)
        .convenienceResult(convenienceResult)
        .suitabilityResult(suitabilityResult)
        .creationDate(CREATION_DATE)
        .signatureDate(SIGNATURE_DATE)
        .versionExpirationDate(null)
        .build();
  }

  private AnswersTest createMockAnswersTest() {
    var option = AnswersTest.Option.builder()
        .id(1)
        .text("Sí")
        .score(1)
        .value("B")
        .build();

    var question = AnswersTest.Question.builder()
        .id(17)
        .text("¿Quiere que se tenga en cuenta la sostenibilidad?")
        .option(option)
        .build();

    return AnswersTest.builder()
        .testId(1)
        .respuestaClienteId(1)
        .version(VERSION_ID)
        .questions(List.of(question))
        .build();
  }

  private SustainabilityPreferences createMockSustainabilityPreferences() {
    return SustainabilityPreferences.builder()
        .percentInPortfolio(10)
        .percentSustainableInvestment(5)
        .percentEUTaxonomyAlignment(null)
        .greenhouseGasPAI(false)
        .environmentalPAI(true)
        .socialPAI(false)
        .generalPAI(false)
        .build();
  }
}