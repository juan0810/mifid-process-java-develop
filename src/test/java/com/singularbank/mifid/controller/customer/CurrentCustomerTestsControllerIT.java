package com.singularbank.mifid.controller.customer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.controller.customer.dto.CurrentCustomerTestsResponseDTO;
import com.singularbank.mifid.controller.customer.dto.CurrentCustomerTestsResponseDTO.ActiveTestDTO;
import com.singularbank.mifid.controller.customer.mapper.CustomerActiveTestsMapper;
import com.singularbank.mifid.entity.CustomerActiveTests;
import com.singularbank.mifid.entity.CustomerActiveTests.ActiveTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.customer.CurrentCustomerTestsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
class CurrentCustomerTestsControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/current-customer-tests";

  private static final String VALID_DOCUMENT_DNI = "12345678A";
  private static final String VALID_DOCUMENT_NIE = "X1234567Z";
  private static final String INVALID_DOCUMENT_SHORT = "1234567";
  private static final String INVALID_DOCUMENT_LONG = "1234567890";

  private static final String SERVICE_ONBOARDING = "Onboarding";
  private static final String SERVICE_ADVISORY = "Advisory";
  private static final String SERVICE_WEB = "Web";

  private static final Integer TEST_ID_CONVENIENCE = 101;
  private static final Integer TEST_ID_SUITABILITY = 102;
  private static final Integer TEST_ID_SUSTAINABILITY = 103;
  private static final Integer TEST_ID_SINGLE = 201;

  private static final String RESULT_CONVENIENCE = "Conveniente para familias: A, B, C";
  private static final String RESULT_SUITABILITY = "Perfil conservador detectado";
  private static final String RESULT_SUSTAINABILITY =
      "El cliente ha manifestado su deseo de integrar un 10% de productos sostenibles";

  private static final LocalDateTime CREATION_DATE_1 = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
  private static final LocalDateTime SIGNATURE_DATE_1 = LocalDateTime.of(2024, 1, 15, 11, 0, 0);
  private static final LocalDate EXPIRATION_DATE_1 = LocalDate.of(2025, 1, 15);

  private static final LocalDateTime CREATION_DATE_2 = LocalDateTime.of(2024, 2, 20, 14, 15, 0);
  private static final LocalDateTime SIGNATURE_DATE_2 = LocalDateTime.of(2024, 2, 20, 15, 0, 0);
  private static final LocalDate EXPIRATION_DATE_2 = LocalDate.of(2025, 2, 20);

  private static final LocalDateTime CREATION_DATE_3 = LocalDateTime.of(2024, 3, 10, 9, 0, 0);
  private static final LocalDateTime SIGNATURE_DATE_3 = LocalDateTime.of(2024, 3, 10, 10, 30, 0);
  private static final LocalDate EXPIRATION_DATE_3 = LocalDate.of(2025, 3, 10);

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CurrentCustomerTestsService currentCustomerTestsService;

  @MockitoBean
  private CustomerActiveTestsMapper mapper;

  @Test
  void getCurrentCustomerTests_whenCustomerHasMultipleActiveTests_shouldReturn200WithAllTests()
      throws Exception {
    // Given
    var serviceResponse = createCustomerWithMultipleTests();
    var dtoResponse = createDtoWithMultipleTests();

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerIdentity").value(VALID_DOCUMENT_DNI))
        .andExpect(jsonPath("$.activeTests").isArray())
        .andExpect(jsonPath("$.activeTests.length()").value(3))
        // First test - Convenience
        .andExpect(jsonPath("$.activeTests[0].testId").value(TEST_ID_CONVENIENCE))
        .andExpect(jsonPath("$.activeTests[0].serviceName").value(SERVICE_ONBOARDING))
        .andExpect(jsonPath("$.activeTests[0].testType").value("CONVENIENCE"))
        .andExpect(jsonPath("$.activeTests[0].testResult").value(RESULT_CONVENIENCE))
        .andExpect(jsonPath("$.activeTests[0].creationDate").value("2024-01-15T10:30:00"))
        .andExpect(jsonPath("$.activeTests[0].signatureDate").value("2024-01-15T11:00:00"))
        .andExpect(jsonPath("$.activeTests[0].expirationDate").value("2025-01-15"))
        // Second test - Suitability
        .andExpect(jsonPath("$.activeTests[1].testId").value(TEST_ID_SUITABILITY))
        .andExpect(jsonPath("$.activeTests[1].serviceName").value(SERVICE_ADVISORY))
        .andExpect(jsonPath("$.activeTests[1].testType").value("SUITABILITY"))
        .andExpect(jsonPath("$.activeTests[1].testResult").value(RESULT_SUITABILITY))
        .andExpect(jsonPath("$.activeTests[1].creationDate").value("2024-02-20T14:15:00"))
        .andExpect(jsonPath("$.activeTests[1].signatureDate").value("2024-02-20T15:00:00"))
        .andExpect(jsonPath("$.activeTests[1].expirationDate").value("2025-02-20"))
        // Third test - Sustainability
        .andExpect(jsonPath("$.activeTests[2].testId").value(TEST_ID_SUSTAINABILITY))
        .andExpect(jsonPath("$.activeTests[2].serviceName").value(SERVICE_WEB))
        .andExpect(jsonPath("$.activeTests[2].testType").value("SUSTAINABILITY"))
        .andExpect(jsonPath("$.activeTests[2].testResult").value(RESULT_SUSTAINABILITY))
        .andExpect(jsonPath("$.activeTests[2].creationDate").value("2024-03-10T09:00:00"))
        .andExpect(jsonPath("$.activeTests[2].signatureDate").value("2024-03-10T10:30:00"))
        .andExpect(jsonPath("$.activeTests[2].expirationDate").value("2025-03-10"));

    verify(currentCustomerTestsService).getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI);
    verify(mapper).toDto(serviceResponse);
  }

  @Test
  void getCurrentCustomerTests_whenCustomerHasSingleActiveTest_shouldReturn200WithOneTest()
      throws Exception {
    // Given
    var serviceResponse = createCustomerWithSingleTest();
    var dtoResponse = createDtoWithSingleTest();

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerIdentity").value(VALID_DOCUMENT_DNI))
        .andExpect(jsonPath("$.activeTests").isArray())
        .andExpect(jsonPath("$.activeTests.length()").value(1))
        .andExpect(jsonPath("$.activeTests[0].testId").value(TEST_ID_SINGLE))
        .andExpect(jsonPath("$.activeTests[0].serviceName").value(SERVICE_ONBOARDING))
        .andExpect(jsonPath("$.activeTests[0].testType").value("CONVENIENCE"))
        .andExpect(jsonPath("$.activeTests[0].testResult").value(RESULT_CONVENIENCE));

    verify(currentCustomerTestsService).getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI);
    verify(mapper).toDto(serviceResponse);
  }

  @Test
  void getCurrentCustomerTests_whenCustomerHasNoActiveTests_shouldReturn200WithEmptyList()
      throws Exception {
    // Given
    var serviceResponse = createCustomerWithNoTests();
    var dtoResponse = createDtoWithNoTests();

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerIdentity").value(VALID_DOCUMENT_DNI))
        .andExpect(jsonPath("$.activeTests").isArray())
        .andExpect(jsonPath("$.activeTests").isEmpty());

    verify(currentCustomerTestsService).getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI);
    verify(mapper).toDto(serviceResponse);
  }

  @Test
  void getCurrentCustomerTests_whenNIEDocument_shouldReturn200() throws Exception {
    // Given
    var serviceResponse = createCustomerWithSingleTest(VALID_DOCUMENT_NIE);
    var dtoResponse = createDtoWithSingleTest(VALID_DOCUMENT_NIE);

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_NIE))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_NIE)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerIdentity").value(VALID_DOCUMENT_NIE))
        .andExpect(jsonPath("$.activeTests").isArray());

    verify(currentCustomerTestsService).getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_NIE);
  }

  @Test
  void getCurrentCustomerTests_whenConvenienceTestOnly_shouldReturn200() throws Exception {
    // Given
    var activeTest = createActiveTest(
        TEST_ID_CONVENIENCE,
        SERVICE_ONBOARDING,
        TypeTest.CONVENIENCE,
        RESULT_CONVENIENCE,
        CREATION_DATE_1,
        SIGNATURE_DATE_1,
        EXPIRATION_DATE_1
    );

    var serviceResponse = CustomerActiveTests.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(activeTest))
        .build();

    var activeTestDto = createActiveTestDto(
        TEST_ID_CONVENIENCE,
        SERVICE_ONBOARDING,
        TypeTest.CONVENIENCE,
        RESULT_CONVENIENCE,
        CREATION_DATE_1,
        SIGNATURE_DATE_1,
        EXPIRATION_DATE_1
    );

    var dtoResponse = CurrentCustomerTestsResponseDTO.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(activeTestDto))
        .build();

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.activeTests[0].testType").value("CONVENIENCE"));
  }

  @Test
  void getCurrentCustomerTests_whenSuitabilityTestOnly_shouldReturn200() throws Exception {
    // Given
    var activeTest = createActiveTest(
        TEST_ID_SUITABILITY,
        SERVICE_ADVISORY,
        TypeTest.SUITABILITY,
        RESULT_SUITABILITY,
        CREATION_DATE_2,
        SIGNATURE_DATE_2,
        EXPIRATION_DATE_2
    );

    var serviceResponse = CustomerActiveTests.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(activeTest))
        .build();

    var activeTestDto = createActiveTestDto(
        TEST_ID_SUITABILITY,
        SERVICE_ADVISORY,
        TypeTest.SUITABILITY,
        RESULT_SUITABILITY,
        CREATION_DATE_2,
        SIGNATURE_DATE_2,
        EXPIRATION_DATE_2
    );

    var dtoResponse = CurrentCustomerTestsResponseDTO.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(activeTestDto))
        .build();

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.activeTests[0].testType").value("SUITABILITY"));
  }

  @Test
  void getCurrentCustomerTests_whenSustainabilityTestOnly_shouldReturn200() throws Exception {
    // Given
    var activeTest = createActiveTest(
        TEST_ID_SUSTAINABILITY,
        SERVICE_WEB,
        TypeTest.SUSTAINABILITY,
        RESULT_SUSTAINABILITY,
        CREATION_DATE_3,
        SIGNATURE_DATE_3,
        EXPIRATION_DATE_3
    );

    var serviceResponse = CustomerActiveTests.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(activeTest))
        .build();

    var activeTestDto = createActiveTestDto(
        TEST_ID_SUSTAINABILITY,
        SERVICE_WEB,
        TypeTest.SUSTAINABILITY,
        RESULT_SUSTAINABILITY,
        CREATION_DATE_3,
        SIGNATURE_DATE_3,
        EXPIRATION_DATE_3
    );

    var dtoResponse = CurrentCustomerTestsResponseDTO.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(activeTestDto))
        .build();

    when(currentCustomerTestsService.getCurrentCustomerTestsByIdClient(VALID_DOCUMENT_DNI))
        .thenReturn(serviceResponse);
    when(mapper.toDto(serviceResponse)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.activeTests[0].testType").value("SUSTAINABILITY"));
  }

  @Test
  void getCurrentCustomerTests_whenDocumentTooShort_shouldReturn400() throws Exception {
    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", INVALID_DOCUMENT_SHORT)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getCurrentCustomerTests_whenDocumentTooLong_shouldReturn400() throws Exception {
    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", INVALID_DOCUMENT_LONG)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  private CustomerActiveTests createCustomerWithMultipleTests() {
    return CustomerActiveTests.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(
            createActiveTest(TEST_ID_CONVENIENCE, SERVICE_ONBOARDING, TypeTest.CONVENIENCE,
                RESULT_CONVENIENCE, CREATION_DATE_1, SIGNATURE_DATE_1, EXPIRATION_DATE_1),
            createActiveTest(TEST_ID_SUITABILITY, SERVICE_ADVISORY, TypeTest.SUITABILITY,
                RESULT_SUITABILITY, CREATION_DATE_2, SIGNATURE_DATE_2, EXPIRATION_DATE_2),
            createActiveTest(TEST_ID_SUSTAINABILITY, SERVICE_WEB, TypeTest.SUSTAINABILITY,
                RESULT_SUSTAINABILITY, CREATION_DATE_3, SIGNATURE_DATE_3, EXPIRATION_DATE_3)
        ))
        .build();
  }

  private CustomerActiveTests createCustomerWithSingleTest() {
    return createCustomerWithSingleTest(VALID_DOCUMENT_DNI);
  }

  private CustomerActiveTests createCustomerWithSingleTest(String documentNumber) {
    return CustomerActiveTests.builder()
        .customerIdentity(documentNumber)
        .activeTests(List.of(
            createActiveTest(TEST_ID_SINGLE, SERVICE_ONBOARDING, TypeTest.CONVENIENCE,
                RESULT_CONVENIENCE, CREATION_DATE_1, SIGNATURE_DATE_1, EXPIRATION_DATE_1)
        ))
        .build();
  }

  private CustomerActiveTests createCustomerWithNoTests() {
    return CustomerActiveTests.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(Collections.emptyList())
        .build();
  }

  private ActiveTest createActiveTest(
      Integer testId,
      String serviceName,
      TypeTest typeTest,
      String testResult,
      LocalDateTime creationDate,
      LocalDateTime signatureDate,
      LocalDate expirationDate
  ) {
    return ActiveTest.builder()
        .testId(testId)
        .serviceName(serviceName)
        .testType(typeTest)
        .testResult(testResult)
        .creationDate(creationDate)
        .signatureDate(signatureDate)
        .expirationDate(expirationDate)
        .build();
  }

  private CurrentCustomerTestsResponseDTO createDtoWithMultipleTests() {
    return CurrentCustomerTestsResponseDTO.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(List.of(
            createActiveTestDto(TEST_ID_CONVENIENCE, SERVICE_ONBOARDING, TypeTest.CONVENIENCE,
                RESULT_CONVENIENCE, CREATION_DATE_1, SIGNATURE_DATE_1, EXPIRATION_DATE_1),
            createActiveTestDto(TEST_ID_SUITABILITY, SERVICE_ADVISORY, TypeTest.SUITABILITY,
                RESULT_SUITABILITY, CREATION_DATE_2, SIGNATURE_DATE_2, EXPIRATION_DATE_2),
            createActiveTestDto(TEST_ID_SUSTAINABILITY, SERVICE_WEB, TypeTest.SUSTAINABILITY,
                RESULT_SUSTAINABILITY, CREATION_DATE_3, SIGNATURE_DATE_3, EXPIRATION_DATE_3)
        ))
        .build();
  }

  private CurrentCustomerTestsResponseDTO createDtoWithSingleTest() {
    return createDtoWithSingleTest(VALID_DOCUMENT_DNI);
  }

  private CurrentCustomerTestsResponseDTO createDtoWithSingleTest(String documentNumber) {
    return CurrentCustomerTestsResponseDTO.builder()
        .customerIdentity(documentNumber)
        .activeTests(List.of(
            createActiveTestDto(TEST_ID_SINGLE, SERVICE_ONBOARDING, TypeTest.CONVENIENCE,
                RESULT_CONVENIENCE, CREATION_DATE_1, SIGNATURE_DATE_1, EXPIRATION_DATE_1)
        ))
        .build();
  }

  private CurrentCustomerTestsResponseDTO createDtoWithNoTests() {
    return CurrentCustomerTestsResponseDTO.builder()
        .customerIdentity(VALID_DOCUMENT_DNI)
        .activeTests(Collections.emptyList())
        .build();
  }

  private ActiveTestDTO createActiveTestDto(
      Integer testId,
      String serviceName,
      TypeTest typeTest,
      String testResult,
      LocalDateTime creationDate,
      LocalDateTime signatureDate,
      LocalDate expirationDate
  ) {
    return ActiveTestDTO.builder()
        .testId(testId.shortValue())
        .serviceName(serviceName)
        .testType(typeTest)
        .testResult(testResult)
        .creationDate(creationDate)
        .signatureDate(signatureDate)
        .expirationDate(expirationDate)
        .build();
  }
}