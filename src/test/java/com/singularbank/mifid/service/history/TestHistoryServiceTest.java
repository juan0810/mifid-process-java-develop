package com.singularbank.mifid.service.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestItem;
import com.singularbank.mifid.entity.HistoryTestPage;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.history.impl.HistoryTestServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test History Service Tests")
class TestHistoryServiceTest {

  @Mock
  private RespuestaClienteRepository respuestaClienteRepository;

  @InjectMocks
  private HistoryTestServiceImpl service;

  private static final String DOCUMENT_NUMBER = "12345678A";

  @Nested
  @DisplayName("Edge Cases - Invalid Input")
  class EdgeCases {

    @Test
    @DisplayName("Should throw NullPointerException when documentNumber is null")
    void nullDocumentNumber() {
      var filter = createDefaultFilter();

      assertThatThrownBy(() -> service.getTestHistory(null, filter))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should return empty page when no tests found")
    void noTestsFound() {
      var filter = createDefaultFilter();
      var emptyPage = createEmptyPage();

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(emptyPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result).isNotNull();
      assertThat(result.getTests()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
      assertThat(result.getTotalPages()).isZero();

      verify(respuestaClienteRepository).findTestHistory(DOCUMENT_NUMBER, filter);
    }
  }

  @Nested
  @DisplayName("Successful Retrieval Scenarios")
  class SuccessfulRetrievalScenarios {

    @Test
    @DisplayName("Should return test history with default filter")
    void withDefaultFilter() {
      var filter = createDefaultFilter();
      var expectedPage = createPageWithTests(3);

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(expectedPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result.getTests()).hasSize(3);
      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getCurrentPage()).isZero();

      verify(respuestaClienteRepository).findTestHistory(DOCUMENT_NUMBER, filter);
    }

    @ParameterizedTest
    @EnumSource(TypeTest.class)
    @DisplayName("Should filter by test type")
    void filterByTestType(TypeTest typeTest) {
      var filter = HistoryTestFilter.builder()
          .type(typeTest)
          .page(0)
          .size(20)
          .build();
      var expectedPage = createPageWithTestType(typeTest);

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(expectedPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result.getTests()).isNotEmpty();
      assertThat(result.getTests()).allMatch(t -> t.getType() == typeTest);
    }

    @ParameterizedTest
    @EnumSource(StateTest.class)
    @DisplayName("Should filter by test state")
    void filterByTestState(StateTest state) {
      var filter = HistoryTestFilter.builder()
          .state(state)
          .page(0)
          .size(20)
          .build();
      var expectedPage = createPageWithState(state);

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(expectedPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result.getTests()).isNotEmpty();
      assertThat(result.getTests()).allMatch(t -> t.getState() == state);
    }

    @Test
    @DisplayName("Should filter by date range")
    void filterByDateRange() {
      var fromDate = LocalDate.of(2025, 1, 1);
      var toDate = LocalDate.of(2025, 12, 31);
      var filter = HistoryTestFilter.builder()
          .from(fromDate)
          .to(toDate)
          .page(0)
          .size(20)
          .build();
      var expectedPage = createPageWithTests(2);

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(expectedPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result.getTests()).hasSize(2);
      verify(respuestaClienteRepository).findTestHistory(DOCUMENT_NUMBER, filter);
    }
  }

  @Nested
  @DisplayName("Pagination Scenarios")
  class PaginationScenarios {

    @Test
    @DisplayName("Should return correct pagination info")
    void correctPaginationInfo() {
      var filter = HistoryTestFilter.builder()
          .page(2)
          .size(10)
          .build();
      var expectedPage = HistoryTestPage.builder()
          .totalElements(45)
          .totalPages(5)
          .currentPage(2)
          .pageSize(10)
          .tests(createTestItems(10))
          .build();

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(expectedPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result.getTotalElements()).isEqualTo(45);
      assertThat(result.getTotalPages()).isEqualTo(5);
      assertThat(result.getCurrentPage()).isEqualTo(2);
      assertThat(result.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return last page with remaining elements")
    void lastPageWithRemainingElements() {
      var filter = HistoryTestFilter.builder()
          .page(4)
          .size(10)
          .build();
      var expectedPage = HistoryTestPage.builder()
          .totalElements(45)
          .totalPages(5)
          .currentPage(4)
          .pageSize(10)
          .tests(createTestItems(5))
          .build();

      when(respuestaClienteRepository.findTestHistory(DOCUMENT_NUMBER, filter))
          .thenReturn(expectedPage);

      var result = service.getTestHistory(DOCUMENT_NUMBER, filter);

      assertThat(result.getTests()).hasSize(5);
      assertThat(result.getCurrentPage()).isEqualTo(4);
    }
  }

  private HistoryTestFilter createDefaultFilter() {
    return HistoryTestFilter.builder()
        .page(0)
        .size(20)
        .build();
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

  private HistoryTestPage createPageWithTests(int count) {
    return HistoryTestPage.builder()
        .totalElements(count)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(createTestItems(count))
        .build();
  }

  private HistoryTestPage createPageWithTestType(TypeTest type) {
    var item = HistoryTestItem.builder()
        .id(1)
        .type(type)
        .state(StateTest.SIGNED)
        .createdAt(LocalDateTime.now())
        .build();
    return HistoryTestPage.builder()
        .totalElements(1)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(List.of(item))
        .build();
  }

  private HistoryTestPage createPageWithState(StateTest state) {
    var item = HistoryTestItem.builder()
        .id(1)
        .type(TypeTest.CONVENIENCE)
        .state(state)
        .createdAt(LocalDateTime.now())
        .build();
    return HistoryTestPage.builder()
        .totalElements(1)
        .totalPages(1)
        .currentPage(0)
        .pageSize(20)
        .tests(List.of(item))
        .build();
  }

  private List<HistoryTestItem> createTestItems(int count) {
    return java.util.stream.IntStream.range(0, count)
        .mapToObj(i -> HistoryTestItem.builder()
            .id(i + 1)
            .type(TypeTest.CONVENIENCE)
            .state(StateTest.SIGNED)
            .createdAt(LocalDateTime.now().minusDays(i))
            .build())
        .toList();
  }
}