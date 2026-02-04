package com.singularbank.mifid.service.helpers;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.BadRequestException;
import com.singularbank.mifid.repository.ItemRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnswersTestLoader {

  private final ItemRepository itemRepository;

  public LoadedAnswers loadByIds(List<Integer> answerIds) {
    log.debug("Loading {} answers", answerIds.size());

    List<Answer> answers = itemRepository.findAnswersByIds(answerIds);
    validateAllFound(answers, answerIds, null);

    return buildResult(answers);
  }

  public LoadedAnswers loadByIdsAndTestType(List<Integer> answerIds, TypeTest typeTest) {
    log.debug("Loading {} answers for test type: {}", answerIds.size(), typeTest.getCode());

    List<Answer> answers = itemRepository.findAnswersByIdsAndTestType(answerIds, typeTest);
    validateAllFound(answers, answerIds, typeTest);

    return buildResult(answers);
  }

  private void validateAllFound(List<Answer> answers, List<Integer> answerIds,
      TypeTest typeTest) {
    if (answers.size() != answerIds.size()) {
      Set<Integer> foundIds = answers.stream()
          .map(Answer::getId)
          .collect(Collectors.toSet());

      List<Integer> missingIds = answerIds.stream()
          .filter(id -> !foundIds.contains(id))
          .toList();

      String errorMessage = typeTest != null
          ? String.format(
          "Invalid answers for test type '%s' (%s). Answer IDs not found or don't belong to this test type: %s",
          typeTest.name(), typeTest.getCode(), missingIds)
          : "Answer IDs not found: " + missingIds;

      throw new BadRequestException(errorMessage);
    }

    log.debug("✓ All {} answers validated", answers.size());
  }

  private LoadedAnswers buildResult(List<Answer> answers) {
    Map<Integer, Answer> answersMap = answers.stream()
        .collect(Collectors.toMap(Answer::getId, r -> r));

    Map<Integer, Question> questionsMap = answers.stream()
        .filter(r -> r.getQuestion() != null)
        .collect(Collectors.toMap(
            r -> r.getQuestion().getId(),
            Answer::getQuestion,
            (existing, replacement) -> existing
        ));

    log.debug("✓ Built maps: {} answers, {} questions", answersMap.size(), questionsMap.size());

    return new LoadedAnswers(answers, answersMap, questionsMap);
  }

  public record LoadedAnswers(
      List<Answer> answers,
      Map<Integer, Answer> answersMap,
      Map<Integer, Question> questionsMap
  ) {

  }
}