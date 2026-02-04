package com.singularbank.mifid.controller.suitability;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
class SuitabilityTestGetQuestionsControllerIT extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldGetSuitabilityTestSuccessfully() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").exists())
        .andExpect(jsonPath("$.version.id").value(1))
        .andExpect(jsonPath("$.questions").isArray())
        .andExpect(jsonPath("$.questions.length()").value(6))
        .andExpect(jsonPath("$.questions[0].id").value(11))
        .andExpect(jsonPath("$.questions[0].text").value(
            "¿Qué parte de las rentas de sus inversiones podría necesitar para cubrir sus gastos corrientes?"))
        .andExpect(jsonPath("$.questions[0].options").isArray())
        .andExpect(jsonPath("$.questions[0].options.length()").value(5));
  }

  @Test
  void shouldGetSuitabilityTestWithSubQuestions() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[1].id").value(12))
        .andExpect(jsonPath("$.questions[1].text").value(
            "¿Qué porcentaje representa su inversión sobre su patrimonio global?"))
        .andExpect(jsonPath("$.questions[1].subQuestions").isArray())
        .andExpect(jsonPath("$.questions[1].subQuestions.length()").value(2))
        .andExpect(jsonPath("$.questions[1].subQuestions[0].id").value(35))
        .andExpect(jsonPath("$.questions[1].subQuestions[0].text").value("Inversión en SB (%)"))
        .andExpect(jsonPath("$.questions[1].subQuestions[1].id").value(36))
        .andExpect(jsonPath("$.questions[1].subQuestions[1].text").value("Inversión total (%)"));
  }

  @Test
  void shouldGetSuitabilityTestWithPregunta16SubQuestions() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questions[5].id").value(16))
        .andExpect(jsonPath("$.questions[5].subQuestions").isArray())
        .andExpect(jsonPath("$.questions[5].subQuestions.length()").value(3))
        .andExpect(jsonPath("$.questions[5].subQuestions[0].id").value(37))
        .andExpect(jsonPath("$.questions[5].subQuestions[0].text").value("Un mes"))
        .andExpect(jsonPath("$.questions[5].subQuestions[1].id").value(38))
        .andExpect(jsonPath("$.questions[5].subQuestions[1].text").value("Un año"))
        .andExpect(jsonPath("$.questions[5].subQuestions[2].id").value(39))
        .andExpect(jsonPath("$.questions[5].subQuestions[2].text").value("Tres años"));
  }

  @Test
  void shouldGetSuitabilityTestWithVersion1() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING?version=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version.id").value(1))
        .andExpect(jsonPath("$.questions.length()").value(6));
  }

  @Test
  void shouldAcceptCaseInsensitiveApplication() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/onboarding"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/test-mifid/suitability/OnBoarding"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn404WhenApplicationNotExists() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ADVISE"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            org.hamcrest.Matchers.containsString(
                "No versions found for suitability test with application='ADVISE'")));
  }

  @Test
  void shouldReturn400WhenInvalidVersion() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING?version=-1"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING?version=0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn404WhenVersionNotExists() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/suitability/ONBOARDING?version=999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(
            org.hamcrest.Matchers.containsString(
                "No suitability test found for application='ONBOARDING' and version=999")));
  }
}