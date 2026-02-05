package com.singularbank.mifid.service.convenience.impl;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.FamilyDTO;
import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO.TestResults;
import com.singularbank.mifid.entity.*;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.RelRespuestaCombinacionRepository;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.convenience.ConvenienceCalculatorService;
import com.singularbank.mifid.service.convenience.SaveAnswersConvenienceTestService;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.AnswersTestLoader.LoadedAnswers;
import com.singularbank.mifid.service.helpers.ResultTestDescriptionBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaveAnswersConvenienceTestServiceImpl implements SaveAnswersConvenienceTestService {

  private final AnswersTestLoader answersTestLoader;
  private final RespuestaClienteRepository respuestaClienteRepository;
  private final RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;
  private final RelRespuestaCombinacionRepository relRespuestaCombinacionRepository;
  private final ConvenienceCalculatorService convenienceCalculator;
  private final ResultTestDescriptionBuilder descriptionBuilder;

  public SaveAnswersConvenienceTestServiceImpl(
      AnswersTestLoader answersTestLoader,
      RespuestaClienteRepository respuestaClienteRepository,
      RespuestaClienteDetalleRepository respuestaClienteDetalleRepository,
      RelRespuestaCombinacionRepository relRespuestaCombinacionRepository,
      ConvenienceCalculatorService convenienceCalculator,
      ResultTestDescriptionBuilder descriptionBuilder) {
    this.answersTestLoader = answersTestLoader;
    this.respuestaClienteRepository = respuestaClienteRepository;
    this.respuestaClienteDetalleRepository = respuestaClienteDetalleRepository;
    this.relRespuestaCombinacionRepository = relRespuestaCombinacionRepository;
    this.convenienceCalculator = convenienceCalculator;
    this.descriptionBuilder = descriptionBuilder;
  }


  @Override
  public TestResponseCreatedDTO saveAnswers(String documentNumber, StoreTestAnswers saveAnswers) {

    log.info("Processing Convenience MiFID test answers for document: {}", documentNumber);

    Objects.requireNonNull(documentNumber, "documentNumber is required");
    Objects.requireNonNull(saveAnswers, "testAnswers is required");

    List<StoreTestAnswers.QuestionResponse> questionResponses = saveAnswers.getQuestionResponses();

    List<Integer> allAnswerIds = extractAnswerIds(questionResponses);
    LoadedAnswers loadedAnswers = answersTestLoader.loadByIds(allAnswerIds);

    validateTestTypes(questionResponses, loadedAnswers.answersMap());

    List<RespuestaDetalle> allDetails = buildDetails(questionResponses, loadedAnswers.answersMap());

    CalculatedResults results = calculateResults(questionResponses, loadedAnswers.answersMap(), saveAnswers.getVersion());

    Integer responseId = saveResponse(
            documentNumber,
            saveAnswers.getVersion(),
            results.convenienceResult(),
            allDetails,
            results.convenienceCombinationIds()
    );

    log.info("✓ Saved Convenience MiFID test - Response ID: {}", responseId);

    return buildResponse(responseId, results);
  }

  private void validateTestTypes(List<StoreTestAnswers.QuestionResponse> questionResponses, Map<Integer, Answer> answersMap) {
    log.debug("Validating test types");

    questionResponses.stream()
                    .map(qr -> new ValidationPair(
                            TypeTest.CONVENIENCE,
                            qr.getSelectedOptionId(),
                            answersMap.get(qr.getSelectedOptionId())
                    ))
            .forEach(this::validateQuestionType);

    log.debug("✓ Test types validated");
  }

  private record ValidationPair(TypeTest expectedTypeTest, Integer answerId, Answer answer) { }

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
   * Metodo principal de cálculo: Para Conveniencia.
   * Para Conveniencia mantenemos ID si el servicio legacy lo
   * requiere, o migramos a Orden si se actualizó.
   */
  private CalculatedResults calculateResults(
      List<StoreTestAnswers.QuestionResponse> questionResponses,
      Map<Integer, Answer> answersMap,
      Short versionId) {

    Map<Integer, String> responsesById = extractResponses(questionResponses, answersMap);
    var result = calculateConvenience(responsesById, versionId);

    String convenienceResultText = result.formatConvenientFamiliesAsCommaSeparated();

    return new CalculatedResults(convenienceResultText,  result.getCombinationIds());
  }

  private ConvenienceResult calculateConvenience(
      Map<Integer, String> responses,
      Short versionId) {
    log.info("Calculating convenience result");
    var result = convenienceCalculator.calculateConvenienceDetailed(responses, versionId);
    log.info("Convenience result: {}", result.formatConvenientFamiliesAsCommaSeparated());
    return result;
  }

  private Map<Integer, String> extractResponses(List<StoreTestAnswers.QuestionResponse> questionResponses,
      Map<Integer, Answer> answersMap) {
    return questionResponses.stream()
        .collect(Collectors.toMap(
            qr -> answersMap.get(qr.getSelectedOptionId()).getQuestion().getId(),
            qr -> answersMap.get(qr.getSelectedOptionId()).getValue()
        ));
  }

  private Integer saveResponse(
      String documentNumber,
      Short versionId,
      String convenienceResult,
      List<RespuestaDetalle> details,
      List<Integer> convenienceCombinationIds) {

    RespuestaCliente response = RespuestaCliente.builder()
        .clienteDni(documentNumber)
        .versionId(versionId)
        .estado(StateTest.DRAFT)
        .resultadoConveniencia(convenienceResult)
        .build();

    RespuestaCliente saved = respuestaClienteRepository.save(response);
    respuestaClienteDetalleRepository.saveAll(saved.getId(), details);

    saveCombinationRelations(saved.getId(), convenienceCombinationIds);

    log.debug("✓ Saved response ID: {} with {} details", saved.getId(), details.size());
    return saved.getId();
  }

  private void saveCombinationRelations(
      Integer responseId,
      List<Integer> convenienceCombinationIds) {

    List<Integer> allCombinationIds = new ArrayList<>();

    if (convenienceCombinationIds != null && !convenienceCombinationIds.isEmpty()) {
      allCombinationIds.addAll(convenienceCombinationIds);
      log.debug("Adding {} convenience combination IDs", convenienceCombinationIds.size());
    }

    if (!allCombinationIds.isEmpty()) {
      relRespuestaCombinacionRepository.saveAll(responseId, allCombinationIds);
      log.info("✓ Saved {} combination relations for response {}",
          allCombinationIds.size(), responseId);
    } else {
      log.debug("No combination IDs to save for response {}", responseId);
    }
  }

  private TestResponseCreatedDTO buildResponse(Integer responseId,  CalculatedResults results) {

    TestResponseCreatedDTO.ConvenienceResult convenienceDto = buildConvenienceResult(results.convenienceResult());

    TestResults testResults = new TestResults(convenienceDto, null, null);

    return new TestResponseCreatedDTO(responseId, testResults);
  }

  private TestResponseCreatedDTO.ConvenienceResult buildConvenienceResult(String convenienceResult) {
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

  private record CalculatedResults(
      String convenienceResult,
      List<Integer> convenienceCombinationIds
  ) { }
}