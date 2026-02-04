package com.singularbank.mifid.service.answer.impl;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.FamilyDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.TestResults;
import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.entity.CustomerActiveTests.SustainabilityPreferences;
import com.singularbank.mifid.entity.ProductFamily;
import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.SuitabilityResult;
import com.singularbank.mifid.entity.SaveAnswers;
import com.singularbank.mifid.entity.SaveAnswers.QuestionResponse;
import com.singularbank.mifid.entity.SaveAnswers.TestBlock;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.RelRespuestaCombinacionRepository;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.answer.SaveAnswersService;
import com.singularbank.mifid.service.convenience.ConvenienceCalculatorService;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.AnswersTestLoader.LoadedAnswers;
import com.singularbank.mifid.service.helpers.ResultTestDescriptionBuilder;
import com.singularbank.mifid.service.suitability.SuitabilityCalculatorService;
import com.singularbank.mifid.service.sustainability.SustainabilityCalculatorService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SaveAnswersServiceImpl implements SaveAnswersService {

  private final AnswersTestLoader answersTestLoader;
  private final RespuestaClienteRepository respuestaClienteRepository;
  private final RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;
  private final RelRespuestaCombinacionRepository relRespuestaCombinacionRepository;
  private final ConvenienceCalculatorService convenienceCalculator;
  private final SuitabilityCalculatorService suitabilityCalculator;
  private final SustainabilityCalculatorService sustainabilityCalculator;
  private final ResultTestDescriptionBuilder descriptionBuilder;

  public SaveAnswersServiceImpl(
      AnswersTestLoader answersTestLoader,
      RespuestaClienteRepository respuestaClienteRepository,
      RespuestaClienteDetalleRepository respuestaClienteDetalleRepository,
      RelRespuestaCombinacionRepository relRespuestaCombinacionRepository,
      ConvenienceCalculatorService convenienceCalculator,
      SuitabilityCalculatorService suitabilityCalculator,
      SustainabilityCalculatorService sustainabilityCalculator,
      ResultTestDescriptionBuilder descriptionBuilder) {
    this.answersTestLoader = answersTestLoader;
    this.respuestaClienteRepository = respuestaClienteRepository;
    this.respuestaClienteDetalleRepository = respuestaClienteDetalleRepository;
    this.relRespuestaCombinacionRepository = relRespuestaCombinacionRepository;
    this.convenienceCalculator = convenienceCalculator;
    this.suitabilityCalculator = suitabilityCalculator;
    this.sustainabilityCalculator = sustainabilityCalculator;
    this.descriptionBuilder = descriptionBuilder;
  }

  @Override
  @Transactional
  public TestResponseCreatedDTO saveAnswers(String documentNumber,
      SaveAnswers saveAnswers) {
    log.info("Processing MiFID test answers for document: {}", documentNumber);

    Objects.requireNonNull(documentNumber, "documentNumber is required");
    Objects.requireNonNull(saveAnswers, "testAnswers is required");

    validateNoDuplicates(saveAnswers.tests());
    List<TestBlock> sortedTests = sortByProcessingOrder(saveAnswers.tests());

    log.info("Processing {} test(s): {}",
        sortedTests.size(),
        sortedTests.stream().map(t -> t.type().name()).collect(Collectors.joining(", ")));

    List<Integer> allAnswerIds = extractAnswerIds(sortedTests);
    LoadedAnswers loadedAnswers = answersTestLoader.loadByIds(allAnswerIds);

    validateTestTypes(sortedTests, loadedAnswers.answersMap());

    List<RespuestaDetalle> allDetails = buildDetails(sortedTests, loadedAnswers.answersMap());

    CalculatedResults results = calculateResults(sortedTests, loadedAnswers.answersMap(),
        documentNumber, saveAnswers.version());

    Integer responseId = saveResponse(
        documentNumber,
        saveAnswers.version(),
        results.convenienceResult(),
        results.suitabilityResult(),
        allDetails,
        results.convenienceCombinationIds(),
        results.suitabilityCombinationId()
    );

    log.info("✓ Saved MiFID test - Response ID: {}", responseId);

    return buildResponse(responseId, results, sortedTests);
  }

  private void validateNoDuplicates(List<TestBlock> tests) {
    long uniqueTypes = tests.stream().map(TestBlock::type).distinct().count();

    if (uniqueTypes != tests.size()) {
      throw new BadRequestException("Duplicate test types found");
    }
  }

  private void validateTestTypes(List<TestBlock> tests, Map<Integer, Answer> answersMap) {
    log.debug("Validating test types");

    tests.stream()
        .flatMap(test -> test.questionResponses().stream()
            .map(qr -> new ValidationPair(
                test.type(),
                qr.selectedOptionId(),
                answersMap.get(qr.selectedOptionId())
            )))
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

  private List<Integer> extractAnswerIds(List<TestBlock> tests) {
    return tests.stream()
        .flatMap(test -> test.questionResponses().stream())
        .map(QuestionResponse::selectedOptionId)
        .toList();
  }

  private List<TestBlock> sortByProcessingOrder(List<TestBlock> tests) {
    return tests.stream()
        .sorted(Comparator.comparingInt(test -> switch (test.type()) {
          case CONVENIENCE -> 1;
          case SUITABILITY -> 2;
          case SUSTAINABILITY -> 3;
          default -> throw new IllegalStateException("Unexpected test type: " + test.type());
        }))
        .toList();
  }

  private List<RespuestaDetalle> buildDetails(List<TestBlock> tests,
      Map<Integer, Answer> answersMap) {
    return tests.stream()
        .flatMap(test -> test.questionResponses().stream())
        .map(qr -> {
          Answer answer = answersMap.get(qr.selectedOptionId());
          return RespuestaDetalle.builder()
              .respuestaId(answer.getId())
              .valor(answer.getValue())
              .correcta(answer.getCorrect())
              .build();
        })
        .toList();
  }

  // ... imports y estructura de clase anteriores ...

  /**
   * Método principal de cálculo. CORRECCIÓN: Para Idoneidad y Sostenibilidad usamos mapas basados
   * en ORDEN (independiente del entorno). Para Conveniencia mantenemos ID si el servicio legacy lo
   * requiere, o migramos a Orden si se actualizó.
   */
  private CalculatedResults calculateResults(
      List<TestBlock> tests,
      Map<Integer, Answer> answersMap,
      String documentNumber,
      Short versionId) {

    String convenienceResultText = null;
    String suitabilityResultText = null;
    SustainabilityResultData sustainabilityData = null;

    Map<Integer, String> convenienceResponsesByOrder = null;

    List<Integer> convenienceCombinationIds = null;
    Integer suitabilityCombinationId = null;

    for (TestBlock test : tests) {
      switch (test.type()) {
        case CONVENIENCE -> {
          Map<Integer, String> responsesById = extractResponses(test, answersMap);
          var result = calculateConvenience(responsesById, versionId);

          convenienceResultText = result.formatConvenientFamiliesAsCommaSeparated();
          convenienceCombinationIds = result.getCombinationIds();
          convenienceResponsesByOrder = extractResponsesByOrder(test, answersMap);
        }
        case SUITABILITY -> {
          Map<Integer, String> suitabilityResponsesByOrder = extractResponsesByOrder(test,
              answersMap);

          if (convenienceResponsesByOrder == null) {
            convenienceResponsesByOrder = loadPreviousConvenienceByOrder(documentNumber, versionId);
          }

          var result = calculateSuitability(suitabilityResponsesByOrder,
              convenienceResponsesByOrder);

          suitabilityResultText = result.getResult();
          suitabilityCombinationId = result.combinationId();
        }
        case SUSTAINABILITY -> {
          Map<Integer, String> responsesByOrder = extractResponsesByOrder(test, answersMap);
          sustainabilityData = calculateSustainability(responsesByOrder);
        }
        default -> throw new IllegalStateException("Unexpected test type: " + test.type());
      }
    }

    return new CalculatedResults(
        convenienceResultText,
        suitabilityResultText,
        sustainabilityData,
        convenienceCombinationIds,
        suitabilityCombinationId
    );
  }

  private ConvenienceResult calculateConvenience(
      Map<Integer, String> responses,
      Short versionId) {
    log.info("Calculating convenience result");
    var result = convenienceCalculator.calculateConvenienceDetailed(responses, versionId);
    log.info("Convenience result: {}", result.formatConvenientFamiliesAsCommaSeparated());
    return result;
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

  private Map<Integer, String> extractResponsesByOrder(TestBlock test,
      Map<Integer, Answer> answersMap) {
    return test.questionResponses().stream()
        .collect(Collectors.toMap(
            qr -> {
              Answer answer = answersMap.get(qr.selectedOptionId());
              if (answer == null || answer.getQuestion() == null) {
                throw new BadRequestException(
                    "Data integrity error: Answer/Question not found for ID "
                        + qr.selectedOptionId());
              }
              return answer.getQuestion().getOrder().intValue();
            },
            qr -> answersMap.get(qr.selectedOptionId()).getValue()
        ));
  }

  private Map<Integer, String> loadPreviousConvenienceByOrder(String identity, Short versionId) {
    log.info("🔍 Loading previous CONVENIENCE by ORDER for identity: {}, versionId: {}", identity,
        versionId);

    var optionalConv = respuestaClienteRepository.findConvenienceTestByIdentityAndVersion(identity,
        versionId);

    if (optionalConv.isEmpty()) {
      throw new BadRequestException(
          "Convenience test required before suitability for client " + identity);
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

    log.info("📋 Loaded {} convenience responses (by order) from previous test",
        convResponses.size());
    return convResponses;
  }

  private SustainabilityResultData calculateSustainability(Map<Integer, String> responses) {
    log.info("Calculating sustainability result");

    AnswersTest answersTest = buildAnswersTest(responses);

    String result = sustainabilityCalculator.calculateSustainabilityResult(answersTest);
    SustainabilityPreferences preferences =
        sustainabilityCalculator.extractSustainabilityPreferences(answersTest);

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

  private Map<Integer, String> extractResponses(TestBlock test,
      Map<Integer, Answer> answersMap) {
    return test.questionResponses().stream()
        .collect(Collectors.toMap(
            qr -> answersMap.get(qr.selectedOptionId()).getQuestion().getId(),
            qr -> answersMap.get(qr.selectedOptionId()).getValue()
        ));
  }

  private Integer saveResponse(
      String documentNumber,
      Short versionId,
      String convenienceResult,
      String suitabilityResult,
      List<RespuestaDetalle> details,
      List<Integer> convenienceCombinationIds,
      Integer suitabilityCombinationId) {

    RespuestaCliente response = RespuestaCliente.builder()
        .clienteDni(documentNumber)
        .versionId(versionId)
        .estado(StateTest.DRAFT)
        .resultadoConveniencia(convenienceResult)
        .resultadoIdoneidad(suitabilityResult)
        .build();

    RespuestaCliente saved = respuestaClienteRepository.save(response);
    respuestaClienteDetalleRepository.saveAll(saved.getId(), details);

    saveCombinationRelations(saved.getId(), convenienceCombinationIds, suitabilityCombinationId);

    log.debug("✓ Saved response ID: {} with {} details", saved.getId(), details.size());
    return saved.getId();
  }

  private void saveCombinationRelations(
      Integer responseId,
      List<Integer> convenienceCombinationIds,
      Integer suitabilityCombinationId) {

    List<Integer> allCombinationIds = new ArrayList<>();

    if (convenienceCombinationIds != null && !convenienceCombinationIds.isEmpty()) {
      allCombinationIds.addAll(convenienceCombinationIds);
      log.debug("Adding {} convenience combination IDs", convenienceCombinationIds.size());
    }

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
      CalculatedResults results,
      List<TestBlock> tests) {

    TestResponseCreatedDTO.ConvenienceResult convenienceDto = null;
    TestResponseCreatedDTO.SuitabilityResult suitabilityDto = null;
    TestResponseCreatedDTO.SustainabilityResult sustainabilityDto = null;

    for (TestBlock test : tests) {
      switch (test.type()) {
        case CONVENIENCE -> convenienceDto = buildConvenienceResult(results.convenienceResult());
        case SUITABILITY -> suitabilityDto = buildSuitabilityResult(results.suitabilityResult());
        case SUSTAINABILITY ->
            sustainabilityDto = buildSustainabilityResult(results.sustainabilityData());
        default -> throw new IllegalStateException("Unexpected test type: " + test.type());
      }
    }

    TestResults testResults = new TestResults(convenienceDto, suitabilityDto, sustainabilityDto);

    return new TestResponseCreatedDTO(responseId, testResults);
  }

  private TestResponseCreatedDTO.ConvenienceResult buildConvenienceResult(
      String convenienceResult) {
    if (convenienceResult == null) {
      return null;
    }

    String description = descriptionBuilder.buildConvenienceDescription(convenienceResult);
    List<ProductFamily> families = descriptionBuilder.parseConvenienceFamilies(convenienceResult);

    List<FamilyDTO> familyDTOs = families.stream()
        .map(family -> new FamilyDTO(family.name(), family.getDescription()))
        .toList();

    return new TestResponseCreatedDTO.ConvenienceResult(convenienceResult, description, familyDTOs);
  }

  private TestResponseCreatedDTO.SuitabilityResult buildSuitabilityResult(
      String suitabilityResult) {
    if (suitabilityResult == null) {
      return null;
    }

    String description = descriptionBuilder.buildSuitabilityDescription(suitabilityResult);

    return new TestResponseCreatedDTO.SuitabilityResult(suitabilityResult, description);
  }

  private TestResponseCreatedDTO.SustainabilityResult buildSustainabilityResult(
      SustainabilityResultData data) {
    if (data == null) {
      return null;
    }

    return new TestResponseCreatedDTO.SustainabilityResult(data.result(), data.result());
  }

  private record CalculatedResults(
      String convenienceResult,
      String suitabilityResult,
      SustainabilityResultData sustainabilityData,
      List<Integer> convenienceCombinationIds,
      Integer suitabilityCombinationId
  ) {

  }

  private record SustainabilityResultData(String result, SustainabilityPreferences preferences) {

  }
}