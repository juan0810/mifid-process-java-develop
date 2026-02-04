package com.singularbank.mifid.service.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.StatusTestResult;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.status.impl.StatusTestServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Status Service Tests")
class TestStatusServiceTest {

  @Mock
  private RespuestaClienteRepository respuestaClienteRepository;

  private StatusTestServiceImpl service;

  private static final Integer TEST_ID = 123;
  private static final LocalDateTime EXISTING_SIGNATURE_DATE = LocalDateTime.of(2025, 6, 15, 10, 0);
  private static final LocalDateTime EXISTING_CANCELLATION_DATE = LocalDateTime.of(2025, 6, 16, 11, 0);
  private static final LocalDateTime EXISTING_CADUCITY_DATE = LocalDateTime.of(2025, 6, 15, 10, 0);

  @BeforeEach
  void setUp() {
    StateTransitionValidator transitionValidator = new StateTransitionValidator();
    service = new StatusTestServiceImpl(respuestaClienteRepository, transitionValidator);
  }

  @Nested
  @DisplayName("Edge Cases - Invalid Input")
  class EdgeCases {

    @Test
    @DisplayName("Should throw NullPointerException when testId is null")
    void nullTestId() {
      assertThatThrownBy(() -> service.updateStatus(null, StateTest.SIGNED))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("testId");

      verifyNoInteractions(respuestaClienteRepository);
    }

