package com.singularbank.mifid.service.suitability.impl;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.TestResults;
import com.singularbank.mifid.entity.*;
import com.singularbank.mifid.entity.CustomerActiveTests.SustainabilityPreferences;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.RelRespuestaCombinacionRepository;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.AnswersTestLoader.LoadedAnswers;
import com.singularbank.mifid.service.helpers.ResultTestDescriptionBuilder;
import com.singularbank.mifid.service.suitability.SaveAnswersSuitabilityService;
import com.singularbank.mifid.service.suitability.SuitabilityCalculatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaveAnswersSuitabilityServiceImpl implements SaveAnswersSuitabilityService {

  private final AnswersTestLoader answersTestLoader;
  private final RespuestaClienteRepository respuestaClienteRepository;
  private final RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;
  private final RelRespuestaCombinacionRepository relRespuestaCombinacionRepository;
  private final SuitabilityCalculatorService suitabilityCalculator;
  private final ResultTestDescriptionBuilder descriptionBuilder;

  public SaveAnswersSuitabilityServiceImpl(
      AnswersTestLoader answersTestLoader,
      RespuestaClienteRepository respuestaClienteRepository,
      RespuestaClienteDetalleRepository respuestaClienteDetalleRepository,
      RelRespuestaCombinacionRepository relRespuestaCombinacionRepository,
      SuitabilityCalculatorService suitabilityCalculator,
      ResultTestDescriptionBuilder descriptionBuilder) {
    this.answersTestLoader = answersTestLoader;
    this.respuestaClienteRepository = respuestaClienteRepository;
    this.respuestaClienteDetalleRepository = respuestaClienteDetalleRepository;
    this.relRespuestaCombinacionRepository = relRespuestaCombinacionRepository;
    this.suitabilityCalculator = suitabilityCalculator;
    this.descriptionBuilder = descriptionBuilder;
  }

  @Override
  @Transactional
  public TestResponseCreatedDTO saveAnswers(String documentNumber, StoreTestAnswers saveAnswers) {
    log.info("Processing MiFID test answers for document: {}", documentNumber);

    Objects.requireNonNull(documentNumber, "documentNumber is required");
    Objects.requireNonNull(saveAnswers, "testAnswers is required");

    List<StoreTestAnswers.QuestionResponse> questionResponses = saveAnswers.getQuestionResponses();

    List<Integer> allAnswerIds = extractAnswerIds(questionResponses);
    LoadedAnswers loadedAnswers = answersTestLoader.loadByIds(allAnswerIds);

    validateTestTypes(questionResponses, loadedAnswers.answersMap());

    List<RespuestaDetalle> allDetails = buildDetails(questionResponses, loadedAnswers.answersMap());

    CalculatedResults results = calculateResults(questionResponses, loadedAnswers.answersMap(), documentNumber, saveAnswers.getVersion());

    Integer responseId = saveResponse(
        documentNumber,
        saveAnswers.getVersion(),
        results.suitabilityResult(),
        allDetails,
        results.suitabilityCombinationId()
    );

    log.info("✓ Saved Suitability MiFID test - Response ID: {}", responseId);

    return buildResponse(responseId, results);
  }

  private void validateTestTypes(List<StoreTestAnswers.QuestionResponse> questionResponses, Map<Integer, Answer> answersMap) {
    log.debug("Validating test types");

    questionResponses.stream()
            .map(qr -> new ValidationPair(
                    TypeTest.SUITABILITY,
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
   * Metodo principal de cálculo: Para Idoneidad
   */
  private CalculatedResults calculateResults(
      List<StoreTestAnswers.QuestionResponse> questionResponses,
      Map<Integer, Answer> answersMap,
      String documentNumber,
      Short versionId) {

    Map<Integer, String> suitabilityResponsesByOrder = extractResponsesByOrder(questionResponses, answersMap);

    Map<Integer, String> convenienceResponsesByOrder = loadPreviousConvenienceByOrder(documentNumber, versionId);

    var result = calculateSuitability(suitabilityResponsesByOrder,  convenienceResponsesByOrder);

    return new CalculatedResults(result.getResult(), result.combinationId());
  }

  private SuitabilityResult calculateSuitability(
      Map<Integer, String> responses,
      Map<Integer, String> convenienceResponses) {

    log.info("Calculating suitability result");

    if (convenienceResponses == null || convenienceResponses.isEmpty()) {
      throw new BadRequestException("Convenience test required for suitability calculation");
    }

    var result = suitabilityCalculator.calculateWithConvenience(responses, convenienceResponses);
    log.info("Suitability result: {}", result.getResult());
    return result;
  }

  private Map<Integer, String> extractResponsesByOrder(List<StoreTestAnswers.QuestionResponse> questionResponses, Map<Integer, Answer> answersMap) {
    return questionResponses.stream()
        .collect(Collectors.toMap(
            qr -> {
              Answer answer = answersMap.get(qr.getSelectedOptionId());
              if (answer == null || answer.getQuestion() == null) {
                throw new BadRequestException("Data integrity error: Answer/Question not found for ID " + qr.getSelectedOptionId());
              }
              return answer.getQuestion().getOrder().intValue();
            },
            qr -> answersMap.get(qr.getSelectedOptionId()).getValue()
        ));
  }

  private Map<Integer, String> loadPreviousConvenienceByOrder(String identity, Short versionId) {
    log.info("🔍 Loading previous CONVENIENCE by ORDER for identity: {}, versionId: {}", identity, versionId);

    var optionalConv = respuestaClienteRepository.findConvenienceTestByIdentityAndVersion(identity, versionId);

    if (optionalConv.isEmpty()) {
      throw new BadRequestException("Convenience test required before suitability for client " + identity);
    }

    RespuestaCliente conv = optionalConv.get();
    List<RespuestaDetalle> convDetails = conv.getDetalles();

    if (convDetails == null || convDetails.isEmpty()) {
      throw new BadRequestException("Convenience test details empty for client " + identity);
    }

    Map<Integer, String> convResponses = convDetails.stream()
        .collect(Collectors.toMap(
            d -> d.getAnswer().getQuestion().getOrder().intValue(),
            d -> d.getAnswer().getValue() != null ? d.getAnswer().getValue() : d.getValor()
        ));

    log.info("📋 Loaded {} convenience responses (by order) from previous test", convResponses.size());
    return convResponses;
  }

  private Integer saveResponse(
      String documentNumber,
      Short versionId,
      String suitabilityResult,
      List<RespuestaDetalle> details,
      Integer suitabilityCombinationId) {

    RespuestaCliente response = RespuestaCliente.builder()
        .clienteDni(documentNumber)
        .versionId(versionId)
        .estado(StateTest.DRAFT)
        .resultadoIdoneidad(suitabilityResult)
        .build();

    RespuestaCliente saved = respuestaClienteRepository.save(response);
    respuestaClienteDetalleRepository.saveAll(saved.getId(), details);

    saveCombinationRelations(saved.getId(), suitabilityCombinationId);

    log.debug("✓ Saved response ID: {} with {} details", saved.getId(), details.size());
    return saved.getId();
  }

  private void saveCombinationRelations(
      Integer responseId,
      Integer suitabilityCombinationId) {

    List<Integer> allCombinationIds = new ArrayList<>();

    if (suitabilityCombinationId != null) {
      allCombinationIds.add(suitabilityCombinationId);
      log.debug("Adding suitability combination ID: {}", suitabilityCombinationId);
    }

    if (!allCombinationIds.isEmpty()) {
      relRespuestaCombinacionRepository.saveAll(responseId, allCombinationIds);
      log.info("✓ Saved {} combination relations for response {}",
          allCombinationIds.size(), responseId);
    } else {
      log.debug("No combination IDs to save for response {}", responseId);
    }
  }

  private TestResponseCreatedDTO buildResponse(
      Integer responseId,
      CalculatedResults results) {

    TestResponseCreatedDTO.SuitabilityResult suitabilityDto = buildSuitabilityResult(results.suitabilityResult());

    TestResults testResults = new TestResults(null, suitabilityDto, null);

    return new TestResponseCreatedDTO(responseId, testResults);
  }

  private TestResponseCreatedDTO.SuitabilityResult buildSuitabilityResult(String suitabilityResult) {
    if (suitabilityResult == null) {
      return null;
    }

    String description = descriptionBuilder.buildSuitabilityDescription(suitabilityResult);

    return new TestResponseCreatedDTO.SuitabilityResult(suitabilityResult, description);
  }

  private record CalculatedResults(
      String suitabilityResult,
      Integer suitabilityCombinationId
  ) { }

  private record SustainabilityResultData(String result, SustainabilityPreferences preferences) { }
}