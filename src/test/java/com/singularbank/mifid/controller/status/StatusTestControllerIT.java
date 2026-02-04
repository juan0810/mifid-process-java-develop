package com.singularbank.mifid.controller.status;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.controller.status.mapper.StateTestMapper;
import com.singularbank.mifid.controller.status.request.StateTestRequest;
import com.singularbank.mifid.controller.status.response.UpdateTestStatusResponseDTO;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.StatusTestResult;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.service.status.StatusTestService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@DisplayName("Test Status Controller Integration Tests")
class StatusTestControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/responses/{test-id}/status";

  private static final Integer TEST_ID = 123;
  private static final Integer NON_EXISTING_TEST_ID = 99999;

  private static final LocalDateTime SIGNATURE_DATE = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
  private static final LocalDateTime CANCELLATION_DATE = LocalDateTime.of(2026, 1, 15, 11, 0, 0);

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private StatusTestService statusTestService;

  @MockitoBean
  private StateTestMapper mapper;

  @Nested
  @DisplayName("Successful Updates")
  class SuccessfulUpdates {

    @Test
    @DisplayName("Should return 200 when updating to SIGNED")
    void updateToSigned() throws Exception {
      var serviceResult = createResult(StateTest.SIGNED, SIGNATURE_DATE, null);
      var dtoResponse = createResponseDto("SIGNED", SIGNATURE_DATE, null);

      when(mapper.toDomain(StateTestRequest.SIGNED)).thenReturn(StateTest.SIGNED);
      when(statusTestService.updateStatus(TEST_ID, StateTest.SIGNED)).thenReturn(serviceResult);
      when(mapper.toResponse(serviceResult)).thenReturn(dtoResponse);

      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "SIGNED"}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.testId").value(TEST_ID))
          .andExpect(jsonPath("$.status").value("SIGNED"))
          .andExpect(jsonPath("$.signatureDate").value("2026-01-15T10:30:00"))
          .andExpect(jsonPath("$.cancellationDate").doesNotExist());

      verify(statusTestService).updateStatus(TEST_ID, StateTest.SIGNED);
    }

    @Test
    @DisplayName("Should return 200 when updating to CANCELLED")
    void updateToCancelled() throws Exception {
      var serviceResult = createResult(StateTest.CANCELLED, null, CANCELLATION_DATE);
      var dtoResponse = createResponseDto("CANCELLED", null, CANCELLATION_DATE);

      when(mapper.toDomain(StateTestRequest.CANCELLED)).thenReturn(StateTest.CANCELLED);
      when(statusTestService.updateStatus(TEST_ID, StateTest.CANCELLED)).thenReturn(serviceResult);
      when(mapper.toResponse(serviceResult)).thenReturn(dtoResponse);

      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "CANCELLED"}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.testId").value(TEST_ID))
          .andExpect(jsonPath("$.status").value("CANCELLED"))
          .andExpect(jsonPath("$.signatureDate").doesNotExist())
          .andExpect(jsonPath("$.cancellationDate").value("2026-01-15T11:00:00"));
    }

    @Test
    @DisplayName("Should return 200 when updating to PENDING")
    void updateToPending() throws Exception {
      var serviceResult = createResult(StateTest.PENDING, null, null);
      var dtoResponse = createResponseDto("PENDING", null, null);

      when(mapper.toDomain(StateTestRequest.PENDING)).thenReturn(StateTest.PENDING);
      when(statusTestService.updateStatus(TEST_ID, StateTest.PENDING)).thenReturn(serviceResult);
      when(mapper.toResponse(serviceResult)).thenReturn(dtoResponse);

      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "PENDING"}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return 200 when updating to DRAFT")
    void updateToDraft() throws Exception {
      var serviceResult = createResult(StateTest.DRAFT, null, null);
      var dtoResponse = createResponseDto("DRAFT", null, null);

      when(mapper.toDomain(StateTestRequest.DRAFT)).thenReturn(StateTest.DRAFT);
      when(statusTestService.updateStatus(TEST_ID, StateTest.DRAFT)).thenReturn(serviceResult);
      when(mapper.toResponse(serviceResult)).thenReturn(dtoResponse);

      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "DRAFT"}
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("DRAFT"));
    }
  }

  @Nested
  @DisplayName("Error Responses")
  class ErrorResponses {

    @Test
    @DisplayName("Should return 404 when test not found")
    void testNotFound() throws Exception {
      when(mapper.toDomain(StateTestRequest.SIGNED)).thenReturn(StateTest.SIGNED);
      when(statusTestService.updateStatus(NON_EXISTING_TEST_ID, StateTest.SIGNED))
          .thenThrow(
              new ResourceNotFoundException("Test not found with id: " + NON_EXISTING_TEST_ID));

      mockMvc.perform(put(BASE_URL, NON_EXISTING_TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "SIGNED"}
                  """))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.message").value("Test not found with id: " + NON_EXISTING_TEST_ID));
    }

    @Test
    @DisplayName("Should return 400 when invalid transition")
    void invalidTransition() throws Exception {
      when(mapper.toDomain(StateTestRequest.DRAFT)).thenReturn(StateTest.DRAFT);
      when(statusTestService.updateStatus(TEST_ID, StateTest.DRAFT))
          .thenThrow(
              new BadRequestException("Cannot change status from SIGNED. It's a final state."));

      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "DRAFT"}
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value("Cannot change status from SIGNED. It's a final state."));
    }
  }

  @Nested
  @DisplayName("Validation Errors")
  class ValidationErrors {

    @ParameterizedTest
    @ValueSource(strings = {"EXPIRED", "SIGNA", "INVALID", "signed", "123"})
    @DisplayName("Should return 400 when invalid status value")
    void invalidStatusValue(String invalidStatus) throws Exception {
      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "%s"}
                  """.formatted(invalidStatus)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(
              org.hamcrest.Matchers.containsString(
                  "Valid values: [DRAFT, PENDING, SIGNED, CANCELLED]")));

      verifyNoInteractions(statusTestService);
    }

    @Test
    @DisplayName("Should return 400 when status is null")
    void nullStatus() throws Exception {
      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": null}
                  """))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(statusTestService);
    }

    @Test
    @DisplayName("Should return 400 when body is empty")
    void emptyBody() throws Exception {
      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(
              org.hamcrest.Matchers.containsString("status")));

      verifyNoInteractions(statusTestService);
    }

    @Test
    @DisplayName("Should return 400 when no body provided")
    void noBody() throws Exception {
      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(statusTestService);
    }

    @Test
    @DisplayName("Should return 400 when test-id is not a number")
    void invalidTestIdFormat() throws Exception {
      mockMvc.perform(put("/api/v1/test-mifid/responses/abc/status")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "SIGNED"}
                  """))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(statusTestService);
    }
  }

  @Nested
  @DisplayName("Case Sensitivity")
  class CaseSensitivity {

    @ParameterizedTest
    @EnumSource(StateTestRequest.class)
    @DisplayName("Should accept uppercase status values")
    void uppercaseStatusValues(StateTestRequest status) throws Exception {
      var domainState = StateTest.valueOf(status.name());
      var serviceResult = createResult(domainState, null, null);
      var dtoResponse = createResponseDto(status.name(), null, null);

      when(mapper.toDomain(status)).thenReturn(domainState);
      when(statusTestService.updateStatus(TEST_ID, domainState)).thenReturn(serviceResult);
      when(mapper.toResponse(serviceResult)).thenReturn(dtoResponse);

      mockMvc.perform(put(BASE_URL, TEST_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"status": "%s"}
                  """.formatted(status.name())))
          .andExpect(status().isOk());
    }
  }

// Helper methods

  private StatusTestResult createResult(StateTest status, LocalDateTime signatureDate,
      LocalDateTime cancellationDate) {
    return StatusTestResult.builder()
        .testId(TEST_ID)
        .status(status)
        .signatureDate(signatureDate)
        .cancellationDate(cancellationDate)
        .build();
  }

  private UpdateTestStatusResponseDTO createResponseDto(String status, LocalDateTime signatureDate,
      LocalDateTime cancellationDate) {
    return new UpdateTestStatusResponseDTO(TEST_ID, status, signatureDate, cancellationDate);
  }
}