    @Test
    @DisplayName("Should throw NullPointerException when newStatus is null")
    void nullNewStatus() {
      assertThatThrownBy(() -> service.updateStatus(TEST_ID, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("newStatus");

      verifyNoInteractions(respuestaClienteRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when test not found")
    void testNotFound() {
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.updateStatus(TEST_ID, StateTest.SIGNED))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining(TEST_ID.toString());

      verify(respuestaClienteRepository).findById(TEST_ID);
    }
  }

  @Nested
  @DisplayName("Idempotent Behavior")
  class IdempotentBehavior {

    @ParameterizedTest
    @EnumSource(StateTest.class)
    @DisplayName("Should return success without changes when already in requested state")
    void alreadyInRequestedState(StateTest state) {
      var respuesta = createRespuesta(state);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, state);

      assertThat(result.getTestId()).isEqualTo(TEST_ID);
      assertThat(result.getStatus()).isEqualTo(state);

      verify(respuestaClienteRepository).findById(TEST_ID);
      verify(respuestaClienteRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("Valid Transitions from DRAFT")
  class ValidTransitionsFromDraft {

    @Test
    @DisplayName("Should transition from DRAFT to PENDING")
    void draftToPending() {
      var respuesta = createRespuesta(StateTest.DRAFT);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.PENDING);

      assertThat(result.getStatus()).isEqualTo(StateTest.PENDING);
      assertThat(result.getSignatureDate()).isNull();
      assertThat(result.getCancellationDate()).isNull();

      verify(respuestaClienteRepository).updateStatus(
          eq(TEST_ID), eq(StateTest.PENDING), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should transition from DRAFT to SIGNED and set signature date")
    void draftToSigned() {
      var respuesta = createRespuesta(StateTest.DRAFT);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.SIGNED);

      assertThat(result.getStatus()).isEqualTo(StateTest.SIGNED);
      assertThat(result.getSignatureDate()).isNotNull();
      assertThat(result.getCancellationDate()).isNull();

      verify(respuestaClienteRepository).updateStatus(
          eq(TEST_ID), eq(StateTest.SIGNED), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should transition from DRAFT to CANCELLED and set cancellation date")
    void draftToCancelled() {
      var respuesta = createRespuesta(StateTest.DRAFT);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.CANCELLED);

      assertThat(result.getStatus()).isEqualTo(StateTest.CANCELLED);
      assertThat(result.getSignatureDate()).isNull();
      assertThat(result.getCancellationDate()).isNotNull();

      verify(respuestaClienteRepository).updateStatus(
          eq(TEST_ID), eq(StateTest.CANCELLED), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("Valid Transitions from PENDING")
  class ValidTransitionsFromPending {

    @Test
    @DisplayName("Should transition from PENDING to SIGNED")
    void pendingToSigned() {
      var respuesta = createRespuesta(StateTest.PENDING);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.SIGNED);

      assertThat(result.getStatus()).isEqualTo(StateTest.SIGNED);
      assertThat(result.getSignatureDate()).isNotNull();

      verify(respuestaClienteRepository).updateStatus(
          eq(TEST_ID), eq(StateTest.SIGNED), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should transition from PENDING to CANCELLED")
    void pendingToCancelled() {
      var respuesta = createRespuesta(StateTest.PENDING);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.CANCELLED);

      assertThat(result.getStatus()).isEqualTo(StateTest.CANCELLED);
      assertThat(result.getCancellationDate()).isNotNull();

      verify(respuestaClienteRepository).updateStatus(
          eq(TEST_ID), eq(StateTest.CANCELLED), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("Invalid Transitions - Final States")
  class InvalidTransitionsFromFinalStates {

    @ParameterizedTest
    @EnumSource(value = StateTest.class, names = {"DRAFT", "PENDING", "CANCELLED"})
    @DisplayName("Should throw BadRequestException when transitioning from SIGNED")
    void fromSignedToOther(StateTest targetState) {
      var respuesta = createRespuesta(StateTest.SIGNED);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      assertThatThrownBy(() -> service.updateStatus(TEST_ID, targetState))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("final state");

      verify(respuestaClienteRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = StateTest.class, names = {"DRAFT", "PENDING", "SIGNED"})
    @DisplayName("Should throw BadRequestException when transitioning from CANCELLED")
    void fromCancelledToOther(StateTest targetState) {
      var respuesta = createRespuesta(StateTest.CANCELLED);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      assertThatThrownBy(() -> service.updateStatus(TEST_ID, targetState))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("final state");

      verify(respuestaClienteRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = StateTest.class, names = {"DRAFT", "PENDING", "SIGNED", "CANCELLED"})
    @DisplayName("Should throw BadRequestException when transitioning from EXPIRED")
    void fromExpiredToOther(StateTest targetState) {
      var respuesta = createRespuesta(StateTest.EXPIRED);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      assertThatThrownBy(() -> service.updateStatus(TEST_ID, targetState))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("final state");

      verify(respuestaClienteRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("Invalid Transitions - Non-transitionable targets")
  class NonTransitionableTargets {

    @ParameterizedTest
    @EnumSource(value = StateTest.class, names = {"DRAFT", "PENDING", "SIGNED", "CANCELLED"})
    @DisplayName("Should throw BadRequestException when transitioning to EXPIRED from any state")
    void toExpiredFromAny(StateTest currentState) {
      var respuesta = createRespuesta(currentState);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      assertThatThrownBy(() -> service.updateStatus(TEST_ID, StateTest.EXPIRED))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Cannot manually transition to EXPIRED");

      verify(respuestaClienteRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }
  }

  @Nested
  @DisplayName("Invalid Transitions - PENDING restrictions")
  class InvalidTransitionsFromPending {

    @Test
    @DisplayName("Should throw BadRequestException when transitioning from PENDING to DRAFT")
    void pendingToDraft() {
      var respuesta = createRespuesta(StateTest.PENDING);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      assertThatThrownBy(() -> service.updateStatus(TEST_ID, StateTest.DRAFT))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Invalid transition");

      verify(respuestaClienteRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }
  }


  @Nested
  @DisplayName("Caducity Dates")
  class SwitchCaducityDates {

    @Test
    @DisplayName("Should replace existing state, caducity and signature date when transitioning to SIGNED")
    void changeCaducityDateAndSignatureDateWhenSigning() {
      var expectedCaducityCalculated = LocalDate.now().plusYears(3);
      var respuesta = createRespuestaWithDates(StateTest.DRAFT, EXISTING_SIGNATURE_DATE, null, expectedCaducityCalculated);
      when(respuestaClienteRepository.findById(TEST_ID))
              .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.SIGNED);

      assertThat(result.getSignatureDate()).isNotNull();
      assertThat(result.getCaducityDate()).isNotNull();
      assertThat(result.getCaducityDate().getYear()).isEqualTo(expectedCaducityCalculated.getYear());
    }
  }

  @Nested
  @DisplayName("Preserve Existing Dates")
  class PreserveExistingDates {

    @Test
    @DisplayName("Should preserve existing signature date when transitioning to CANCELLED")
    void preserveSignatureDateWhenCancelling() {
      var respuesta = createRespuestaWithDates(StateTest.DRAFT, EXISTING_SIGNATURE_DATE, null, null);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.CANCELLED);

      verify(respuestaClienteRepository).updateStatus(eq(TEST_ID),
              eq(StateTest.CANCELLED),
              eq(EXISTING_SIGNATURE_DATE),
              any(),
              any(),
              any()
      );
      assertThat(result.getSignatureDate()).isEqualTo(EXISTING_SIGNATURE_DATE);
      assertThat(result.getCancellationDate()).isNotNull();
    }

    @Test
    @DisplayName("Should preserve existing cancellation date when already set")
    void preserveCancellationDateWhenAlreadySet() {
      var respuesta = createRespuestaWithDates(StateTest.DRAFT, null, EXISTING_CANCELLATION_DATE, null);
      when(respuestaClienteRepository.findById(TEST_ID))
          .thenReturn(Optional.of(respuesta));

      StatusTestResult result = service.updateStatus(TEST_ID, StateTest.SIGNED);

      assertThat(result.getSignatureDate()).isNotNull();
      assertThat(result.getCancellationDate()).isEqualTo(EXISTING_CANCELLATION_DATE);
    }
  }

  private RespuestaCliente createRespuesta(StateTest state) {
    return RespuestaCliente.builder()
        .id(TEST_ID)
        .estado(state)
        .fechaFirma(null)
        .fechaAnulacion(null)
        .build();
  }

  private RespuestaCliente createRespuestaWithDates(StateTest state,
      LocalDateTime signatureDate, LocalDateTime cancellationDate, LocalDate fechaCaducidad) {
    return RespuestaCliente.builder()
        .id(TEST_ID)
        .estado(state)
        .fechaFirma(signatureDate)
        .fechaAnulacion(cancellationDate)
        .fechaCaducidad(fechaCaducidad)
        .build();
  }
}