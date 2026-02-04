package com.singularbank.mifid.controller.convenience;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WithMockUser
class ConvenienceQuestionsTestControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/convenience";
  private static final String ONBOARDING_PATH = BASE_URL + "/ONBOARDING";

  private static final String SERVICE_ONBOARDING = "ONBOARDING";
  private static final String SERVICE_WEB = "WEB";
  private static final String SERVICE_INVALID = "INVALID";

  // Version constants
  private static final int VERSION_1 = 1;
  private static final int VERSION_INVALID_NEGATIVE = -1;
  private static final int VERSION_INVALID_ZERO = 0;
  private static final int VERSION_NOT_EXISTS = 999;

  // Question IDs
  private static final int Q1_ID = 1;
  private static final int Q3_ID = 3;
  private static final int Q10_ID = 10;

  // SubQuestion IDs (Q10)
  private static final int Q10_SUB1_ID = 24;

  // Expected counts
  private static final int TOTAL_QUESTIONS = 10;
  private static final int Q1_OPTIONS_COUNT = 4;
  private static final int Q10_SUBQUESTIONS_COUNT = 11;
  private static final int Q10_SUB1_OPTIONS_COUNT = 2;

  // Expected texts
  private static final String Q1_TEXT = "¿Cuál es su nivel de estudios?";
  private static final String Q1_OPTION1_TEXT =
      "Estudios universitarios (o superiores) con un elevado componente técnico, matemático o relacionados con economía y finanzas";
  private static final String Q3_TEXT = "¿Qué es un índice bursátil (por ejemplo, el Ibex 35)?";
  private static final String Q10_TEXT_PARTIAL = "Seguidamente mostramos una tabla";
  private static final String Q10_SUB1_TEXT = "A. Depósitos bancarios e imposiciones a plazo fijo";

  // Family codes
  private static final String FAMILY_E = "E";
  private static final String FAMILY_F = "F";
  private static final String FAMILY_G = "G";
  private static final String FAMILY_JK = "J,K";

  // Question indices (0-based)
  private static final int Q1_INDEX = 0;
  private static final int Q3_INDEX = 2;
  private static final int Q4_INDEX = 3;
  private static final int Q5_INDEX = 4;
  private static final int Q6_INDEX = 5;
  private static final int Q9_INDEX = 8;
  private static final int Q10_INDEX = 9;

  // Option indices (0-based)
  private static final int OPTION_1_INDEX = 0;
  private static final int OPTION_2_INDEX = 1;
  private static final int OPTION_3_INDEX = 2;
  private static final int OPTION_4_INDEX = 3;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldGetConvenienceTestSuccessfully() throws Exception {
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").exists())
        .andExpect(jsonPath("$.version.id").value(VERSION_1))
        .andExpect(jsonPath("$.version.releaseDate").exists())
        .andExpect(jsonPath("$.questions").isArray())
        .andExpect(jsonPath("$.questions.length()").value(TOTAL_QUESTIONS))
        .andExpect(jsonPath("$.questions[" + Q1_INDEX + "].id").value(Q1_ID))
        .andExpect(jsonPath("$.questions[" + Q1_INDEX + "].text").value(Q1_TEXT))
        .andExpect(jsonPath("$.questions[" + Q1_INDEX + "].options").isArray())
        .andExpect(
            jsonPath("$.questions[" + Q1_INDEX + "].options.length()").value(Q1_OPTIONS_COUNT))
        .andExpect(
            jsonPath("$.questions[" + Q1_INDEX + "].options[" + OPTION_1_INDEX + "].id").isNumber())
        .andExpect(
            jsonPath("$.questions[" + Q1_INDEX + "].options[" + OPTION_1_INDEX + "].text").value(
                Q1_OPTION1_TEXT))
        .andExpect(
            jsonPath("$.questions[" + Q1_INDEX + "].options[" + OPTION_1_INDEX + "].correct").value(
                false))
        .andExpect(jsonPath("$.questions[" + Q3_INDEX + "].id").value(Q3_ID))
        .andExpect(jsonPath("$.questions[" + Q3_INDEX + "].text").value(Q3_TEXT))
        .andExpect(
            jsonPath("$.questions[" + Q3_INDEX + "].options[" + OPTION_4_INDEX + "].id").isNumber())
        .andExpect(
            jsonPath("$.questions[" + Q3_INDEX + "].options[" + OPTION_4_INDEX + "].correct").value(
                true));
  }

  @Test
  void shouldGetConvenienceTestWithSubQuestions() throws Exception {
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[" + Q10_INDEX + "].id").value(Q10_ID))
        .andExpect(
            jsonPath("$.questions[" + Q10_INDEX + "].text").value(containsString(Q10_TEXT_PARTIAL)))
        .andExpect(jsonPath("$.questions[" + Q10_INDEX + "].subQuestions").isArray())
        .andExpect(jsonPath("$.questions[" + Q10_INDEX + "].subQuestions.length()").value(
            Q10_SUBQUESTIONS_COUNT))
        .andExpect(jsonPath("$.questions[" + Q10_INDEX + "].subQuestions[0].id").value(Q10_SUB1_ID))
        .andExpect(
            jsonPath("$.questions[" + Q10_INDEX + "].subQuestions[0].text").value(Q10_SUB1_TEXT))
        .andExpect(
            jsonPath("$.questions[" + Q10_INDEX + "].subQuestions[0].options.length()").value(
                Q10_SUB1_OPTIONS_COUNT))
        .andExpect(jsonPath("$.questions[" + Q10_INDEX + "].subQuestions[0].options[0].id").isNumber())
        .andExpect(
            jsonPath("$.questions[" + Q10_INDEX + "].subQuestions[0].options[0].correct").value(
                false));
  }

  @Test
  void shouldGetConvenienceTestWithVersion1() throws Exception {
    performGet(ONBOARDING_PATH + "?version=" + VERSION_1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version.id").value(VERSION_1))
        .andExpect(jsonPath("$.questions.length()").value(TOTAL_QUESTIONS));
  }

  @Test
  void shouldAcceptCaseInsensitiveApplication() throws Exception {
    performGet(BASE_URL + "/onboarding")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version.id").value(VERSION_1));

    performGet(BASE_URL + "/OnBoarding")
        .andExpect(status().isOk());

    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn404WhenApplicationNotExists() throws Exception {
    performGet(BASE_URL + "/" + SERVICE_WEB)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            containsString(
                "No versions found for convenience test with application='" + SERVICE_WEB + "'")));
  }

  @Test
  void shouldReturn400WhenInvalidApplication() throws Exception {
    performGet(BASE_URL + "/" + SERVICE_INVALID)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void shouldReturn400WhenNegativeVersion() throws Exception {
    performGet(ONBOARDING_PATH + "?version=" + VERSION_INVALID_NEGATIVE)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("positive number")));
  }

  @Test
  void shouldReturn400WhenZeroVersion() throws Exception {
    performGet(ONBOARDING_PATH + "?version=" + VERSION_INVALID_ZERO)
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn404WhenVersionNotFound() throws Exception {
    performGet(ONBOARDING_PATH + "?version=" + VERSION_NOT_EXISTS)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            containsString("No convenience test found for application='"
                + SERVICE_ONBOARDING + "' and version=" + VERSION_NOT_EXISTS)));
  }

  @Test
  void shouldGetConvenienceTestWithFamilyCodes() throws Exception {
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[" + Q4_INDEX + "].familyCode").value(FAMILY_E))
        .andExpect(jsonPath("$.questions[" + Q5_INDEX + "].familyCode").value(FAMILY_F))
        .andExpect(jsonPath("$.questions[" + Q6_INDEX + "].familyCode").value(FAMILY_G))
        .andExpect(jsonPath("$.questions[" + Q9_INDEX + "].familyCode").value(FAMILY_JK));
  }

  @Test
  void shouldVerifyCorrectAnswers() throws Exception {
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.questions[" + Q3_INDEX + "].options[" + OPTION_4_INDEX + "].id").isNumber())
        .andExpect(
            jsonPath("$.questions[" + Q3_INDEX + "].options[" + OPTION_4_INDEX + "].correct").value(
                true))
        .andExpect(
            jsonPath("$.questions[" + Q4_INDEX + "].options[" + OPTION_3_INDEX + "].id").isNumber())
        .andExpect(
            jsonPath("$.questions[" + Q4_INDEX + "].options[" + OPTION_3_INDEX + "].correct").value(
                true))
        .andExpect(
            jsonPath("$.questions[" + Q5_INDEX + "].options[" + OPTION_2_INDEX + "].id").isNumber())
        .andExpect(
            jsonPath("$.questions[" + Q5_INDEX + "].options[" + OPTION_2_INDEX + "].correct").value(
                true));
  }

  @Test
  void shouldUseLatestVersionWhenVersionNotProvided() throws Exception {
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version.id").value(VERSION_1));
  }

  private ResultActions performGet(String url) throws Exception {
    return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
  }
}