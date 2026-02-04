package com.singularbank.mifid.repository.database;

import static org.assertj.core.api.Assertions.assertThat;

import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestPage;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.repository.config.AbstractRepositoryTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RespuestaCliente Repository Integration Tests")
class RespuestaClienteRepositoryIT extends AbstractRepositoryTest {

  @Autowired
  private RespuestaClienteRepository repository;

  private static final String EXISTING_IDENTITY = "09775525L";
  private static final String NON_EXISTING_IDENTITY = "99999999Z";
  private static final Integer EXISTING_TEST_ID = 135;//creo que 243?
  private static final Integer NON_EXISTING_TEST_ID = 99999;

  @Nested
  @DisplayName("findTestHistory")
  class FindTestHistory {

    @Nested
    @DisplayName("Sin filtros")
    class WithoutFilters {

      @Test
      @DisplayName("Debe devolver los 2 tests del cliente")
      void shouldReturnAllTestsForExistingClient() {
        var filter = createFilter(null, null, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTests()).hasSize(2);
        assertThat(result.getCurrentPage()).isZero();
      }

      @Test
      @DisplayName("Debe devolver página vacía para cliente inexistente")
      void shouldReturnEmptyPageForNonExistingClient() {
        var filter = createFilter(null, null, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(NON_EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTests()).isEmpty();
      }
    }

    @Nested
    @DisplayName("Filtro por tipo")
    class FilterByType {

      @Test
      @DisplayName("Debe devolver 2 tests de sostenibilidad")
      void shouldFilterBySustainability() {
        var filter = createFilter(TypeTest.SUSTAINABILITY, null, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTests())
            .hasSize(2)
            .allMatch(t -> t.getType() == TypeTest.SUSTAINABILITY);
      }

      @Test
      @DisplayName("Debe devolver vacío para conveniencia (no hay datos)")
      void shouldReturnEmptyForConvenience() {
        var filter = createFilter(TypeTest.CONVENIENCE, null, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTests()).isEmpty();
      }

      @Test
      @DisplayName("Debe devolver vacío para idoneidad (no hay datos)")
      void shouldReturnEmptyForSuitability() {
        var filter = createFilter(TypeTest.SUITABILITY, null, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTests()).isEmpty();
      }
    }

    @Nested
    @DisplayName("Filtro por estado")
    class FilterByState {

      @Test
      @DisplayName("Debe devolver los 2 tests con estado SIGNED")
      void shouldFilterBySigned() {
        var filter = createFilter(null, StateTest.SIGNED, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTests())
            .allMatch(t -> t.getState() == StateTest.SIGNED);
      }

      @Test
      @DisplayName("Debe devolver vacío para estado DRAFT")
      void shouldReturnEmptyForDraft() {
        var filter = createFilter(null, StateTest.DRAFT, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTests()).isEmpty();
      }
    }

    @Nested
    @DisplayName("Filtro por fechas")
    class FilterByDates {

      @Test
      @DisplayName("Debe devolver tests dentro del rango de fechas")
      void shouldFilterByDateRange() {
        var from = LocalDate.of(2025, 4, 1);
        var to = LocalDate.of(2025, 4, 30);
        var filter = createFilter(null, null, from, to, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTests()).allSatisfy(t -> {
          assertThat(t.getCreatedAt().toLocalDate()).isAfterOrEqualTo(from);
          assertThat(t.getCreatedAt().toLocalDate()).isBeforeOrEqualTo(to);
        });
      }

      @Test
      @DisplayName("Debe devolver vacío fuera del rango de fechas")
      void shouldReturnEmptyOutsideDateRange() {
        var from = LocalDate.of(2020, 1, 1);
        var to = LocalDate.of(2020, 12, 31);
        var filter = createFilter(null, null, from, to, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isZero();
      }
    }

    @Nested
    @DisplayName("Paginación")
    class Pagination {

      @Test
      @DisplayName("Debe devolver 1 test en primera página con size=1")
      void shouldReturnFirstPage() {
        var filter = createFilter(null, null, null, null, 0, 1);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getCurrentPage()).isZero();
        assertThat(result.getTests()).hasSize(1);
      }

      @Test
      @DisplayName("Debe devolver segunda página correctamente")
      void shouldReturnSecondPage() {
        var filter = createFilter(null, null, null, null, 1, 1);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getTests()).hasSize(1);
      }
    }

    @Nested
    @DisplayName("Filtros combinados")
    class CombinedFilters {

      @Test
      @DisplayName("Debe filtrar por tipo y estado simultáneamente")
      void shouldFilterByTypeAndState() {
        var filter = createFilter(TypeTest.SUSTAINABILITY, StateTest.SIGNED, null, null, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTests()).allSatisfy(t -> {
          assertThat(t.getType()).isEqualTo(TypeTest.SUSTAINABILITY);
          assertThat(t.getState()).isEqualTo(StateTest.SIGNED);
        });
      }

