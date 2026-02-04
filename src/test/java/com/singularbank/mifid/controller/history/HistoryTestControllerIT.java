package com.singularbank.mifid.controller.history;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.controller.history.mapper.HistoryTestResponseMapper;
import com.singularbank.mifid.controller.history.response.HistoryTestResponseDTO;
import com.singularbank.mifid.controller.history.response.HistoryTestResponseDTO.TestItemDTO;
import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestItem;
import com.singularbank.mifid.entity.HistoryTestPage;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.history.HistoryTestService;
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
class HistoryTestControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid";

  private static final String VALID_DOCUMENT_DNI = "12345678A";
  private static final String VALID_DOCUMENT_NIE = "X1234567Z";
  private static final String INVALID_DOCUMENT_SHORT = "1234567";
  private static final String INVALID_DOCUMENT_LONG = "1234567890";

  private static final Integer TEST_ID_1 = 101;
  private static final Integer TEST_ID_2 = 102;
  private static final Integer TEST_ID_3 = 103;

  private static final String PROFILE_CONSERVADOR = "Conservador";
  private static final String PROFILE_MODERADO = "Moderado";

  private static final LocalDateTime CREATED_AT_1 = LocalDateTime.of(2025, 10, 1, 9, 30, 0);
  private static final LocalDateTime SIGNED_AT_1 = LocalDateTime.of(2025, 10, 1, 10, 15, 0);
  private static final LocalDateTime EXPIRES_AT_1 = LocalDateTime.of(2028, 10, 1, 10, 15, 0);

  private static final LocalDateTime CREATED_AT_2 = LocalDateTime.of(2026, 1, 14, 8, 0, 0);
  private static final LocalDateTime CREATED_AT_3 = LocalDateTime.of(2022, 5, 20, 14, 20, 0);
  private static final LocalDateTime SIGNED_AT_3 = LocalDateTime.of(2022, 5, 21, 9, 0, 0);
  private static final LocalDateTime EXPIRES_AT_3 = LocalDateTime.of(2024, 5, 21, 9, 0, 0);

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private HistoryTestService historyTestService;

  @MockitoBean
  private HistoryTestResponseMapper mapper;

  @Test
  void getTestHistory_whenCustomerHasMultipleTests_shouldReturn200WithAllTests() throws Exception {
    // Given
    var servicePage = createPageWithMultipleTests();
    var dtoResponse = createDtoWithMultipleTests();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.currentPage").value(0))
        .andExpect(jsonPath("$.pagination").value(20))
        .andExpect(jsonPath("$.tests").isArray())
        .andExpect(jsonPath("$.tests.length()").value(3))
        // First test - SUITABILITY SIGNED
        .andExpect(jsonPath("$.tests[0].id").value(TEST_ID_1))
        .andExpect(jsonPath("$.tests[0].type").value("SUITABILITY"))
        .andExpect(jsonPath("$.tests[0].state").value("SIGNED"))
        .andExpect(jsonPath("$.tests[0].profile").value(PROFILE_MODERADO))
        .andExpect(jsonPath("$.tests[0].createdAt").value("2025-10-01T09:30:00"))
        .andExpect(jsonPath("$.tests[0].signedAt").value("2025-10-01T10:15:00"))
        .andExpect(jsonPath("$.tests[0].expiresAt").value("2028-10-01T10:15:00"))
        // Second test - CONVENIENCE DRAFT
        .andExpect(jsonPath("$.tests[1].id").value(TEST_ID_2))
        .andExpect(jsonPath("$.tests[1].type").value("CONVENIENCE"))
        .andExpect(jsonPath("$.tests[1].state").value("DRAFT"))
        .andExpect(jsonPath("$.tests[1].profile").doesNotExist())
        .andExpect(jsonPath("$.tests[1].signedAt").doesNotExist())
        .andExpect(jsonPath("$.tests[1].expiresAt").doesNotExist())
        // Third test - SUSTAINABILITY EXPIRED
        .andExpect(jsonPath("$.tests[2].id").value(TEST_ID_3))
        .andExpect(jsonPath("$.tests[2].type").value("SUSTAINABILITY"))
        .andExpect(jsonPath("$.tests[2].state").value("EXPIRED"));

    verify(historyTestService).getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class));
    verify(mapper).toResponse(servicePage);
  }

  @Test
  void getTestHistory_whenNoTests_shouldReturn200WithEmptyList() throws Exception {
    // Given
    var servicePage = createEmptyPage();
    var dtoResponse = createEmptyDto();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0))
        .andExpect(jsonPath("$.tests").isArray())
        .andExpect(jsonPath("$.tests").isEmpty());
  }

  @Test
  void getTestHistory_whenFilterByTypeSuitability_shouldReturn200() throws Exception {
    // Given
    var servicePage = createPageWithSingleSuitabilityTest();
    var dtoResponse = createDtoWithSingleSuitabilityTest();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .param("type", "SUITABILITY")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.tests[0].type").value("SUITABILITY"))
        .andExpect(jsonPath("$.tests[0].profile").value(PROFILE_CONSERVADOR));
  }

  @Test
  void getTestHistory_whenFilterByTypeConvenience_shouldReturn200() throws Exception {
    // Given
    var servicePage = createPageWithSingleConvenienceTest();
    var dtoResponse = createDtoWithSingleConvenienceTest();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .param("type", "CONVENIENCE")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tests[0].type").value("CONVENIENCE"))
        .andExpect(jsonPath("$.tests[0].profile").doesNotExist());
  }

  @Test
  void getTestHistory_whenFilterByStateSigned_shouldReturn200() throws Exception {
    // Given
    var servicePage = createPageWithSignedTest();
    var dtoResponse = createDtoWithSignedTest();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .param("state", "SIGNED")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tests[0].state").value("SIGNED"))
        .andExpect(jsonPath("$.tests[0].signedAt").exists());
  }

  @Test
  void getTestHistory_whenFilterByDateRange_shouldReturn200() throws Exception {
    // Given
    var servicePage = createPageWithSingleSuitabilityTest();
    var dtoResponse = createDtoWithSingleSuitabilityTest();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .param("from", "2025-01-01")
            .param("to", "2025-12-31")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tests").isArray());
  }

  @Test
  void getTestHistory_whenPaginationApplied_shouldReturn200WithPaginatedResults() throws Exception {
    // Given
    var servicePage = createPaginatedPage();
    var dtoResponse = createPaginatedDto();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .param("page", "0")
            .param("size", "2")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.currentPage").value(0))
        .andExpect(jsonPath("$.pagination").value(2))
        .andExpect(jsonPath("$.tests.length()").value(2));
  }

  @Test
  void getTestHistory_whenNIEDocument_shouldReturn200() throws Exception {
    // Given
    var servicePage = createPageWithSingleSuitabilityTest();
    var dtoResponse = createDtoWithSingleSuitabilityTest();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_NIE), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_NIE)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tests").isArray());

    verify(historyTestService).getTestHistory(eq(VALID_DOCUMENT_NIE), any(HistoryTestFilter.class));
  }

  @Test
  void getTestHistory_whenDocumentTooShort_shouldReturn400() throws Exception {
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", INVALID_DOCUMENT_SHORT)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTestHistory_whenDocumentTooLong_shouldReturn400() throws Exception {
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", INVALID_DOCUMENT_LONG)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTestHistory_whenCombinedFilters_shouldReturn200() throws Exception {
    // Given
    var servicePage = createPageWithSingleSuitabilityTest();
    var dtoResponse = createDtoWithSingleSuitabilityTest();

    when(historyTestService.getTestHistory(eq(VALID_DOCUMENT_DNI), any(HistoryTestFilter.class)))
        .thenReturn(servicePage);
    when(mapper.toResponse(servicePage)).thenReturn(dtoResponse);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}/tests", VALID_DOCUMENT_DNI)
            .param("type", "SUITABILITY")
            .param("state", "SIGNED")
            .param("from", "2025-01-01")
            .param("to", "2025-12-31")
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

