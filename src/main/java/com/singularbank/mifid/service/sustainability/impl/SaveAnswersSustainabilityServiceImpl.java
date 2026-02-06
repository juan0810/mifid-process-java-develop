package com.singularbank.mifid.service.sustainability.impl;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.FamilyDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.TestResults;
import com.singularbank.mifid.entity.*;
import com.singularbank.mifid.entity.CustomerActiveTests.SustainabilityPreferences;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.AnswersTestLoader.LoadedAnswers;
import com.singularbank.mifid.service.helpers.ResultTestDescriptionBuilder;
import com.singularbank.mifid.service.sustainability.SaveAnswersSustainabilityService;
import com.singularbank.mifid.service.sustainability.SustainabilityCalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaveAnswersSustainabilityServiceImpl implements SaveAnswersSustainabilityService {

  private final AnswersTestLoader answersTestLoader;
  private final RespuestaClienteRepository respuestaClienteRepository;
  private final RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;
  private final SustainabilityCalculatorService sustainabilityCalculator;

  public SaveAnswersSustainabilityServiceImpl(
      AnswersTestLoader answersTestLoader,
      RespuestaClienteRepository respuestaClienteRepository,
      RespuestaClienteDetalleRepository respuestaClienteDetalleRepository,
      SustainabilityCalculatorService sustainabilityCalculator) {
    this.answersTestLoader = answersTestLoader;
    this.respuestaClienteRepository = respuestaClienteRepository;
    this.respuestaClienteDetalleRepository = respuestaClienteDetalleRepository;
    this.sustainabilityCalculator = sustainabilityCalculator;
  }

  @Override
  @Transactional
  public   TestResponseCreatedDTO saveAnswers(String documentNumber, StoreTestAnswers saveAnswers){
    log.info("Processing Sustainability MiFID test answers for document: {}", documentNumber);

    Objects.requireNonNull(documentNumber, "documentNumber is required");
    Objects.requireNonNull(saveAnswers, "testAnswers is required");

    List<StoreTestAnswers.QuestionResponse> questionResponses = saveAnswers.getQuestionResponses();

    List<Integer> allAnswerIds = extractAnswerIds(questionResponses);
    LoadedAnswers loadedAnswers = answersTestLoader.loadByIds(allAnswerIds);

    validateTestTypes(questionResponses, loadedAnswers.answersMap());

    List<RespuestaDetalle> allDetails = buildDetails(questionResponses, loadedAnswers.answersMap());

    CalculatedResults results = calculateResults(questionResponses, loadedAnswers.answersMap());

    Integer responseId = saveResponse(
        documentNumber,
        saveAnswers.getVersion(),
        allDetails
    );

    log.info("✓ Saved MiFID test - Response ID: {}", responseId);

    return buildResponse(responseId, results);
  }

  private void validateTestTypes(List<StoreTestAnswers.QuestionResponse> questionResponses, Map<Integer, Answer> answersMap) {
    log.debug("Validating test types");

    questionResponses.stream()
            .map(qr -> new ValidationPair(
                    TypeTest.SUSTAINABILITY,
                    qr.getSelectedOptionId(),
                    answersMap.get(qr.getSelectedOptionId())
            ))
            .forEach(this::validateQuestionType);

    log.debug("✓ Test types validated");
  }

  private void validateQuestionType(ValidationPair pair) {
    var respuesta = pair.answer();

    if (respuesta == null || respuesta.getQuestion() == null) {
      throw new BadRequestException("Answer or question not found for ID: " + pair.answerId());
    }

    String questionTestType = respuesta.getQuestion().getTestType();

    if (!questionTestType.equals(pair.expectedTypeTest().getCode())) {
      throw new BadRequestException(
          "Answer %d belongs to test type '%s' but was sent in test type '%s'"
              .formatted(pair.answerId(), questionTestType, pair.expectedTypeTest().getCode()));
    }
  }

  private record ValidationPair(TypeTest expectedTypeTest, Integer answerId, Answer answer) {

  }

  private List<Integer> extractAnswerIds(List<StoreTestAnswers.QuestionResponse> questionResponses) {
    return questionResponses.stream()
            .map(StoreTestAnswers.QuestionResponse::getSelectedOptionId)
            .toList();
  }

  private List<RespuestaDetalle> buildDetails(List<StoreTestAnswers.QuestionResponse> questionResponses,
                                              Map<Integer, Answer> answersMap) {
    return questionResponses.stream()
            .map(qr -> {
              Answer answer = answersMap.get(qr.getSelectedOptionId());
              return RespuestaDetalle.builder()
                      .respuestaId(answer.getId())
                      .valor(answer.getValue())
                      .correcta(answer.getCorrect())
                      .build();
            })
            .toList();
  }

  /**
   * Metodo principal de cálculo: Sostenibilidad
   */
  private CalculatedResults calculateResults(
      List<StoreTestAnswers.QuestionResponse> questionResponses,
      Map<Integer, Answer> answersMap) {

    Map<Integer, String> responses = extractResponses(questionResponses, answersMap);
    SustainabilityResultData sustainabilityData = calculateSustainability(responses);

    return new CalculatedResults(sustainabilityData);
  }

  private Map<Integer, String> extractResponses(List<StoreTestAnswers.QuestionResponse> questionResponses, Map<Integer, Answer> answersMap) {
    return questionResponses.stream()
            .collect(Collectors.toMap(
                    qr -> answersMap.get(qr.getSelectedOptionId()).getQuestion().getId(),
                    qr -> answersMap.get(qr.getSelectedOptionId()).getValue()
            ));
  }

  private SustainabilityResultData calculateSustainability(Map<Integer, String> responses) {
    log.info("Calculating sustainability result");

    AnswersTest answersTest = buildAnswersTest(responses);

    String result = sustainabilityCalculator.calculateSustainabilityResult(answersTest);
    SustainabilityPreferences preferences = sustainabilityCalculator.extractSustainabilityPreferences(answersTest);

    log.info("Sustainability result calculated");
    return new SustainabilityResultData(result, preferences);
  }

  private AnswersTest buildAnswersTest(Map<Integer, String> responses) {
    List<AnswersTest.Question> questions = responses.entrySet().stream()
        .map(entry -> AnswersTest.Question.builder()
            .id(entry.getKey())
            .option(AnswersTest.Option.builder()
                .value(entry.getValue())
                .build())
            .build())
        .toList();

    return AnswersTest.builder()
        .questions(questions)
        .build();
  }

  private Integer saveResponse(
      String documentNumber,
      Short versionId,
      List<RespuestaDetalle> details) {

    RespuestaCliente response = RespuestaCliente.builder()
        .clienteDni(documentNumber)
        .versionId(versionId)
        .estado(StateTest.DRAFT)
        .build();

    RespuestaCliente saved = respuestaClienteRepository.save(response);
    respuestaClienteDetalleRepository.saveAll(saved.getId(), details);

    log.debug("✓ Saved response ID: {} with {} details", saved.getId(), details.size());
    return saved.getId();
  }

  private TestResponseCreatedDTO buildResponse(
      Integer responseId,
      CalculatedResults results) {

    TestResponseCreatedDTO.SustainabilityResult sustainabilityDto = buildSustainabilityResult(results.sustainabilityData());

    TestResults testResults = new TestResults(null, null, sustainabilityDto);

    return new TestResponseCreatedDTO(responseId, testResults);
  }

  private TestResponseCreatedDTO.SustainabilityResult buildSustainabilityResult(
      SustainabilityResultData data) {
    if (data == null) {
      return null;
    }

    return new TestResponseCreatedDTO.SustainabilityResult(data.result(), data.result());
  }

  private record CalculatedResults(SustainabilityResultData sustainabilityData) { }

  private record SustainabilityResultData(String result, SustainabilityPreferences preferences) {

  }
}