      @Test
      @DisplayName("Debe filtrar por tipo, estado y fechas")
      void shouldFilterByAllCriteria() {
        var from = LocalDate.of(2025, 1, 1);
        var to = LocalDate.of(2025, 12, 31);
        var filter = createFilter(TypeTest.SUSTAINABILITY, StateTest.SIGNED, from, to, 0, 10);

        HistoryTestPage result = repository.findTestHistory(EXISTING_IDENTITY, filter);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTests()).allSatisfy(t -> {
          assertThat(t.getType()).isEqualTo(TypeTest.SUSTAINABILITY);
          assertThat(t.getState()).isEqualTo(StateTest.SIGNED);
        });
      }
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("Debe devolver el test cuando existe")
    void shouldReturnTestWhenExists() {
      var result = repository.findById(EXISTING_TEST_ID);

      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(EXISTING_TEST_ID);
      assertThat(result.get().getClienteDni()).isEqualTo(EXISTING_IDENTITY);
    }

    @Test
    @DisplayName("Debe devolver vacío cuando no existe")
    void shouldReturnEmptyWhenNotExists() {
      var result = repository.findById(NON_EXISTING_TEST_ID);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("updateStatus")
  class UpdateStatus {

    @Test
    @DisplayName("Debe actualizar estado a SIGNED con fecha de firma")
    void shouldUpdateToSignedWithSignatureDate() {
      // Given
      var testId = EXISTING_TEST_ID;
      var signatureDate = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
      var modificationDate = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

      // When
      repository.updateStatus(testId, StateTest.SIGNED, signatureDate, null, modificationDate, null);

      // Then
      var updated = repository.findById(testId);
      assertThat(updated).isPresent();
      assertThat(updated.get().getEstado()).isEqualTo(StateTest.SIGNED);
      assertThat(updated.get().getFechaFirma()).isEqualTo(signatureDate);
    }

    @Test
    @DisplayName("Debe actualizar estado a CANCELLED con fecha de anulación")
    void shouldUpdateToCancelledWithCancellationDate() {
      // Given
      var testId = 139; // Otro test existente
      var cancellationDate = LocalDateTime.of(2026, 1, 15, 11, 0, 0);
      var modificationDate = LocalDateTime.of(2026, 1, 15, 11, 0, 0);

      // When
      repository.updateStatus(testId, StateTest.CANCELLED, null, cancellationDate,
          modificationDate, null);

      // Then
      var updated = repository.findById(testId);
      assertThat(updated).isPresent();
      assertThat(updated.get().getEstado()).isEqualTo(StateTest.CANCELLED);
      assertThat(updated.get().getFechaAnulacion()).isEqualTo(cancellationDate);
    }

    @Test
    @DisplayName("Debe actualizar estado a PENDING sin fechas adicionales")
    void shouldUpdateToPendingWithoutDates() {
      // Given
      var testId = EXISTING_TEST_ID;
      var modificationDate = LocalDateTime.of(2026, 1, 15, 12, 0, 0);

      // When
      repository.updateStatus(testId, StateTest.PENDING, null, null, modificationDate, null);

      // Then
      var updated = repository.findById(testId);
      assertThat(updated).isPresent();
      assertThat(updated.get().getEstado()).isEqualTo(StateTest.PENDING);
    }

    @Test
    @DisplayName("Debe actualizar fecha de modificación")
    void shouldUpdateModificationDate() {
      // Given
      var testId = EXISTING_TEST_ID;
      var modificationDate = LocalDateTime.of(2026, 1, 15, 14, 0, 0);

      // When
      repository.updateStatus(testId, StateTest.DRAFT, null, null, modificationDate, null);

      // Then
      var updated = repository.findById(testId);
      assertThat(updated).isPresent();
      assertThat(updated.get().getFechaModificacion()).isEqualTo(modificationDate);
    }

    @Test
    @DisplayName("Debe preservar fecha de firma existente al actualizar a CANCELLED")
    void shouldPreserveSignatureDateWhenCancelling() {
      // Given
      var testId = EXISTING_TEST_ID;
      var signatureDate = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
      var cancellationDate = LocalDateTime.of(2026, 1, 15, 10, 0, 0);
      var modificationDate = LocalDateTime.now();

      // First set to SIGNED
      repository.updateStatus(testId, StateTest.SIGNED, signatureDate, null, modificationDate, null);

      // When - update to CANCELLED preserving signature date
      repository.updateStatus(testId, StateTest.CANCELLED, signatureDate, cancellationDate,
          modificationDate, null);

      // Then
      var updated = repository.findById(testId);
      assertThat(updated).isPresent();
      assertThat(updated.get().getEstado()).isEqualTo(StateTest.CANCELLED);
      assertThat(updated.get().getFechaFirma()).isEqualTo(signatureDate);
      assertThat(updated.get().getFechaAnulacion()).isEqualTo(cancellationDate);
    }
  }

  private HistoryTestFilter createFilter(
      TypeTest type, StateTest state, LocalDate from, LocalDate to, int page, int size) {
    return HistoryTestFilter.builder()
        .type(type)
        .state(state)
        .from(from)
        .to(to)
        .page(page)
        .size(size)
        .build();
  }
}