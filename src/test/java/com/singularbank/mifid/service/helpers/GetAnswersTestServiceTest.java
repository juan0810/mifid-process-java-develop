package com.singularbank.mifid.service.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.AnswersTest.Option;
import com.singularbank.mifid.entity.AnswersTest.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.service.helpers.impl.GetAnswersTestServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAnswersTestServiceTest {

  @Mock
  private RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;

  @InjectMocks
  private GetAnswersTestServiceImpl getAnswersTestService;

  private static final Integer TEST_ID = 1;
  private static final Short VERSION_ID = 1;

  @Test
  void shouldGetAnswersTestSuccessfully() {
    // Given
    AnswersTest expected = createAnswersTest(TEST_ID, TypeTest.SUITABILITY, 2);

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUITABILITY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTestId()).isEqualTo(TEST_ID);
    assertThat(result.getQuestions()).hasSize(2);

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY);
  }

  @Test
  void shouldGetAnswersTestForConvenience() {
    // Given
    AnswersTest expected = createAnswersTest(TEST_ID, TypeTest.CONVENIENCE, 3);

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.CONVENIENCE))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.CONVENIENCE);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTestId()).isEqualTo(TEST_ID);
    assertThat(result.getQuestions()).hasSize(3);

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.CONVENIENCE);
  }

  @Test
  void shouldGetAnswersTestForSustainability() {
    // Given
    AnswersTest expected = createAnswersTest(TEST_ID, TypeTest.SUSTAINABILITY, 1);

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUSTAINABILITY))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUSTAINABILITY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTestId()).isEqualTo(TEST_ID);
    assertThat(result.getQuestions()).hasSize(1);

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUSTAINABILITY);
  }

  @Test
  void shouldReturnNullWhenNoAnswersFound() {
    // Given
    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY))
        .thenReturn(null);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUITABILITY);

    // Then
    assertThat(result).isNull();

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY);
  }

  @Test
  void shouldReturnEmptyQuestionsWhenNoQuestionsInAnswer() {
    // Given
    AnswersTest expected = AnswersTest.builder()
        .testId(TEST_ID)
        .respuestaClienteId(100)
        .version(VERSION_ID)
        .questions(List.of())
        .build();

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUITABILITY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getQuestions()).isEmpty();

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY);
  }

  @Test
  void shouldHandleMultipleQuestions() {
    // Given
    AnswersTest expected = createAnswersTest(TEST_ID, TypeTest.SUITABILITY, 5);

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUITABILITY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getQuestions())
        .hasSize(5)
        .extracting(Question::getId)
        .containsExactly(1, 2, 3, 4, 5);

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY);
  }

  @Test
  void shouldPassCorrectParametersToRepository() {
    // Given
    Integer customTestId = 999;
    TypeTest customTypeTest = TypeTest.CONVENIENCE;
    AnswersTest expected = createAnswersTest(customTestId, customTypeTest, 1);

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(customTestId,
        customTypeTest))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(customTestId, customTypeTest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTestId()).isEqualTo(customTestId);

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(customTestId,
        customTypeTest);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenTestIdIsNull() {
    try {
      // When
      getAnswersTestService.get(null, TypeTest.SUITABILITY);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Then
      assertEquals("testId is required", e.getMessage());
      verifyNoInteractions(respuestaClienteDetalleRepository);
    }
  }

  @Test
  void shouldThrowNullPointerExceptionWhenTestTypeIsNull() {
    try {
      // When
      getAnswersTestService.get(TEST_ID, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Then
      assertEquals("testType is required", e.getMessage());
      verifyNoInteractions(respuestaClienteDetalleRepository);
    }
  }

  @Test
  void shouldThrowNullPointerExceptionWhenBothParametersAreNull() {
    try {
      // When
      getAnswersTestService.get(null, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Then
      assertEquals("testId is required", e.getMessage());
      verifyNoInteractions(respuestaClienteDetalleRepository);
    }
  }

  @Test
  void shouldHandleDifferentTestIds() {
    // Given
    List<Integer> testIds = List.of(1, 100, 999, 12345);

    for (Integer testId : testIds) {
      AnswersTest expected = createAnswersTest(testId, TypeTest.SUITABILITY, 1);

      when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(testId,
          TypeTest.SUITABILITY))
          .thenReturn(expected);

      // When
      AnswersTest result = getAnswersTestService.get(testId, TypeTest.SUITABILITY);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getTestId()).isEqualTo(testId);
    }

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(1,
        TypeTest.SUITABILITY);
    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(100,
        TypeTest.SUITABILITY);
    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(999,
        TypeTest.SUITABILITY);
    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(12345,
        TypeTest.SUITABILITY);
  }

  @Test
  void shouldReturnAnswersTestWithCompleteQuestionData() {
    // Given
    Option option = Option.builder()
        .id(1)
        .text("Opción A")
        .score(10)
        .build();

    Question question = Question.builder()
        .id(1)
        .text("¿Cuál es tu perfil de riesgo?")
        .option(option)
        .build();

    AnswersTest expected = AnswersTest.builder()
        .testId(TEST_ID)
        .respuestaClienteId(100)
        .version(VERSION_ID)
        .questions(List.of(question))
        .build();

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUITABILITY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getQuestions()).hasSize(1);

    Question resultQuestion = result.getQuestions().getFirst();
    assertThat(resultQuestion.getId()).isEqualTo(1);
    assertThat(resultQuestion.getText()).isEqualTo("¿Cuál es tu perfil de riesgo?");
    assertThat(resultQuestion.getOption()).isNotNull();
    assertThat(resultQuestion.getOption().getId()).isEqualTo(1);
    assertThat(resultQuestion.getOption().getText()).isEqualTo("Opción A");
    assertThat(resultQuestion.getOption().getScore()).isEqualTo(10);

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY);
  }

  @Test
  void shouldHandleQuestionsWithNullOptions() {
    // Given
    Question questionWithoutOption = Question.builder()
        .id(1)
        .text("Pregunta sin opción")
        .option(null)
        .build();

    AnswersTest expected = AnswersTest.builder()
        .testId(TEST_ID)
        .respuestaClienteId(100)
        .version(VERSION_ID)
        .questions(List.of(questionWithoutOption))
        .build();

    when(respuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY))
        .thenReturn(expected);

    // When
    AnswersTest result = getAnswersTestService.get(TEST_ID, TypeTest.SUITABILITY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getQuestions()).hasSize(1);
    assertThat(result.getQuestions().getFirst().getOption()).isNull();

    verify(respuestaClienteDetalleRepository).findAnswersByTestIdAndTestType(TEST_ID,
        TypeTest.SUITABILITY);
  }

  private AnswersTest createAnswersTest(Integer testId, TypeTest typeTest, int numberOfQuestions) {
    List<Question> questions = new java.util.ArrayList<>();

    for (int i = 1; i <= numberOfQuestions; i++) {
      Option option = Option.builder()
          .id(i)
          .text("Opción " + i)
          .score(i * 10)
          .build();

      Question question = Question.builder()
          .id(i)
          .text("Pregunta " + i + " del test " + typeTest.name())
          .option(option)
          .build();

      questions.add(question);
    }

    return AnswersTest.builder()
        .testId(testId)
        .respuestaClienteId(100)
        .version(VERSION_ID)
        .questions(questions)
        .build();
  }
}
