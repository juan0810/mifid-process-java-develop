package com.singularbank.mifid.service.answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.entity.SaveAnswers;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.CombinacionMatchingRepository;
import com.singularbank.mifid.repository.CombinacionRespuestaRepository;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.answer.impl.SaveAnswersServiceImpl;
import com.singularbank.mifid.service.convenience.ConvenienceCalculatorService;
import com.singularbank.mifid.service.convenience.ConvenienceQuestionIdLoader;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.ResultTestDescriptionBuilder;
import com.singularbank.mifid.service.suitability.SuitabilityCalculatorService;
import com.singularbank.mifid.service.sustainability.SustainabilityCalculatorService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaveAnswersServiceTest {

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private RespuestaClienteRepository respuestaClienteRepository;

  @Mock
  private RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;

  @Mock
  private com.singularbank.mifid.repository.RelRespuestaCombinacionRepository relRespuestaCombinacionRepository;

  @Captor
  private ArgumentCaptor<RespuestaCliente> respuestaClienteCaptor;

  @Captor
  private ArgumentCaptor<List<RespuestaDetalle>> detallesCaptor;

  private SaveAnswersServiceImpl saveAnswersService;

  private static final String DOCUMENT_NUMBER = "12345678A";
  private static final Short VERSION_ID = 1;

  private static final Integer CONV_QUESTION_1 = 1;
  private static final Integer CONV_QUESTION_2 = 2;
  private static final Integer CONV_ANSWER_1 = 101;
  private static final Integer CONV_ANSWER_2 = 102;

  private static final Integer SUIT_QUESTION_1 = 11;
  private static final Integer SUIT_QUESTION_2 = 121;
  private static final Integer SUIT_ANSWER_1 = 201;
  private static final Integer SUIT_ANSWER_2 = 202;

  private static final Integer SUST_QUESTION_1 = 17;
  private static final Integer SUST_ANSWER_1 = 301;

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
    SuitabilityCalculatorService suitabilityCalculator = new SuitabilityCalculatorService(combinacionRepository);
    SustainabilityCalculatorService sustainabilityCalculator = new SustainabilityCalculatorService();
    ResultTestDescriptionBuilder descriptionBuilder = new ResultTestDescriptionBuilder();

    saveAnswersService = new SaveAnswersServiceImpl(
        answersTestLoader,
        respuestaClienteRepository,
        respuestaClienteDetalleRepository,
        relRespuestaCombinacionRepository,
        convenienceCalculator,
        suitabilityCalculator,
        sustainabilityCalculator,
        descriptionBuilder
    );
  }

  @Test
  void shouldSaveSingleConvenienceTest() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createConvenienceTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.CONVENIENCE)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

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
  void shouldSaveSingleSuitabilityTest() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createSuitabilityTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(SUIT_ANSWER_1, SUIT_QUESTION_1, "D", TypeTest.SUITABILITY),
        createRespuesta(SUIT_ANSWER_2, SUIT_QUESTION_2, "C", TypeTest.SUITABILITY)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);
    RespuestaCliente previousConvenience = createPreviousConvenienceTest();

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.findConvenienceTestByIdentityAndVersion(DOCUMENT_NUMBER,
        VERSION_ID))
        .thenReturn(Optional.of(previousConvenience));
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
    assertThat(response.results().convenience()).isNull();
    assertThat(response.results().suitability()).isNotNull();
    assertThat(response.results().sustainability()).isNull();

    verify(respuestaClienteRepository).save(respuestaClienteCaptor.capture());
    RespuestaCliente captured = respuestaClienteCaptor.getValue();
    assertThat(captured.getResultadoConveniencia()).isNull();
    assertThat(captured.getResultadoIdoneidad()).isNotNull();
  }

  @Test
  void shouldSaveSingleSustainabilityTest() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createSustainabilityTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(SUST_ANSWER_1, SUST_QUESTION_1, "C", TypeTest.SUSTAINABILITY)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
    assertThat(response.results().convenience()).isNull();
    assertThat(response.results().suitability()).isNull();
    assertThat(response.results().sustainability()).isNotNull();

    verify(respuestaClienteRepository).save(respuestaClienteCaptor.capture());
    RespuestaCliente captured = respuestaClienteCaptor.getValue();
    assertThat(captured.getResultadoConveniencia()).isNull();
    assertThat(captured.getResultadoIdoneidad()).isNull();

    verify(respuestaClienteDetalleRepository).saveAll(eq(RESPONSE_ID), detallesCaptor.capture());
    assertThat(detallesCaptor.getValue()).hasSize(1);
  }

  @Test
  void shouldSaveConvenienceAndSuitabilityTogether() {
    SaveAnswers saveAnswers = createTestAnswers(
        List.of(createConvenienceTestBlock(), createSuitabilityTestBlock())
    );

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.CONVENIENCE),
        createRespuesta(SUIT_ANSWER_1, SUIT_QUESTION_1, "D", TypeTest.SUITABILITY),
        createRespuesta(SUIT_ANSWER_2, SUIT_QUESTION_2, "C", TypeTest.SUITABILITY)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
    assertThat(response.results().convenience()).isNotNull();
    assertThat(response.results().suitability()).isNotNull();
    assertThat(response.results().sustainability()).isNull();

    verify(respuestaClienteRepository).save(respuestaClienteCaptor.capture());
    RespuestaCliente captured = respuestaClienteCaptor.getValue();
    assertThat(captured.getResultadoConveniencia()).isNotNull();
    assertThat(captured.getResultadoIdoneidad()).isNotNull();

    verify(respuestaClienteDetalleRepository).saveAll(eq(RESPONSE_ID), detallesCaptor.capture());
    assertThat(detallesCaptor.getValue()).hasSize(4);
  }

  @Test
  void shouldSaveAllThreeTestsTogether() {
    SaveAnswers saveAnswers = createTestAnswers(
        List.of(
            createConvenienceTestBlock(),
            createSuitabilityTestBlock(),
            createSustainabilityTestBlock()
        )
    );

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.CONVENIENCE),
        createRespuesta(SUIT_ANSWER_1, SUIT_QUESTION_1, "D", TypeTest.SUITABILITY),
        createRespuesta(SUIT_ANSWER_2, SUIT_QUESTION_2, "C", TypeTest.SUITABILITY),
        createRespuesta(SUST_ANSWER_1, SUST_QUESTION_1, "C", TypeTest.SUSTAINABILITY)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
    assertThat(response.results().convenience()).isNotNull();
    assertThat(response.results().suitability()).isNotNull();
    assertThat(response.results().sustainability()).isNotNull();

    verify(respuestaClienteRepository).save(respuestaClienteCaptor.capture());
    RespuestaCliente captured = respuestaClienteCaptor.getValue();
    assertThat(captured.getResultadoConveniencia()).isNotNull();
    assertThat(captured.getResultadoIdoneidad()).isNotNull();

    verify(respuestaClienteDetalleRepository).saveAll(eq(RESPONSE_ID), detallesCaptor.capture());
    assertThat(detallesCaptor.getValue()).hasSize(5);
  }

  @Test
  void shouldSortTestsInCorrectOrder() {
    SaveAnswers saveAnswers = createTestAnswers(
        List.of(
            createSustainabilityTestBlock(),
            createSuitabilityTestBlock(),
            createConvenienceTestBlock()
        )
    );

    List<Answer> answers = List.of(
        createRespuesta(SUST_ANSWER_1, SUST_QUESTION_1, "C", TypeTest.SUSTAINABILITY),
        createRespuesta(SUIT_ANSWER_1, SUIT_QUESTION_1, "D", TypeTest.SUITABILITY),
        createRespuesta(SUIT_ANSWER_2, SUIT_QUESTION_2, "C", TypeTest.SUITABILITY),
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.CONVENIENCE)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
  }

  @Test
  void shouldThrowBadRequestExceptionWhenDuplicateTestTypes() {
    SaveAnswers saveAnswers = createTestAnswers(
        List.of(createConvenienceTestBlock(), createConvenienceTestBlock())
    );

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Duplicate test types found");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerNotFound() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createConvenienceTestBlock()));

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenPartialAnswersFound() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createConvenienceTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.CONVENIENCE)
    );

    when(itemRepository.findAnswersByIds(List.of(CONV_ANSWER_1, CONV_ANSWER_2)))
        .thenReturn(answers);

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer IDs not found")
        .hasMessageContaining("102");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenQuestionBelongsToWrongTestType() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createConvenienceTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(CONV_ANSWER_1, CONV_QUESTION_1, "A", TypeTest.SUITABILITY),
        createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B", TypeTest.SUITABILITY)
    );

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("belongs to test type 'ID'")
        .hasMessageContaining("but was sent in test type 'CO'");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenSuitabilityWithoutPreviousConvenience() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createSuitabilityTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(SUIT_ANSWER_1, SUIT_QUESTION_1, "D", TypeTest.SUITABILITY),
        createRespuesta(SUIT_ANSWER_2, SUIT_QUESTION_2, "C", TypeTest.SUITABILITY)
    );

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.findConvenienceTestByIdentityAndVersion(DOCUMENT_NUMBER,
        VERSION_ID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Convenience test required before suitability");
  }

  @Test
  void shouldUsePreviousConvenienceForSuitability() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createSuitabilityTestBlock()));

    List<Answer> answers = List.of(
        createRespuesta(SUIT_ANSWER_1, SUIT_QUESTION_1, "D", TypeTest.SUITABILITY),
        createRespuesta(SUIT_ANSWER_2, SUIT_QUESTION_2, "C", TypeTest.SUITABILITY)
    );

    RespuestaCliente savedResponse = createSavedResponse(RESPONSE_ID);
    RespuestaCliente previousConvenience = createPreviousConvenienceTest();

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(answers);
    when(respuestaClienteRepository.findConvenienceTestByIdentityAndVersion(DOCUMENT_NUMBER,
        VERSION_ID))
        .thenReturn(Optional.of(previousConvenience));
    when(respuestaClienteRepository.save(any(RespuestaCliente.class))).thenReturn(savedResponse);

    TestResponseCreatedDTO response = saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers);

    assertThat(response).isNotNull();
    assertThat(response.responseClientId()).isEqualTo(RESPONSE_ID);
    assertThat(response.results()).isNotNull();
    verify(respuestaClienteRepository).findConvenienceTestByIdentityAndVersion(DOCUMENT_NUMBER,
        VERSION_ID);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenDocumentNumberIsNull() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createConvenienceTestBlock()));

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(null, saveAnswers))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("documentNumber is required");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenTestAnswersIsNull() {
    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("testAnswers is required");
  }

  @Test
  void shouldThrowBadRequestExceptionWhenAnswerHasNullQuestion() {
    SaveAnswers saveAnswers = createTestAnswers(List.of(createConvenienceTestBlock()));

    Answer answerSinPregunta = Answer.builder()
        .id(CONV_ANSWER_1)
        .value("A")
        .question(null)
        .build();

    Answer answerConPregunta = createRespuesta(CONV_ANSWER_2, CONV_QUESTION_2, "B",
        TypeTest.CONVENIENCE);

    when(itemRepository.findAnswersByIds(anyList())).thenReturn(
        List.of(answerSinPregunta, answerConPregunta));

    assertThatThrownBy(() -> saveAnswersService.saveAnswers(DOCUMENT_NUMBER, saveAnswers))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Answer or question not found for ID: 101");
  }

  private SaveAnswers createTestAnswers(List<SaveAnswers.TestBlock> tests) {
    return new SaveAnswers("ONBOARDING", VERSION_ID, tests);
  }

  private SaveAnswers.TestBlock createConvenienceTestBlock() {
    return new SaveAnswers.TestBlock(
        TypeTest.CONVENIENCE,
        List.of(
            new SaveAnswers.QuestionResponse(CONV_QUESTION_1, CONV_ANSWER_1),
            new SaveAnswers.QuestionResponse(CONV_QUESTION_2, CONV_ANSWER_2)
        )
    );
  }

  private SaveAnswers.TestBlock createSuitabilityTestBlock() {
    return new SaveAnswers.TestBlock(
        TypeTest.SUITABILITY,
        List.of(
            new SaveAnswers.QuestionResponse(SUIT_QUESTION_1, SUIT_ANSWER_1),
            new SaveAnswers.QuestionResponse(SUIT_QUESTION_2, SUIT_ANSWER_2)
        )
    );
  }

  private SaveAnswers.TestBlock createSustainabilityTestBlock() {
    return new SaveAnswers.TestBlock(
        TypeTest.SUSTAINABILITY,
        List.of(new SaveAnswers.QuestionResponse(SUST_QUESTION_1, SUST_ANSWER_1))
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