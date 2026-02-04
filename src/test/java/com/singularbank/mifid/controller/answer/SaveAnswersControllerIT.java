package com.singularbank.mifid.controller.answer;

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
import com.singularbank.mifid.controller.answer.request.SaveAnswersRequestDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.SaveAnswers;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.answer.SaveAnswersService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
class SaveAnswersControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/answers";

  private static final String VALID_DOCUMENT_DNI = "12345678A";
  private static final String VALID_DOCUMENT_NIE = "X1234567Z";
  private static final String INVALID_DOCUMENT_SHORT = "1234567";
  private static final String INVALID_DOCUMENT_LONG = "1234567890";

  private static final String SERVICE_ONBOARDING = "ONBOARDING";
  private static final Short VERSION_1 = 1;

  private static final Integer CONV_QUESTION_ID_1 = 1;
  private static final Integer CONV_QUESTION_ID_2 = 2;
  private static final Integer CONV_QUESTION_ID_3 = 3;

  private static final Integer SUIT_QUESTION_ID_1 = 11;
  private static final Integer SUIT_QUESTION_ID_2 = 121;
  private static final Integer SUIT_QUESTION_ID_3 = 13;

  private static final Integer SUST_QUESTION_ID_1 = 17;
  private static final Integer SUST_QUESTION_ID_2 = 18;

  private static final Integer OPTION_ID_101 = 101;
  private static final Integer OPTION_ID_102 = 102;
  private static final Integer OPTION_ID_103 = 103;
  private static final Integer OPTION_ID_201 = 201;
  private static final Integer OPTION_ID_202 = 202;
  private static final Integer OPTION_ID_301 = 301;

  private static final Integer RESPONSE_ID_SUCCESS = 123;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SaveAnswersService saveAnswersService;

  @Test
  void saveTestAnswers_whenSingleConvenienceTest_shouldReturn201() throws Exception {
    var request = createConvenienceOnlyRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));

    verify(saveAnswersService).saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class));
  }

  @Test
  void saveTestAnswers_whenSingleSuitabilityTest_shouldReturn201() throws Exception {
    var request = createSuitabilityOnlyRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));
  }

  @Test
  void saveTestAnswers_whenSingleSustainabilityTest_shouldReturn201() throws Exception {
    var request = createSustainabilityOnlyRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));
  }

  @Test
  void saveTestAnswers_whenConvenienceAndSuitability_shouldReturn201() throws Exception {
    var request = createConvenienceAndSuitabilityRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));
  }

  @Test
  void saveTestAnswers_whenAllThreeTests_shouldReturn201() throws Exception {
    var request = createAllTestsRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class)))
        .thenReturn(mockResponse);

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.responseClientId").value(RESPONSE_ID_SUCCESS));
  }

  @Test
  void saveTestAnswers_whenNIEDocument_shouldReturn201() throws Exception {
    var request = createConvenienceOnlyRequest();

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_NIE), any(SaveAnswers.class)))
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
    var request = new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            new SaveAnswersRequestDTO.BlockTest(
                TypeTest.CONVENIENCE,
                List.of(
                    new SaveAnswersRequestDTO.QuestionResponseDTO(CONV_QUESTION_ID_1, null),
                    new SaveAnswersRequestDTO.QuestionResponseDTO(CONV_QUESTION_ID_2, OPTION_ID_101)
                )
            )
        )
    );

    TestResponseCreatedDTO mockResponse = new TestResponseCreatedDTO(RESPONSE_ID_SUCCESS, null);

    when(saveAnswersService.saveAnswers(eq(VALID_DOCUMENT_DNI), any(SaveAnswers.class)))
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
    var request = createConvenienceOnlyRequest();

    mockMvc.perform(post(BASE_URL + "/{document-number}", INVALID_DOCUMENT_SHORT)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenDocumentTooLong_shouldReturn400() throws Exception {
    var request = createConvenienceOnlyRequest();

    mockMvc.perform(post(BASE_URL + "/{document-number}", INVALID_DOCUMENT_LONG)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenServiceIsNull_shouldReturn400() throws Exception {
    var request = new SaveAnswersRequestDTO(
        null,
        VERSION_1,
        List.of(createConvenienceBlockTest())
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenVersionIsNull_shouldReturn400() throws Exception {
    var request = new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        null,
        List.of(createConvenienceBlockTest())
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenTestsEmpty_shouldReturn400() throws Exception {
    var request = new SaveAnswersRequestDTO(
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
    var request = new SaveAnswersRequestDTO(
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
  void saveTestAnswers_whenTestTypeIsNull_shouldReturn400() throws Exception {
    var request = new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            new SaveAnswersRequestDTO.BlockTest(
                null,
                List.of(createQuestionResponse(CONV_QUESTION_ID_1, OPTION_ID_101))
            )
        )
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
    var request = new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            new SaveAnswersRequestDTO.BlockTest(
                TypeTest.CONVENIENCE,
                Collections.emptyList()
            )
        )
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenQuestionResponsesNull_shouldReturn400() throws Exception {
    var request = new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            new SaveAnswersRequestDTO.BlockTest(TypeTest.CONVENIENCE, null)
        )
    );

    mockMvc.perform(post(BASE_URL + "/{document-number}", VALID_DOCUMENT_DNI)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveTestAnswers_whenQuestionIdIsNull_shouldReturn400() throws Exception {
    var request = new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            new SaveAnswersRequestDTO.BlockTest(
                TypeTest.CONVENIENCE,
                List.of(new SaveAnswersRequestDTO.QuestionResponseDTO(null, OPTION_ID_101))
            )
        )
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

  private SaveAnswersRequestDTO createConvenienceOnlyRequest() {
    return new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(createConvenienceBlockTest())
    );
  }

  private SaveAnswersRequestDTO createSuitabilityOnlyRequest() {
    return new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(createSuitabilityBlockTest())
    );
  }

  private SaveAnswersRequestDTO createSustainabilityOnlyRequest() {
    return new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(createSustainabilityBlockTest())
    );
  }

  private SaveAnswersRequestDTO createConvenienceAndSuitabilityRequest() {
    return new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            createConvenienceBlockTest(),
            createSuitabilityBlockTest()
        )
    );
  }

  private SaveAnswersRequestDTO createAllTestsRequest() {
    return new SaveAnswersRequestDTO(
        SERVICE_ONBOARDING,
        VERSION_1,
        List.of(
            createConvenienceBlockTest(),
            createSuitabilityBlockTest(),
            createSustainabilityBlockTest()
        )
    );
  }

  private SaveAnswersRequestDTO.BlockTest createConvenienceBlockTest() {
    return new SaveAnswersRequestDTO.BlockTest(
        TypeTest.CONVENIENCE,
        List.of(
            createQuestionResponse(CONV_QUESTION_ID_1, OPTION_ID_101),
            createQuestionResponse(CONV_QUESTION_ID_2, OPTION_ID_102),
            createQuestionResponse(CONV_QUESTION_ID_3, OPTION_ID_103)
        )
    );
  }

  private SaveAnswersRequestDTO.BlockTest createSuitabilityBlockTest() {
    return new SaveAnswersRequestDTO.BlockTest(
        TypeTest.SUITABILITY,
        List.of(
            createQuestionResponse(SUIT_QUESTION_ID_1, OPTION_ID_201),
            createQuestionResponse(SUIT_QUESTION_ID_2, OPTION_ID_202),
            createQuestionResponse(SUIT_QUESTION_ID_3, OPTION_ID_201)
        )
    );
  }

  private SaveAnswersRequestDTO.BlockTest createSustainabilityBlockTest() {
    return new SaveAnswersRequestDTO.BlockTest(
        TypeTest.SUSTAINABILITY,
        List.of(
            createQuestionResponse(SUST_QUESTION_ID_1, OPTION_ID_301),
            createQuestionResponse(SUST_QUESTION_ID_2, OPTION_ID_301)
        )
    );
  }

  private SaveAnswersRequestDTO.QuestionResponseDTO createQuestionResponse(
      Integer questionId, Integer optionId) {
    return new SaveAnswersRequestDTO.QuestionResponseDTO(questionId, optionId);
  }
}