// Helper methods for creating test data

  private HistoryTestPage createPageWithMultipleTests() {
    return HistoryTestPage.builder()
        .totalElements(3)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(List.of(
            createTestItem(TEST_ID_1, TypeTest.SUITABILITY, StateTest.SIGNED, PROFILE_MODERADO,
                CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1),
            createTestItem(TEST_ID_2, TypeTest.CONVENIENCE, StateTest.DRAFT, null,
                CREATED_AT_2, null, null),
            createTestItem(TEST_ID_3, TypeTest.SUSTAINABILITY, StateTest.EXPIRED, null,
                CREATED_AT_3, SIGNED_AT_3, EXPIRES_AT_3)
        ))
        .build();
  }

  private HistoryTestResponseDTO createDtoWithMultipleTests() {
    return new HistoryTestResponseDTO(
        3, 1, 0, 20,
        List.of(
            new TestItemDTO(TEST_ID_1, "SUITABILITY", "SIGNED", PROFILE_MODERADO,
                CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1),
            new TestItemDTO(TEST_ID_2, "CONVENIENCE", "DRAFT", null,
                CREATED_AT_2, null, null),
            new TestItemDTO(TEST_ID_3, "SUSTAINABILITY", "EXPIRED", null,
                CREATED_AT_3, SIGNED_AT_3, EXPIRES_AT_3)
        )
    );
  }

  private HistoryTestPage createEmptyPage() {
    return HistoryTestPage.builder()
        .totalElements(0)
        .totalPages(0)
        .currentPage(0)
        .pageSize(20)
        .tests(Collections.emptyList())
        .build();
  }

  private HistoryTestResponseDTO createEmptyDto() {
    return new HistoryTestResponseDTO(0, 0, 0, 20, Collections.emptyList());
  }

  private HistoryTestPage createPageWithSingleSuitabilityTest() {
    return HistoryTestPage.builder()
        .totalElements(1)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(List.of(
            createTestItem(TEST_ID_1, TypeTest.SUITABILITY, StateTest.SIGNED, PROFILE_CONSERVADOR,
                CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1)
        ))
        .build();
  }

  private HistoryTestResponseDTO createDtoWithSingleSuitabilityTest() {
    return new HistoryTestResponseDTO(
        1, 1, 0, 20,
        List.of(new TestItemDTO(TEST_ID_1, "SUITABILITY", "SIGNED", PROFILE_CONSERVADOR,
            CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1))
    );
  }

  private HistoryTestPage createPageWithSingleConvenienceTest() {
    return HistoryTestPage.builder()
        .totalElements(1)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(List.of(
            createTestItem(TEST_ID_2, TypeTest.CONVENIENCE, StateTest.DRAFT, null,
                CREATED_AT_2, null, null)
        ))
        .build();
  }

  private HistoryTestResponseDTO createDtoWithSingleConvenienceTest() {
    return new HistoryTestResponseDTO(
        1, 1, 0, 20,
        List.of(new TestItemDTO(TEST_ID_2, "CONVENIENCE", "DRAFT", null,
            CREATED_AT_2, null, null))
    );
  }

  private HistoryTestPage createPageWithSignedTest() {
    return HistoryTestPage.builder()
        .totalElements(1)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(List.of(
            createTestItem(TEST_ID_1, TypeTest.SUITABILITY, StateTest.SIGNED, PROFILE_MODERADO,
                CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1)
        ))
        .build();
  }

  private HistoryTestResponseDTO createDtoWithSignedTest() {
    return new HistoryTestResponseDTO(
        1, 1, 0, 20,
        List.of(new TestItemDTO(TEST_ID_1, "SUITABILITY", "SIGNED", PROFILE_MODERADO,
            CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1))
    );
  }

  private HistoryTestPage createPaginatedPage() {
    return HistoryTestPage.builder()
        .totalElements(5)
        .totalPages(3)
        .currentPage(0)
        .pageSize(2)
        .tests(List.of(
            createTestItem(TEST_ID_1, TypeTest.SUITABILITY, StateTest.SIGNED, PROFILE_MODERADO,
                CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1),
            createTestItem(TEST_ID_2, TypeTest.CONVENIENCE, StateTest.DRAFT, null,
                CREATED_AT_2, null, null)
        ))
        .build();
  }

  private HistoryTestResponseDTO createPaginatedDto() {
    return new HistoryTestResponseDTO(
        5, 3, 0, 2,
        List.of(
            new TestItemDTO(TEST_ID_1, "SUITABILITY", "SIGNED", PROFILE_MODERADO,
                CREATED_AT_1, SIGNED_AT_1, EXPIRES_AT_1),
            new TestItemDTO(TEST_ID_2, "CONVENIENCE", "DRAFT", null,
                CREATED_AT_2, null, null)
        )
    );
  }

  private HistoryTestItem createTestItem(
      Integer id,
      TypeTest type,
      StateTest state,
      String profile,
      LocalDateTime createdAt,
      LocalDateTime signedAt,
      LocalDateTime expiresAt
  ) {
    return HistoryTestItem.builder()
        .id(id)
        .type(type)
        .state(state)
        .profile(profile)
        .createdAt(createdAt)
        .signedAt(signedAt)
        .expiresAt(expiresAt)
        .build();
  }
}