package com.singularbank.mifid.service.customer.impl;

import com.singularbank.mifid.entity.*;
import com.singularbank.mifid.entity.CustomerActiveTests.ActiveTest;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.customer.CurrentCustomerTestsService;
import com.singularbank.mifid.service.sustainability.SustainabilityCalculatorService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentCustomerTestsServiceImpl implements CurrentCustomerTestsService {

  private final SustainabilityCalculatorService sustainabilityCalculator;
  private final RespuestaClienteRepository respuestaClienteRepository;
  private final RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;

  private static final String RESULT_PENDING = "Pendiente";
  private static final String RESULT_NO_DATA = "Sin resultado disponible";
  private static final String NO_FAMILIES_MESSAGE = "No hay familias convenientes";

  private static final int CONVENIENCE_EXPIRATION_YEARS = 1;
  private static final int SUITABILITY_EXPIRATION_YEARS = 1;
  private static final int SUSTAINABILITY_EXPIRATION_YEARS = 2;

  @Override
  @Transactional(readOnly = true)
  public CustomerActiveTests getCurrentCustomerTestsByIdClient(String customerIdentity) {

    log.debug("Retrieving active tests for customer: {}", customerIdentity);
    Objects.requireNonNull(customerIdentity, "customerIdentity is required");
    var customerTests = respuestaClienteRepository.findCurrentActiveTestsByIdentity(
        customerIdentity);

    if (customerTests.isEmpty()) {
      log.info("No active tests found for customer: {}", customerIdentity);
      return buildEmptyResponse(customerIdentity);
    }

    var activeTests = customerTests.stream()
        .map(testInfo -> mapToActiveTest(testInfo, customerIdentity))
        .toList();

    log.info("Found {} active test(s) for customer: {}", activeTests.size(), customerIdentity);

    return CustomerActiveTests.builder()
        .customerIdentity(customerIdentity)
        .activeTests(activeTests)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerActiveTests getCurrentCustomerTestsById(Integer id) {

    log.debug("Retrieving tests for id: {}", id);
    Objects.requireNonNull(id, "Id is required");
    var customerTests = respuestaClienteRepository.findCurrentTestsById(id);

    if (customerTests.isEmpty()) {
      log.info("No tests found for id: {}", id);
      return buildEmptyResponse("");
    }

    var activeTests = customerTests.stream()
            .map(testInfo -> mapToActiveTest(testInfo, id))
            .toList();

    log.info("Found {} test(s) for id: {}", activeTests.size(), id);

    return CustomerActiveTests.builder()
            .customerIdentity(extractCustomerIdentityFromTests(customerTests))
            .activeTests(activeTests)
            .build();
  }

  private String extractCustomerIdentityFromTests(List<CompleteTestInfo> customerTests) {
    return customerTests.stream().findFirst().map(CompleteTestInfo::getCustomerIdentity).orElse("");
  }

  private ActiveTest mapToActiveTest(CompleteTestInfo testInfo, String customerIdentity) {

    return switch (testInfo.getTypeTest()) {
      case CONVENIENCE -> buildConvenienceTest(testInfo, testInfo.getExpirationDate());
      case SUITABILITY -> buildSuitabilityTest(testInfo, testInfo.getExpirationDate());
      case SUSTAINABILITY -> buildSustainabilityTestByCustomerId(testInfo, customerIdentity, testInfo.getExpirationDate());
    };
  }

  private ActiveTest mapToActiveTest(CompleteTestInfo testInfo, Integer id) {

    return switch (testInfo.getTypeTest()) {
      case CONVENIENCE -> buildConvenienceTest(testInfo, testInfo.getExpirationDate());
      case SUITABILITY -> buildSuitabilityTest(testInfo, testInfo.getExpirationDate());
      case SUSTAINABILITY -> buildSustainabilityTestById(testInfo, id, testInfo.getExpirationDate());
    };
  }

  private ActiveTest buildSuitabilityTest(CompleteTestInfo testInfo, LocalDate expirationDate) {
    var testResult =
        (testInfo.getSuitabilityResult() != null && !testInfo.getSuitabilityResult().isBlank())
            ? testInfo.getSuitabilityResult()
            : RESULT_PENDING;

    return ActiveTest.builder()
        .testId(testInfo.getTestId())
        .serviceName(testInfo.getServiceName())
        .testType(TypeTest.SUITABILITY)
        .testResult(testResult)
        .families(null)
        .creationDate(testInfo.getCreationDate())
        .signatureDate(testInfo.getSignatureDate())
        .expirationDate(expirationDate)
        .build();
  }

  private ActiveTest buildConvenienceTest(CompleteTestInfo testInfo, LocalDate expirationDate) {
    var storedResult = testInfo.getConvenienceResult();
    if (storedResult == null) {
      return buildPendingConvenienceTest(testInfo, expirationDate);
    }
    var convenientFamilies = parseConvenienceFamilies(storedResult);

    String testResult;
    String testResultDescription;
    if (convenientFamilies.isEmpty()) {
      testResult = "";
      testResultDescription = NO_FAMILIES_MESSAGE;
    } else {
      testResult = convenientFamilies.stream()
          .map(Enum::name)
          .toList()
          .toString()
          .replaceAll("[\\[\\] ]", ""); // "A,B,C"

      testResultDescription = "Conveniente para " + convenientFamilies.stream()
          .map(ProductFamily::getDescription)
          .reduce((a, b) -> a + ", " + b)
          .orElse("");
    }

    return ActiveTest.builder()
        .testId(testInfo.getTestId())
        .serviceName(testInfo.getServiceName())
        .testType(TypeTest.CONVENIENCE)
        .testResult(testResult)
        .testResultDescription(testResultDescription)
        .families(convenientFamilies)
        .creationDate(testInfo.getCreationDate())
        .signatureDate(testInfo.getSignatureDate())
        .expirationDate(expirationDate)
        .build();
  }

  private ActiveTest buildPendingConvenienceTest(CompleteTestInfo testInfo,
      LocalDate expirationDate) {
    return ActiveTest.builder()
        .testId(testInfo.getTestId())
        .serviceName(testInfo.getServiceName())
        .testType(TypeTest.CONVENIENCE)
        .testResult(RESULT_PENDING)
        .testResultDescription(null)
        .families(null)
        .creationDate(testInfo.getCreationDate())
        .signatureDate(testInfo.getSignatureDate())
        .expirationDate(expirationDate)
        .build();
  }

  private ActiveTest buildSustainabilityTestByCustomerId(CompleteTestInfo testInfo, String customerIdentity,
                                             LocalDate expirationDate) {
    try {
      var answersTest = respuestaClienteDetalleRepository
              .findAnswersByIdentityAndTestType(customerIdentity, TypeTest.SUSTAINABILITY);

      var testResult = sustainabilityCalculator.calculateSustainabilityResult(answersTest);
      var preferences = sustainabilityCalculator.extractSustainabilityPreferences(answersTest);

      return ActiveTest.builder()
              .testId(testInfo.getTestId())
              .serviceName(testInfo.getServiceName())
              .testType(TypeTest.SUSTAINABILITY)
              .testResult(testResult)
              .sustainabilityPreferences(preferences)
              .families(null)
              .creationDate(testInfo.getCreationDate())
              .signatureDate(testInfo.getSignatureDate())
              .expirationDate(expirationDate)
              .build();

    } catch (ResourceNotFoundException e) {
      log.info("No sustainability answers found for customer: {}", customerIdentity);
      return buildEmptySustainabilityTest(testInfo, expirationDate);
    } catch (Exception e) {
      log.error("Failed to calculate sustainability test for customer '{}': {}",
              customerIdentity, e.getMessage(), e);
      return buildEmptySustainabilityTest(testInfo, expirationDate);
    }
  }

  private ActiveTest buildSustainabilityTestById(CompleteTestInfo testInfo, Integer id, LocalDate expirationDate) {
    try {

      var answersTest = respuestaClienteDetalleRepository.findAnswersByTestId(id, TypeTest.SUSTAINABILITY);
      var testResult = sustainabilityCalculator.calculateSustainabilityResult(answersTest);
      var preferences = sustainabilityCalculator.extractSustainabilityPreferences(answersTest);

      return ActiveTest.builder()
          .testId(testInfo.getTestId())
          .serviceName(testInfo.getServiceName())
          .testType(TypeTest.SUSTAINABILITY)
          .testResult(testResult)
          .sustainabilityPreferences(preferences)
          .families(null)
          .creationDate(testInfo.getCreationDate())
          .signatureDate(testInfo.getSignatureDate())
          .expirationDate(expirationDate)
          .build();

    } catch (Exception e) {
      log.error("Failed to calculate sustainability test for id '{}': {}", id, e.getMessage(), e);
      return buildEmptySustainabilityTest(testInfo, expirationDate);
    }
  }

  private ActiveTest buildEmptySustainabilityTest(CompleteTestInfo testInfo,
      LocalDate expirationDate) {
    return ActiveTest.builder()
        .testId(testInfo.getTestId())
        .serviceName(testInfo.getServiceName())
        .testType(TypeTest.SUSTAINABILITY)
        .testResult(RESULT_NO_DATA)
        .sustainabilityPreferences(null)
        .families(null)
        .creationDate(testInfo.getCreationDate())
        .signatureDate(testInfo.getSignatureDate())
        .expirationDate(expirationDate)
        .build();
  }

  private List<ProductFamily> parseConvenienceFamilies(String convenienceResult) {
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


  private CustomerActiveTests buildEmptyResponse(String customerIdentity) {
    return CustomerActiveTests.builder()
        .customerIdentity(customerIdentity)
        .activeTests(List.of())
        .build();
  }
}