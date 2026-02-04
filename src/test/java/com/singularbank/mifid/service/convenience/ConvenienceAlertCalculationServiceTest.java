package com.singularbank.mifid.service.convenience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.StoreTestAnswers;
import com.singularbank.mifid.entity.StoreTestAnswers.QuestionResponse;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.CombinacionMatchingRepository;
import com.singularbank.mifid.repository.CombinacionRespuestaRepository;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.service.convenience.impl.ConvenienceAlertCalculationServiceImpl;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConvenienceAlertCalculationServiceTest {

  @Mock
  private ItemRepository itemRepository;

  private ConvenienceAlertCalculationServiceImpl convenienceAlertService;

  private static final String CLIENT_DNI = "12345678A";
  private static final Short VERSION_ID = 1;
  private static final TypeTest TEST_TYPE = TypeTest.CONVENIENCE;

  private static final Integer QUESTION_1_ID = 1;
  private static final Integer QUESTION_2_ID = 2;
  private static final Integer QUESTION_3_ID = 3;
  private static final Integer QUESTION_101_ID = 101;

  private static final Integer ANSWER_1_ID = 101;
  private static final Integer ANSWER_2_ID = 102;
  private static final Integer ANSWER_3_ID = 103;
  private static final Integer ANSWER_101_ID = 201;

  @BeforeEach
  void setUp() {
    CombinacionRespuestaRepository combinacionRepository = mock(
        CombinacionRespuestaRepository.class);
    lenient().when(combinacionRepository.findByFamilyCode(anyString(), anyBoolean()))
        .thenAnswer(inv -> {
          String familia = inv.getArgument(0);
          boolean conveniente = inv.getArgument(1);
          int baseId = conveniente ? 20 : 31;
          return baseId + (familia.charAt(0) - 'A');
        });

    ConvenienceQuestionIdLoader questionIdLoader = mock(ConvenienceQuestionIdLoader.class);
    lenient().when(questionIdLoader.getIdByOrder(anyShort(), anyShort()))
        .thenAnswer(inv -> {
          short order = inv.getArgument(1);  // Second argument is the order
          return order + 13; // 11->24, 12->25, etc.
        });

    CombinacionMatchingRepository combinacionMatchingRepository = mock(
        CombinacionMatchingRepository.class);
    AnswersTestLoader answersTestLoader = new AnswersTestLoader(itemRepository);
    ConvenienceCalculatorService convenienceCalculator = new ConvenienceCalculatorService(
        combinacionRepository, questionIdLoader, combinacionMatchingRepository);

    convenienceAlertService = new ConvenienceAlertCalculationServiceImpl(
        answersTestLoader,
        convenienceCalculator
    );
  }

  @Test
  void shouldCalculateAlertsWithValidResponses() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(
        List.of(
            createQuestionResponse(QUESTION_1_ID, ANSWER_1_ID),
            createQuestionResponse(QUESTION_2_ID, ANSWER_2_ID),
            createQuestionResponse(QUESTION_3_ID, ANSWER_3_ID),
            createQuestionResponse(QUESTION_101_ID, ANSWER_101_ID)
        )
    );

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "A"),
        createRespuesta(ANSWER_2_ID, QUESTION_2_ID, "B"),
        createRespuesta(ANSWER_3_ID, QUESTION_3_ID, "D"),
        createRespuesta(ANSWER_101_ID, QUESTION_101_ID, "A")
    );

    when(itemRepository.findAnswersByIdsAndTestType(anyList(), eq(TEST_TYPE)))
        .thenReturn(answers);

    ConvenienceResult result = convenienceAlertService.calculateAlerts(CLIENT_DNI,
        storeTestAnswers);

    assertThat(result).isNotNull();
    assertThat(result.getConvenientFamilies()).isNotNull();
    assertThat(result.getNotConvenientFamilies()).isNotNull();
    assertThat(result.getAlerts()).isNotNull();

    verify(itemRepository).findAnswersByIdsAndTestType(
        List.of(ANSWER_1_ID, ANSWER_2_ID, ANSWER_3_ID, ANSWER_101_ID), TEST_TYPE);
  }

  @Test
  void shouldHandleEmptyQuestionResponses() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(Collections.emptyList());

    when(itemRepository.findAnswersByIdsAndTestType(anyList(), eq(TEST_TYPE)))
        .thenReturn(Collections.emptyList());

    ConvenienceResult result = convenienceAlertService.calculateAlerts(CLIENT_DNI,
        storeTestAnswers);

    assertThat(result).isNotNull();
    assertThat(result.getAlerts()).hasSize(1);
    assertThat(result.getAlerts().getFirst()).contains("No families");

    verify(itemRepository).findAnswersByIdsAndTestType(Collections.emptyList(), TEST_TYPE);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerNotFound() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(
        List.of(
            createQuestionResponse(QUESTION_1_ID, ANSWER_1_ID),
            createQuestionResponse(QUESTION_2_ID, ANSWER_2_ID)
        )
    );

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "A")
    );

    when(itemRepository.findAnswersByIdsAndTestType(anyList(), eq(TEST_TYPE)))
        .thenReturn(answers);

    assertThatThrownBy(() -> convenienceAlertService.calculateAlerts(CLIENT_DNI, storeTestAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid answers for test type 'CONVENIENCE'")
        .hasMessageContaining("102");

    verify(itemRepository).findAnswersByIdsAndTestType(List.of(ANSWER_1_ID, ANSWER_2_ID),
        TEST_TYPE);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenMultipleAnswersNotFound() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(
        List.of(
            createQuestionResponse(QUESTION_1_ID, ANSWER_1_ID),
            createQuestionResponse(QUESTION_2_ID, ANSWER_2_ID),
            createQuestionResponse(3, 103)
        )
    );

    List<Answer> answers = List.of(
        createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "A")
    );

    when(itemRepository.findAnswersByIdsAndTestType(anyList(), eq(TEST_TYPE)))
        .thenReturn(answers);

    assertThatThrownBy(() -> convenienceAlertService.calculateAlerts(CLIENT_DNI, storeTestAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid answers for test type 'CONVENIENCE'")
        .hasMessageContaining("102")
        .hasMessageContaining("103");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenDocumentNumberIsNull() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(Collections.emptyList());

    assertThatThrownBy(() -> convenienceAlertService.calculateAlerts(null, storeTestAnswers))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("documentNumber is required");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenStoreTestAnswersIsNull() {
    assertThatThrownBy(() -> convenienceAlertService.calculateAlerts(CLIENT_DNI, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("storeTestAnswers is required");
  }

  @Test
  void shouldSkipResponsesWithNullPregunta() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(
        List.of(
            createQuestionResponse(QUESTION_1_ID, ANSWER_1_ID),
            createQuestionResponse(QUESTION_2_ID, ANSWER_2_ID)
        )
    );

    Answer answerConPregunta = createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "A");
    Answer answerSinPregunta = Answer.builder()
        .id(ANSWER_2_ID)
        .value("B")
        .question(null)
        .build();

    List<Answer> answers = List.of(answerConPregunta, answerSinPregunta);

    when(itemRepository.findAnswersByIdsAndTestType(anyList(), eq(TEST_TYPE)))
        .thenReturn(answers);

    ConvenienceResult result = convenienceAlertService.calculateAlerts(CLIENT_DNI,
        storeTestAnswers);

    assertThat(result).isNotNull();
    verify(itemRepository).findAnswersByIdsAndTestType(List.of(ANSWER_1_ID, ANSWER_2_ID),
        TEST_TYPE);
  }

  @Test
  void shouldSkipResponsesWithNullValor() {
    StoreTestAnswers storeTestAnswers = createStoreTestAnswers(
        List.of(
            createQuestionResponse(QUESTION_1_ID, ANSWER_1_ID),
            createQuestionResponse(QUESTION_2_ID, ANSWER_2_ID)
        )
    );

    Answer answerConValor = createRespuesta(ANSWER_1_ID, QUESTION_1_ID, "A");
    Answer answerSinValor = createRespuesta(ANSWER_2_ID, QUESTION_2_ID, null);

    List<Answer> answers = List.of(answerConValor, answerSinValor);

    when(itemRepository.findAnswersByIdsAndTestType(anyList(), eq(TEST_TYPE)))
        .thenReturn(answers);

    ConvenienceResult result = convenienceAlertService.calculateAlerts(CLIENT_DNI,
        storeTestAnswers);

    assertThat(result).isNotNull();
  }

  private StoreTestAnswers createStoreTestAnswers(List<QuestionResponse> responses) {
    return StoreTestAnswers.builder()
        .service("ONBOARDING")
        .version(VERSION_ID)
        .questionResponses(responses)
        .build();
  }

  private QuestionResponse createQuestionResponse(Integer questionId, Integer answerId) {
    return QuestionResponse.builder()
        .questionId(questionId)
        .selectedOptionId(answerId)
        .build();
  }

  private Answer createRespuesta(Integer answerId, Integer questionId, String valor) {
    Question question = Question.builder()
        .id(questionId)
        .text("Pregunta " + questionId)
        .testType("CO")
        .build();

    return Answer.builder()
        .id(answerId)
        .value(valor)
        .question(question)
        .build();
  }
}