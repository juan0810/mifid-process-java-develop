package com.singularbank.mifid.controller.sustainability;

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
class SustainabilityTestGetQuestionsControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/sustainability";
  private static final String ONBOARDING_PATH = BASE_URL + "/ONBOARDING";

  private static final String SERVICE_ONBOARDING = "ONBOARDING";
  private static final String SERVICE_WEB = "WEB";
  private static final String SERVICE_INVALID = "INVALID";

  // Question IDs
  private static final int Q1_WANTS_SUSTAINABILITY_ID = 17;
  private static final int Q0_TITLE_ID = 23;
  private static final int Q2_SUSTAINABLE_INVESTMENT_ID = 18;
  private static final int Q3_EU_TAXONOMY_ID = 19;
  private static final int Q4_PAI_ID = 20;
  private static final int Q5_PORTFOLIO_PERCENTAGE_ID = 21;

  // Note: Option IDs are auto-generated and may vary between environments.
  // Tests verify option existence by text instead of ID.

  // Expected texts
  private static final String Q1_TEXT =
      "¿Quiere que se tenga en cuenta la sostenibilidad a la hora de plantearle alternativas de inversión?";
  private static final String Q0_TITLE_TEXT =
      "Seleccione sus Preferencias de sostenibilidad";
  private static final String Q2_TEXT =
      "Productos con inversiones sostenibles de acuerdo con el punto 1) de las Definiciones";
  private static final String OPTION_NO_TEXT = "No";
  private static final String OPTION_YES_INVEST_TEXT = "Sí, deseo invertir en un producto sostenible";
  private static final String OPTION_YES_ADVISOR_TEXT = "Sí, deseo que mi banquero";

  // Expected counts
  private static final int MAIN_QUESTION_OPTIONS_COUNT = 3;
  private static final int SUB_QUESTIONS_COUNT = 5;
  private static final int Q2_OPTIONS_COUNT = 3;
  private static final int Q3_OPTIONS_COUNT = 3;
  private static final int Q4_OPTIONS_COUNT = 5;
  private static final int Q5_OPTIONS_COUNT = 4;

  private static final int VERSION_1 = 1;
  private static final int VERSION_INVALID_ZERO = 0;
  private static final int VERSION_INVALID_NEGATIVE = -1;
  private static final int VERSION_NOT_EXISTS = 999;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldGetSustainabilityTestSuccessfully() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").exists())
        .andExpect(jsonPath("$.version.id").value(VERSION_1))
        .andExpect(jsonPath("$.questions").isArray())
        .andExpect(jsonPath("$.questions.length()").value(1))
        .andExpect(jsonPath("$.questions[0].id").value(Q1_WANTS_SUSTAINABILITY_ID))
        .andExpect(jsonPath("$.questions[0].text").value(Q1_TEXT))
        .andExpect(jsonPath("$.questions[0].options").isArray())
        .andExpect(jsonPath("$.questions[0].options.length()").value(MAIN_QUESTION_OPTIONS_COUNT))
        .andExpect(jsonPath("$.questions[0].options[0].id").exists())
        .andExpect(jsonPath("$.questions[0].options[0].text").value(OPTION_NO_TEXT))
        .andExpect(jsonPath("$.questions[0].options[0].correct").value(false))
        .andExpect(jsonPath("$.questions[0].options[1].id").exists())
        .andExpect(jsonPath("$.questions[0].options[2].id").exists());
  }

  @Test
  void shouldGetSustainabilityTestWithNestedQuestions() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[0].id").value(Q1_WANTS_SUSTAINABILITY_ID))
        .andExpect(jsonPath("$.questions[0].subQuestions").isArray())
        .andExpect(jsonPath("$.questions[0].subQuestions.length()").value(SUB_QUESTIONS_COUNT))
        .andExpect(jsonPath("$.questions[0].subQuestions[0].id").value(Q0_TITLE_ID))
        .andExpect(
            jsonPath("$.questions[0].subQuestions[0].text").value(containsString(Q0_TITLE_TEXT)))
        .andExpect(jsonPath("$.questions[0].subQuestions[0].options").isEmpty())
        .andExpect(
            jsonPath("$.questions[0].subQuestions[1].id").value(Q2_SUSTAINABLE_INVESTMENT_ID))
        .andExpect(jsonPath("$.questions[0].subQuestions[1].text").value(Q2_TEXT))
        .andExpect(
            jsonPath("$.questions[0].subQuestions[1].options.length()").value(Q2_OPTIONS_COUNT))
        .andExpect(jsonPath("$.questions[0].subQuestions[2].id").value(Q3_EU_TAXONOMY_ID))
        .andExpect(jsonPath("$.questions[0].subQuestions[3].id").value(Q4_PAI_ID))
        .andExpect(jsonPath("$.questions[0].subQuestions[4].id").value(Q5_PORTFOLIO_PERCENTAGE_ID));
  }

  @Test
  void shouldGetSustainabilityTestWithVersion1() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH + "?version=" + VERSION_1)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version.id").value(VERSION_1))
        .andExpect(jsonPath("$.questions.length()").value(1));
  }

  @Test
  void shouldAcceptCaseInsensitiveApplication() throws Exception {
    // When & Then
    performGet(BASE_URL + "/onboarding")
        .andExpect(status().isOk());

    performGet(BASE_URL + "/OnBoarding")
        .andExpect(status().isOk());

    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn404WhenApplicationNotExists() throws Exception {
    // When & Then
    performGet(BASE_URL + "/" + SERVICE_WEB)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            containsString(
                "No versions found for sustainability test with application='" + SERVICE_WEB
                    + "'")));
  }

  @Test
  void shouldReturn400WhenInvalidApplication() throws Exception {
    // When & Then
    performGet(BASE_URL + "/" + SERVICE_INVALID)
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenInvalidVersion() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH + "?version=" + VERSION_INVALID_ZERO)
        .andExpect(status().isBadRequest());

    performGet(ONBOARDING_PATH + "?version=" + VERSION_INVALID_NEGATIVE)
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn404WhenVersionNotExists() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH + "?version=" + VERSION_NOT_EXISTS)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            containsString("No sustainability test found for application='"
                + SERVICE_ONBOARDING + "' and version=" + VERSION_NOT_EXISTS)));
  }

  @Test
  void shouldVerifySubQuestionOptions() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.questions[0].subQuestions[1].options.length()").value(Q2_OPTIONS_COUNT))
        .andExpect(jsonPath("$.questions[0].subQuestions[1].options[0].text").value(
            "No me interesa establecer ningún porcentaje mínimo."))
        .andExpect(
            jsonPath("$.questions[0].subQuestions[2].options.length()").value(Q3_OPTIONS_COUNT))
        .andExpect(
            jsonPath("$.questions[0].subQuestions[3].options.length()").value(Q4_OPTIONS_COUNT))
        .andExpect(
            jsonPath("$.questions[0].subQuestions[4].options.length()").value(Q5_OPTIONS_COUNT));
  }

  @Test
  void shouldVerifyMainQuestionOptions() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[0].options.length()").value(MAIN_QUESTION_OPTIONS_COUNT))
        .andExpect(jsonPath("$.questions[0].options[0].id").exists())
        .andExpect(jsonPath("$.questions[0].options[0].text").value(OPTION_NO_TEXT))
        .andExpect(jsonPath("$.questions[0].options[1].id").exists())
        .andExpect(jsonPath("$.questions[0].options[1].text").value(
            containsString(OPTION_YES_INVEST_TEXT)))
        .andExpect(jsonPath("$.questions[0].options[2].id").exists())
        .andExpect(jsonPath("$.questions[0].options[2].text").value(
            containsString(OPTION_YES_ADVISOR_TEXT)));
  }

  @Test
  void shouldUseLatestVersionWhenVersionNotProvided() throws Exception {
    // When & Then
    performGet(ONBOARDING_PATH)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version.id").value(VERSION_1));
  }

  private ResultActions performGet(String url) throws Exception {
    return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
  }
}