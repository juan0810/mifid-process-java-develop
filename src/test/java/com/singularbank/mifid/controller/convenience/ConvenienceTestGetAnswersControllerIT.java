package com.singularbank.mifid.controller.convenience;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.controller.helpers.dto.AnswersTestResponseDTO;
import com.singularbank.mifid.controller.helpers.mapper.AnswersTestResponseMapper;
import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.service.helpers.GetAnswersTestService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
class ConvenienceTestGetAnswersControllerIT extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GetAnswersTestService getAnswersTestService;

  @MockitoBean
  private AnswersTestResponseMapper mapper;

  @Test
  void getAnswersConvenience_whenClientExists_shouldReturn200AndAnswers() throws Exception {
    // Given
    Integer testId = 1;

    AnswersTest serviceResponse = AnswersTest.builder()
        .testId(100)
        .version((short) 1)
        .build();

    AnswersTestResponseDTO mapperResponse = AnswersTestResponseDTO.builder()
        .testId(100)
        .version((short) 1)
        .questions(List.of(
            AnswersTestResponseDTO.QuestionDTO.builder()
                .id(1)
                .text("Seleccione una respuesta")
                .option(AnswersTestResponseDTO.OptionDTO.builder().id(2).build())
                .build(),
            AnswersTestResponseDTO.QuestionDTO.builder()
                .id(2)
                .option(AnswersTestResponseDTO.OptionDTO.builder().id(5).build())
                .build()
        ))
        .build();

    when(getAnswersTestService.get(testId, TypeTest.CONVENIENCE)).thenReturn(serviceResponse);
    when(mapper.toDto(any(AnswersTest.class))).thenReturn(mapperResponse);

    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/convenience-responses/{id}", testId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.testId").value(100));
  }

  @Test
  void getAnswersConvenience_whenTestIdInvalid_shouldReturn400BadRequest() throws Exception {
    // Given
    Integer testId = 0;
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/convenience-responses/{id}", testId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAnswersConvenience_whenServiceThrowsNotFound_shouldReturn404() throws Exception {
    // Given
    Integer testId = 1;
    when(getAnswersTestService.get(testId, TypeTest.CONVENIENCE)).thenThrow(
        ResourceNotFoundException.class);
    // When & Then
    mockMvc.perform(get("/api/v1/test-mifid/convenience-responses/{id}", testId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}