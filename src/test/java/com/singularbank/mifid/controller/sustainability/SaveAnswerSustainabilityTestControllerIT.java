package com.singularbank.mifid.controller.sustainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.StoreTestAnswers;
import com.singularbank.mifid.service.convenience.SaveAnswersConvenienceTestService;
import com.singularbank.mifid.service.sustainability.SaveAnswersSustainabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
class SaveAnswerSustainabilityTestControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/sustainability";

  private static final String VALID_DOCUMENT_DNI = "12345678A";
  private static final String VALID_DOCUMENT_NIE = "X1234567Z";
  private static final String INVALID_DOCUMENT_SHORT = "1234567";
  private static final String INVALID_DOCUMENT_LONG = "1234567890";

  private static final String SERVICE_ONBOARDING = "ONBOARDING";
  private static final Short VERSION_1 = 1;

  private static final Integer SUST_QUESTION_ID_1 = 17;
  private static final Integer SUST_QUESTION_ID_2 = 18;

  private static final Integer OPTION_ID_101 = 101;
  private static final Integer OPTION_ID_102 = 102;
  private static final Integer OPTION_ID_103 = 103;
  private static final Integer OPTION_ID_202 = 202;
  private static final Integer OPTION_ID_301 = 301;

  private static final Integer RESPONSE_ID_SUCCESS = 123;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SaveAnswersSustainabilityService saveAnswersService;

  @Test
  void saveTestAnswers_whenSingleConvenienceTest_shouldReturn201() throws Exception {
    var request = createSustainabilityOnlyRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(StoreTestAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));

    verify(saveAnswersService).saveAnswers(eq(VALID_DOCUMENT_DNI), any(StoreTestAnswers.class));
  }

  @Test
  void saveTestAnswers_whenNIEDocument_shouldReturn201() throws Exception {
    var request = createSustainabilityOnlyRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_NIE), any(StoreTestAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_NIE)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));
  }

  @Test
  void saveTestAnswers_whenSelectedOptionIdIsNull_shouldReturn201() throws Exception {
    var request = new AnswersTestRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            new AnswersTestRequestDTO.QuestionResponseDTO(SUST_QUESTION_ID_1, null),
            new AnswersTestRequestDTO.QuestionResponseDTO(SUST_QUESTION_ID_1, OPTION_ID_101)
        )
    );

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(StoreTestAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));
  }

  @Test
  void saveTestAnswers_whenDocumentTooShort_shouldReturn400() throws Exception {
    var request = createSustainabilityOnlyRequest();

    mockMvc.perform(post(BASE_URL + "/{document-number}", INVALID_DOCUMENT_SHORT)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenDocumentTooLong_shouldReturn400() throws Exception {
    var request = createSustainabilityOnlyRequest();

    mockMvc.perform(post(BASE_URL + "/{document-number}", INVALID_DOCUMENT_LONG)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenServiceIsNull_shouldReturn400() throws Exception {
    var request = new AnswersTestRequestDTO(
            null,
            VERSION_1,
            List.of(createQuestionResponse(SUST_QUESTION_ID_1, OPTION_ID_101))
    );
    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenVersionIsNull_shouldReturn400() throws Exception {
    var request = new AnswersTestRequestDTO(
            SERVICE_ONBOARDING,
        null,
            List.of(createQuestionResponse(SUST_QUESTION_ID_1, OPTION_ID_101))
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenTestsEmpty_shouldReturn400() throws Exception {
    var request = new AnswersTestRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        Collections.emptyList()
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenTestsNull_shouldReturn400() throws Exception {
    var request = new AnswersTestRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        null
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenTestTypeIsInvalid_shouldReturn400() throws Exception {
    String invalidJson = """
        {
          "service": "ONBOARDING",
          "version": 1,
          "tests": [
            {
              "type": "INVALID",
              "questionResponses": [
                {"questionId": 1, "selectedOptionId": 101}
              ]
            }
          ]
        }
        """;

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenQuestionResponsesEmpty_shouldReturn400() throws Exception {
    var request = new AnswersTestRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        Collections.emptyList()
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenQuestionIdIsNull_shouldReturn400() throws Exception {
    var request = new AnswersTestRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(new AnswersTestRequestDTO.QuestionResponseDTO(null, OPTION_ID_101))
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenRequestBodyIsEmpty_shouldReturn400() throws Exception {
    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenRequestBodyIsMalformed_shouldReturn400() throws Exception {
    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("not a valid json"))
        .andExpect(status().isBadRequest());
  }

  private AnswersTestRequestDTO createSustainabilityOnlyRequest() {
    return new AnswersTestRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
                createQuestionResponse(SUST_QUESTION_ID_1, OPTION_ID_301),
                createQuestionResponse(SUST_QUESTION_ID_2, OPTION_ID_301)
        )
    );
  }


  private AnswersTestRequestDTO.QuestionResponseDTO createQuestionResponse(Integer questionId, Integer optionId) {
    return new AnswersTestRequestDTO.QuestionResponseDTO(questionId, optionId);
  }
}