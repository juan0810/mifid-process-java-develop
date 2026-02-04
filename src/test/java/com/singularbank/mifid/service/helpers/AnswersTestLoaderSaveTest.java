package com.singularbank.mifid.service.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.service.helpers.AnswersTestLoader.LoadedAnswers;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnswersTestLoaderSaveTest {

  @Mock
  private ItemRepository itemRepository;

  private AnswersTestLoader answersTestLoader;

  private static final TypeTest TEST_TYPE_CONVENIENCE = TypeTest.CONVENIENCE;
  private static final TypeTest TEST_TYPE_SUITABILITY = TypeTest.SUITABILITY;

  private static final Integer ANSWER_1_ID = 101;
  private static final Integer ANSWER_2_ID = 102;
  private static final Integer ANSWER_3_ID = 103;

  private static final Integer QUESTION_1_ID = 1;
  private static final Integer QUESTION_2_ID = 2;
  private static final Integer QUESTION_3_ID = 3;

  private static final String ANSWER_1_VALUE = "Sí";
  private static final String ANSWER_2_VALUE = "No";

  @BeforeEach
  void setUp() {
    answersTestLoader = new AnswersTestLoader(itemRepository);
  }

  @Test
  void shouldLoadByIdsSuccessfully() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE),
        createRespuesta(ANSWER_2_ID, QUESTION_2_ID, ANSWER_2_VALUE)
    );

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    LoadedAnswers result = answersTestLoader.loadByIds(answerIds);

    assertThat(result).isNotNull();
    assertThat(result.answers()).hasSize(2);
    assertThat(result.answersMap()).hasSize(2)
        .containsKeys(ANSWER_1_ID, ANSWER_2_ID);
    assertThat(result.questionsMap()).hasSize(2)
        .containsKeys(QUESTION_1_ID, QUESTION_2_ID);

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldLoadByIdsAndTestTypeSuccessfully() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE),
        createRespuesta(ANSWER_2_ID, QUESTION_2_ID, ANSWER_2_VALUE)
    );

    when(itemRepository.findAnswersByIdsAndTestType(answerIds, TEST_TYPE_CONVENIENCE))
        .thenReturn(answers);

    LoadedAnswers result = answersTestLoader.loadByIdsAndTestType(answerIds, TEST_TYPE_CONVENIENCE);

    assertThat(result).isNotNull();
    assertThat(result.answers()).hasSize(2);
    assertThat(result.answersMap()).hasSize(2)
        .containsKeys(ANSWER_1_ID, ANSWER_2_ID);
    assertThat(result.questionsMap()).hasSize(2)
        .containsKeys(QUESTION_1_ID, QUESTION_2_ID);

    verify(itemRepository).findAnswersByIdsAndTestType(answerIds, TEST_TYPE_CONVENIENCE);
  }

  @Test
  void shouldLoadEmptyListSuccessfully() {
    when(itemRepository.findAnswersByIds(Collections.emptyList()))
        .thenReturn(Collections.emptyList());

    LoadedAnswers result = answersTestLoader.loadByIds(Collections.emptyList());

    assertThat(result).isNotNull();
    assertThat(result.answers()).isEmpty();
    assertThat(result.answersMap()).isEmpty();
    assertThat(result.questionsMap()).isEmpty();

    verify(itemRepository).findAnswersByIds(Collections.emptyList());
  }

  @Test
  void shouldLoadEmptyListWithTestTypeSuccessfully() {
    when(itemRepository.findAnswersByIdsAndTestType(Collections.emptyList(), TEST_TYPE_CONVENIENCE))
        .thenReturn(Collections.emptyList());

    LoadedAnswers result = answersTestLoader.loadByIdsAndTestType(Collections.emptyList(),
        TEST_TYPE_CONVENIENCE);

    assertThat(result).isNotNull();
    assertThat(result.answers()).isEmpty();
    assertThat(result.answersMap()).isEmpty();
    assertThat(result.questionsMap()).isEmpty();

    verify(itemRepository).findAnswersByIdsAndTestType(Collections.emptyList(),
        TEST_TYPE_CONVENIENCE);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerNotFoundInLoadByIds() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE)
    );

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    assertThatThrownBy(() -> answersTestLoader.loadByIds(answerIds))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found")
        .hasMessageContaining("102");

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerNotFoundInLoadByIdsAndTestType() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE)
    );

    when(itemRepository.findAnswersByIdsAndTestType(answerIds, TEST_TYPE_CONVENIENCE))
        .thenReturn(answers);

    assertThatThrownBy(
        () -> answersTestLoader.loadByIdsAndTestType(answerIds, TEST_TYPE_CONVENIENCE))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid answers for test type 'CONVENIENCE'")
        .hasMessageContaining("(CO)")
        .hasMessageContaining("102");

    verify(itemRepository).findAnswersByIdsAndTestType(answerIds, TEST_TYPE_CONVENIENCE);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenMultipleAnswersNotFound() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID, ANSWER_3_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE)
    );

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    assertThatThrownBy(() -> answersTestLoader.loadByIds(answerIds))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found")
        .hasMessageContaining("102")
        .hasMessageContaining("103");

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldHandleAnswersWithNullPregunta() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    Answer answerConPregunta = createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE);
    Answer answerSinPregunta = Answer.builder()
        .id(ANSWER_2_ID)
        .value(ANSWER_2_VALUE)
        .question(null)
        .build();

    List<Answer> answers = List.of(answerConPregunta, answerSinPregunta);

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    LoadedAnswers result = answersTestLoader.loadByIds(answerIds);

    assertThat(result.answersMap()).hasSize(2);
    assertThat(result.questionsMap()).hasSize(1)
        .containsOnlyKeys(QUESTION_1_ID);

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldHandleDuplicateQuestions() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    Answer answer1 = createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "Valor1");
    Answer answer2 = createRespuesta(ANSWER_2_ID, QUESTION_1_ID, "Valor2");

    List<Answer> answers = List.of(answer1, answer2);

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    LoadedAnswers result = answersTestLoader.loadByIds(answerIds);

    assertThat(result.answersMap()).hasSize(2);
    assertThat(result.questionsMap()).hasSize(1)
        .containsKey(QUESTION_1_ID);

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldLoadMultipleAnswersWithDifferentTestTypes() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, ANSWER_1_VALUE),
        createRespuesta(ANSWER_2_ID, QUESTION_2_ID, ANSWER_2_VALUE)
    );

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    LoadedAnswers result = answersTestLoader.loadByIds(answerIds);

    assertThat(result.answers()).hasSize(2);
    assertThat(result.answersMap()).hasSize(2);
    assertThat(result.questionsMap()).hasSize(2);

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAllAnswersNotFound() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID);

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> answersTestLoader.loadByIds(answerIds))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found")
        .hasMessageContaining("101")
        .hasMessageContaining("102");

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldBuildCorrectAnswersMapWithAllAnswers() {
    List<Integer> answerIds = List.of(ANSWER_1_ID, ANSWER_2_ID, ANSWER_3_ID);

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "Valor1"),
        createRespuesta(ANSWER_2_ID, QUESTION_2_ID, "Valor2"),
        createRespuesta(ANSWER_3_ID, QUESTION_3_ID, "Valor3")
    );

    when(itemRepository.findAnswersByIds(answerIds)).thenReturn(answers);

    LoadedAnswers result = answersTestLoader.loadByIds(answerIds);

    assertThat(result.answersMap()).hasSize(3)
        .containsEntry(ANSWER_1_ID, answers.get(0))
        .containsEntry(ANSWER_2_ID, answers.get(1))
        .containsEntry(ANSWER_3_ID, answers.get(2));

    assertThat(result.questionsMap()).hasSize(3)
        .containsKeys(QUESTION_1_ID, QUESTION_2_ID, QUESTION_3_ID);

    verify(itemRepository).findAnswersByIds(answerIds);
  }

  @Test
  void shouldValidateTestTypeInErrorMessage() {
    List<Integer> answerIds = List.of(ANSWER_1_ID);

    when(itemRepository.findAnswersByIdsAndTestType(answerIds, TEST_TYPE_SUITABILITY))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(
        () -> answersTestLoader.loadByIdsAndTestType(answerIds, TEST_TYPE_SUITABILITY))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid answers for test type 'SUITABILITY'")
        .hasMessageContaining("(ID)")
        .hasMessageContaining("101");

    verify(itemRepository).findAnswersByIdsAndTestType(answerIds, TEST_TYPE_SUITABILITY);
  }

  private Answer createRespuesta(Integer answerId, Integer questionId, String valor) {
    Question question = Question.builder()
        .id(questionId)
        .text("Pregunta " + questionId)
        .build();

    return Answer.builder()
        .id(answerId)
        .value(valor)
        .question(question)
        .build();
  }
}