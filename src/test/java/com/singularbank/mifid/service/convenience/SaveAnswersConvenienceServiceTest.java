package com.singularbank.mifid.service.convenience;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.*;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.*;
import com.singularbank.mifid.service.convenience.impl.SaveAnswersConvenienceTestServiceImpl;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.ResultTestDescriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreTestAnswersConvenienceServiceTest {

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private RespuestaClienteRepository respuestaClienteRepository;

  @Mock
  private RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;

  @Mock
  private RelRespuestaCombinacionRepository relRespuestaCombinacionRepository;

  @Captor
  private ArgumentCaptor<RespuestaCliente> respuestaClienteCaptor;

  @Captor
  private ArgumentCaptor<List<RespuestaDetalle>> detallesCaptor;

  private SaveAnswersConvenienceTestServiceImpl saveAnswersConvenienceTestService;

  private static final String DOCUMENT_NUMBER = "12345678A";
  private static final Short VERSION_ID = 1;

  private static final Integer CONV_QUESTION_1 = 1;
  private static final Integer CONV_QUESTION_2 = 2;
  private static final Integer CONV_ANSWER_1 = 101;
  private static final Integer CONV_ANSWER_2 = 102;

  private static final Integer RESPONSE_ID = 100;

  @BeforeEach
  void setUp() {
    CombinacionRespuestaRepository combinacionRepository = mock(CombinacionRespuestaRepository.class);
    lenient().when(combinacionRepository.findByFamilyCode(anyString(), anyBoolean()))
        .thenAnswer(inv -> {
          String familia = inv.getArgument(0);
          boolean conveniente = inv.getArgument(1);
          int baseId = conveniente ? 20 : 31;
          return baseId + (familia.charAt(0) - 'A');
        });
    lenient().when(combinacionRepository.findByProfileName(anyString()))
        .thenAnswer(inv -> {
          String profile = inv.getArgument(0);
          return switch (profile) {
            case "Conservador" -> 10;
            case "Moderado" -> 11;
            case "Equilibrado" -> 12;
            case "Decidido" -> 13;
            case "Agresivo" -> 14;
            default -> null;
          };
        });
    
    ConvenienceQuestionIdLoader questionIdLoader = mock(ConvenienceQuestionIdLoader.class);
    lenient().when(questionIdLoader.getIdByOrder(anyShort(), anyShort()))
        .thenAnswer(inv -> {
          short order = inv.getArgument(1);  // Second argument is the order
          return order + 13; // 11->24, 12->25, etc.
        });
    
    CombinacionMatchingRepository combinacionMatchingRepository = mock(CombinacionMatchingRepository.class);
    AnswersTestLoader answersTestLoader = new AnswersTestLoader(itemRepository);
    ConvenienceCalculatorService convenienceCalculator = new ConvenienceCalculatorService(combinacionRepository, questionIdLoader, combinacionMatchingRepository);
    ResultTestDescriptionBuilder descriptionBuilder = new ResultTestDescriptionBuilder();

    saveAnswersConvenienceTestService = new SaveAnswersConvenienceTestServiceImpl(
        answersTestLoader,
        respuestaClienteRepository,
        respuestaClienteDetalleRepository,
        relRespuestaCombinacionRepository,
        convenienceCalculator,
        descriptionBuilder
    );
  }

  @Test
  void shouldSaveConvenienceTest() {
    StoreTestAnswers StoreTestAnswers = createTestAnswers(createConvenienceQuestions());

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.CONVENIENCE)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersConvenienceTestService.saveAnswers(DOCUMENT_NUMBER, StoreTestAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
    assertThat(response.results().convenience()).isNotNull();
    assertThat(response.results().suitability()).isNull();
    assertThat(response.results().sustainability()).isNull();

    verify(respuestaClienteRepository).save(respuestaClienteCaptor.capture());
    RespuestaCliente captured = respuestaClienteCaptor.getValue();
    assertThat(captured.getClienteDni()).isEqualTo(DOCUMENT_NUMBER);
    assertThat(captured.getResultadoConveniencia()).isNotNull();
    assertThat(captured.getResultadoIdoneidad()).isNull();

    verify(respuestaClienteDetalleRepository).saveAll(eq(RESPONSE_ID), detallesCaptor.capture());
    assertThat(detallesCaptor.getValue()).hasSize(2);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerNotFound() {
    StoreTestAnswers StoreTestAnswers = createTestAnswers(createConvenienceQuestions());

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> saveAnswersConvenienceTestService.saveAnswers(DOCUMENT_NUMBER, StoreTestAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenPartialAnswersFound() {
    StoreTestAnswers StoreTestAnswers = createTestAnswers(createConvenienceQuestions());

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE)
    );

    when(itemRepository.findAnswersByIds(List.of(CONV_ANSWER_1, CONV_ANSWER_2)))
        .thenReturn(answers);

    assertThatThrownBy(() -> saveAnswersConvenienceTestService.saveAnswers(DOCUMENT_NUMBER, StoreTestAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found")
        .hasMessageContaining("102");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenQuestionBelongsToWrongTestType() {
    StoreTestAnswers StoreTestAnswers = createTestAnswers(createConvenienceQuestions());

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.SUITABILITY),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.SUITABILITY)
    );

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);

    assertThatThrownBy(() -> saveAnswersConvenienceTestService.saveAnswers(DOCUMENT_NUMBER, StoreTestAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("belongs to test type 'ID'")
        .hasMessageContaining("but was sent in test type 'CO'");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenDocumentNumberIsNull() {
    StoreTestAnswers StoreTestAnswers = createTestAnswers(createConvenienceQuestions());

    assertThatThrownBy(() -> saveAnswersConvenienceTestService.saveAnswers(null, StoreTestAnswers))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("documentNumber is required");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenTestAnswersIsNull() {
    assertThatThrownBy(() -> saveAnswersConvenienceTestService.saveAnswers(DOCUMENT_NUMBER, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("testAnswers is required");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerHasNullQuestion() {
    StoreTestAnswers StoreTestAnswers = createTestAnswers(createConvenienceQuestions());

    Answer answerSinPregunta = Answer.builder()
        .id(CONV_ANSWER_1)
        .value("A")
        .question(null)
        .build();

    Answer answerConPregunta = createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B",
        TypeTest.CONVENIENCE);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(
        List.of(answerSinPregunta, answerConPregunta));

    assertThatThrownBy(() -> saveAnswersConvenienceTestService.saveAnswers(DOCUMENT_NUMBER, StoreTestAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer or question not found for ID: 101");
  }

  private StoreTestAnswers createTestAnswers(List<StoreTestAnswers.QuestionResponse> questions) {
    return new StoreTestAnswers("ONBOARDING", VERSION_ID, questions);
  }

  private List<StoreTestAnswers.QuestionResponse> createConvenienceQuestions() {
    return List.of(
            new StoreTestAnswers.QuestionResponse(CONV_QUESTION_1, CONV_ANSWER_1),
            new StoreTestAnswers.QuestionResponse(CONV_QUESTION_2, CONV_ANSWER_2)
    );
  }

  private Answer createRespuesta(Integer answerId, Integer questionId, String valor,
      TypeTest typeTest) {
    Question question = Question.builder()
        .id(questionId)
        .text("Pregunta " + questionId)
        .testType(typeTest.getCode())
        .order(questionId.shortValue())
        .build();

    return Answer.builder()
        .id(answerId)
        .value(valor)
        .question(question)
        .correct(false)
        .build();
  }

  private RespuestaCliente createSavedResponse(Integer id) {
    return RespuestaCliente.builder()
        .id(id)
        .clienteDni(DOCUMENT_NUMBER)
        .versionId(VERSION_ID)
        .build();
  }

  private RespuestaCliente createPreviousConvenienceTest() {
    Answer resp1 = createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A",
        TypeTest.CONVENIENCE);
    Answer resp2 = createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B",
        TypeTest.CONVENIENCE);

    RespuestaDetalle det1 = RespuestaDetalle.builder()
        .respuestaId(CONV_ANSWER_1)
        .valor("A")
        .answer(resp1)
        .build();

    RespuestaDetalle det2 = RespuestaDetalle.builder()
        .respuestaId(CONV_ANSWER_2)
        .valor("B")
        .answer(resp2)
        .build();

    return RespuestaCliente.builder()
        .id(99)
        .clienteDni(DOCUMENT_NUMBER)
        .versionId(VERSION_ID)
        .resultadoConveniencia("A,B,C")
        .detalles(List.of(det1, det2))
        .build();
  }
}