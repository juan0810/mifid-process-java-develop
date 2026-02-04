package com.singularbank.mifid.service.convenience.impl;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.entity.StoreTestAnswers;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.service.convenience.ConvenienceAlertCalculationService;
import com.singularbank.mifid.service.convenience.ConvenienceCalculatorService;
import com.singularbank.mifid.service.helpers.AnswersTestLoader;
import com.singularbank.mifid.service.helpers.AnswersTestLoader.LoadedAnswers;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenienceAlertCalculationServiceImpl implements ConvenienceAlertCalculationService {

  private final AnswersTestLoader answersTestLoader;
  private final ConvenienceCalculatorService convenienceCalculator;

  @Override
  public ConvenienceResult calculateAlerts(
      String documentNumber,
      StoreTestAnswers storeTestAnswers) {

    log.info("Preparing convenience test calculation for client: {}", documentNumber);

    Objects.requireNonNull(documentNumber, "documentNumber is required");
    Objects.requireNonNull(storeTestAnswers, "storeTestAnswers is required");

    List<Integer> answerIds = storeTestAnswers.getQuestionResponses().stream()
        .map(StoreTestAnswers.QuestionResponse::getSelectedOptionId)
        .toList();

    LoadedAnswers loadedAnswers = answersTestLoader.loadByIdsAndTestType(
        answerIds, TypeTest.CONVENIENCE);

    Map<Integer, String> responseValues = mapResponseValues(
        storeTestAnswers.getQuestionResponses(),
        loadedAnswers.answersMap()
    );

    ConvenienceResult result = convenienceCalculator.calculateConvenienceDetailed(
        responseValues,
        storeTestAnswers.getVersion()
    );

    log.info("Calculated convenience for client {} - Alerts: {}", documentNumber,
        result.getAlerts().size());
    logCalculationResults(result);

    return result;
  }

  private Map<Integer, String> mapResponseValues(
      List<StoreTestAnswers.QuestionResponse> questionResponses,
      Map<Integer, Answer> respuestasMap) {

    return questionResponses.stream()
        .filter(qr -> respuestasMap.containsKey(qr.getSelectedOptionId()))
        .filter(qr -> {
          Answer r = respuestasMap.get(qr.getSelectedOptionId());
          return r.getValue() != null && r.getQuestion() != null;
        })
        .collect(Collectors.toMap(
            qr -> respuestasMap.get(qr.getSelectedOptionId()).getQuestion().getId(),
            qr -> respuestasMap.get(qr.getSelectedOptionId()).getValue()
        ));
  }

  private void logCalculationResults(ConvenienceResult result) {
    if (!result.getConvenientFamilies().isEmpty()) {
      log.info("✓ Convenient families: {}", result.getConvenientFamilies());
    }
    if (!result.getNotConvenientFamilies().isEmpty()) {
      log.info("✗ Not convenient families: {}", result.getNotConvenientFamilies());
    }
    if (!result.getAlerts().isEmpty()) {
      log.warn("⚠ Alerts:");
      result.getAlerts().forEach(alert -> log.warn("  - {}", alert));
    }
  }
}