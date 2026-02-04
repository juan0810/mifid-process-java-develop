package com.singularbank.mifid.controller.convenience;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.controller.helpers.dto.AlertsTestResponseDTO;
import com.singularbank.mifid.controller.helpers.dto.AlertsTestResponseDTO.AlertDTO;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO.QuestionResponseDTO;
import com.singularbank.mifid.controller.helpers.mapper.AnswersTestRequestMapper;
import com.singularbank.mifid.controller.helpers.mapper.ConvenienceResultMapper;
import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.entity.StoreTestAnswers;
import com.singularbank.mifid.service.convenience.ConvenienceAlertCalculationService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
@DisplayName("Convenience Test Alerts Controller Integration Tests")
class ConvenienceTestAlertsControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/convenience/alerts";

  private static final String VALID_DOCUMENT_DNI = "12345678A";
  private static final String VALID_DOCUMENT_NIE = "X1234567Z";
  private static final String INVALID_DOCUMENT_SHORT = "1234567";
  private static final String INVALID_DOCUMENT_LONG = "1234567890";

  private static final String SERVICE_ONBOARDING = "ONBOARDING";
  private static final Short VERSION_1 = 1;

  private static final Integer QUESTION_ID_1 = 1;
  private static final Integer QUESTION_ID_2 = 2;
  private static final Integer QUESTION_ID_3 = 3;

  private static final Integer OPTION_ID_A = 1;
  private static final Integer OPTION_ID_B = 2;
  private static final Integer OPTION_ID_C = 3;

  private static final String RESULT_CONVENIENT = "Conveniente";
  private static final String RESULT_NOT_CONVENIENT = "No conveniente";

  private static final String FAMILY_A = "A";
  private static final String FAMILY_B = "B";
  private static final String FAMILY_C = "C";
  private static final String FAMILY_D = "D";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ConvenienceAlertCalculationService convenienceAlertCalculationService;

  @MockitoBean
  private AnswersTestRequestMapper answersTestRequestMapper;

  @MockitoBean
  private ConvenienceResultMapper convenienceResultMapper;

  @Nested
  @DisplayName("Happy Path Tests")
  class HappyPathTests {

    @Test
    @DisplayName("Should return 200 with no alerts when all families convenient")
    void allFamiliesConvenient() throws Exception {
      var request = createValidRequest();
      var domainRequest = createDomainRequest();
      var serviceResponse = ConvenienceResult.builder()
          .convenientFamilies(Set.of(FAMILY_A, FAMILY_B, FAMILY_C))
          .notConvenientFamilies(Set.of())
          .familiesWithAlerts(Set.of())
          .alerts(Collections.emptyList())
          .build();
      var dtoResponse = AlertsTestResponseDTO.builder()
          .result(RESULT_CONVENIENT)
          .alerts(Collections.emptyList())
          .build();

      when(answersTestRequestMapper.toDomain(any(AnswersTestRequestDTO.class)))
          .thenReturn(domainRequest);
      when(convenienceAlertCalculationService.calculateAlerts(eq(VALID_DOCUMENT_DNI),
          any(StoreTestAnswers.class)))
          .thenReturn(serviceResponse);
      when(convenienceResultMapper.toDto(serviceResponse)).thenReturn(dtoResponse);

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value(RESULT_CONVENIENT))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.alerts").isEmpty());

      verify(answersTestRequestMapper).toDomain(any(AnswersTestRequestDTO.class));
      verify(convenienceAlertCalculationService).calculateAlerts(eq(VALID_DOCUMENT_DNI),
          any(StoreTestAnswers.class));
      verify(convenienceResultMapper).toDto(serviceResponse);
    }

    @Test
    @DisplayName("Should return 200 with family alerts when some families not convenient")
    void someFamiliesNotConvenient() throws Exception {
      var request = createValidRequest();
      var domainRequest = createDomainRequest();
      var serviceResponse = ConvenienceResult.builder()
          .convenientFamilies(Set.of(FAMILY_A, FAMILY_B))
          .notConvenientFamilies(Set.of(FAMILY_C, FAMILY_D))
          .familiesWithAlerts(Set.of())
          .alerts(Collections.emptyList())
          .build();
      var dtoResponse = AlertsTestResponseDTO.builder()
          .result(RESULT_NOT_CONVENIENT)
          .alerts(List.of(
              createAlertDTO(FAMILY_C, "Renta variable cotizada"),
              createAlertDTO(FAMILY_D, "Fondos de inversión UCITS...")
          ))
          .build();

      when(answersTestRequestMapper.toDomain(any(AnswersTestRequestDTO.class)))
          .thenReturn(domainRequest);
      when(convenienceAlertCalculationService.calculateAlerts(eq(VALID_DOCUMENT_DNI),
          any(StoreTestAnswers.class)))
          .thenReturn(serviceResponse);
      when(convenienceResultMapper.toDto(serviceResponse)).thenReturn(dtoResponse);

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value(RESULT_NOT_CONVENIENT))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.alerts.length()").value(2))
          .andExpect(jsonPath("$.alerts[0].code").value(FAMILY_C))
          .andExpect(jsonPath("$.alerts[0].description").exists())
          .andExpect(jsonPath("$.alerts[1].code").value(FAMILY_D))
          .andExpect(jsonPath("$.alerts[1].description").exists());
    }

    @Test
    @DisplayName("Should return 200 with multiple question responses")
    void multipleQuestionResponses() throws Exception {
      var request = createRequestWithMultipleResponses();
      var domainRequest = createDomainRequest();
      var serviceResponse = ConvenienceResult.builder()
          .convenientFamilies(Set.of(FAMILY_A, FAMILY_B))
          .notConvenientFamilies(Set.of())
          .familiesWithAlerts(Set.of())
          .alerts(Collections.emptyList())
          .build();
      var dtoResponse = AlertsTestResponseDTO.builder()
          .result(RESULT_CONVENIENT)
          .alerts(Collections.emptyList())
          .build();

      when(answersTestRequestMapper.toDomain(any(AnswersTestRequestDTO.class)))
          .thenReturn(domainRequest);
      when(convenienceAlertCalculationService.calculateAlerts(eq(VALID_DOCUMENT_DNI),
          any(StoreTestAnswers.class)))
          .thenReturn(serviceResponse);
      when(convenienceResultMapper.toDto(serviceResponse)).thenReturn(dtoResponse);

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value(RESULT_CONVENIENT))
          .andExpect(jsonPath("$.alerts").isArray());
    }

    @Test
    @DisplayName("Should return 200 with NIE document")
    void nieDocument() throws Exception {
      var request = createValidRequest();
      var domainRequest = createDomainRequest();
      var serviceResponse = ConvenienceResult.builder()
          .convenientFamilies(Set.of(FAMILY_A))
          .notConvenientFamilies(Set.of())
          .familiesWithAlerts(Set.of())
          .alerts(Collections.emptyList())
          .build();
      var dtoResponse = AlertsTestResponseDTO.builder()
          .result(RESULT_CONVENIENT)
          .alerts(Collections.emptyList())
          .build();

      when(answersTestRequestMapper.toDomain(any(AnswersTestRequestDTO.class)))
          .thenReturn(domainRequest);
      when(convenienceAlertCalculationService.calculateAlerts(eq(VALID_DOCUMENT_NIE),
          any(StoreTestAnswers.class)))
          .thenReturn(serviceResponse);
      when(convenienceResultMapper.toDto(serviceResponse)).thenReturn(dtoResponse);

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_NIE)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value(RESULT_CONVENIENT));
    }

    @Test
    @DisplayName("Should return 200 when selectedOptionId is null")
    void selectedOptionIdNull() throws Exception {
      var request = AnswersTestRequestDTO.builder()
          .service(SERVICE_ONBOARDING)
          .version(VERSION_1)
          .questionResponses(List.of(createQuestionResponse(QUESTION_ID_1, null)))
          .build();

      var domainRequest = createDomainRequest();
      var serviceResponse = ConvenienceResult.builder()
          .convenientFamilies(Set.of(FAMILY_A))
          .notConvenientFamilies(Set.of())
          .familiesWithAlerts(Set.of())
          .alerts(Collections.emptyList())
          .build();
      var dtoResponse = AlertsTestResponseDTO.builder()
          .result(RESULT_CONVENIENT)
          .alerts(Collections.emptyList())
          .build();

      when(answersTestRequestMapper.toDomain(any(AnswersTestRequestDTO.class)))
          .thenReturn(domainRequest);
      when(convenienceAlertCalculationService.calculateAlerts(eq(VALID_DOCUMENT_DNI),
          any(StoreTestAnswers.class)))
          .thenReturn(serviceResponse);
      when(convenienceResultMapper.toDto(serviceResponse)).thenReturn(dtoResponse);

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value(RESULT_CONVENIENT));
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @DisplayName("Should return 400 when document too short")
    void documentTooShort() throws Exception {
      var request = createValidRequest();

      mockMvc.perform(post(BASE_URL + "/{document-number}", INVALID_DOCUMENT_SHORT)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when document too long")
    void documentTooLong() throws Exception {
      var request = createValidRequest();

      mockMvc.perform(post(BASE_URL + "/{document-number}", INVALID_DOCUMENT_LONG)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when service is null")
    void serviceIsNull() throws Exception {
      var request = AnswersTestRequestDTO.builder()
          .service(null)
          .version(VERSION_1)
          .questionResponses(List.of(createQuestionResponse(QUESTION_ID_1, OPTION_ID_A)))
          .build();

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when version is null")
    void versionIsNull() throws Exception {
      var request = AnswersTestRequestDTO.builder()
          .service(SERVICE_ONBOARDING)
          .version(null)
          .questionResponses(List.of(createQuestionResponse(QUESTION_ID_1, OPTION_ID_A)))
          .build();

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when questionResponses is empty")
    void questionResponsesEmpty() throws Exception {
      var request = AnswersTestRequestDTO.builder()
          .service(SERVICE_ONBOARDING)
          .version(VERSION_1)
          .questionResponses(Collections.emptyList())
          .build();

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when questionResponses is null")
    void questionResponsesNull() throws Exception {
      var request = AnswersTestRequestDTO.builder()
          .service(SERVICE_ONBOARDING)
          .version(VERSION_1)
          .questionResponses(null)
          .build();

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when questionId is null")
    void questionIdIsNull() throws Exception {
      var request = AnswersTestRequestDTO.builder()
          .service(SERVICE_ONBOARDING)
          .version(VERSION_1)
          .questionResponses(List.of(
              QuestionResponseDTO.builder()
                  .questionId(null)
                  .selectedOptionId(OPTION_ID_A)
                  .build()
          ))
          .build();

      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is empty")
    void requestBodyEmpty() throws Exception {
      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is malformed")
    void requestBodyMalformed() throws Exception {
      mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{invalid json}"))
          .andExpect(status().isBadRequest());
    }
  }

  private AnswersTestRequestDTO createValidRequest() {
    return AnswersTestRequestDTO.builder()
        .service(SERVICE_ONBOARDING)
        .version(VERSION_1)
        .questionResponses(List.of(
            createQuestionResponse(QUESTION_ID_1, OPTION_ID_A)
        ))
        .build();
  }

  private AnswersTestRequestDTO createRequestWithMultipleResponses() {
    return AnswersTestRequestDTO.builder()
        .service(SERVICE_ONBOARDING)
        .version(VERSION_1)
        .questionResponses(List.of(
            createQuestionResponse(QUESTION_ID_1, OPTION_ID_A),
            createQuestionResponse(QUESTION_ID_2, OPTION_ID_B),
            createQuestionResponse(QUESTION_ID_3, OPTION_ID_C)
        ))
        .build();
  }

  private QuestionResponseDTO createQuestionResponse(Integer questionId, Integer optionId) {
    return QuestionResponseDTO.builder()
        .questionId(questionId)
        .selectedOptionId(optionId)
        .build();
  }

  private StoreTestAnswers createDomainRequest() {
    return StoreTestAnswers.builder()
        .service(SERVICE_ONBOARDING)
        .version(VERSION_1)
        .questionResponses(List.of(
            StoreTestAnswers.QuestionResponse.builder()
                .questionId(QUESTION_ID_1)
                .selectedOptionId(OPTION_ID_A)
                .build()
        ))
        .build();
  }

  private AlertDTO createAlertDTO(String code, String description) {
    return AlertDTO.builder()
        .code(code)
        .description(description)
        .build();
  }
}