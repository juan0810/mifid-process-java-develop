package com.singularbank.mifid.controller.helpers.mapper;

import com.singularbank.mifid.controller.helpers.dto.OptionDTO;
import com.singularbank.mifid.controller.helpers.dto.QuestionDTO;
import com.singularbank.mifid.controller.helpers.dto.VersionDTO;
import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.QuestionsTest;
import com.singularbank.mifid.entity.Version;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class QuestionsTestMapper {

  public com.singularbank.mifid.controller.helpers.dto.QuestionsTest toDto(QuestionsTest domain) {
    if (domain == null) {
      return null;
    }

    return com.singularbank.mifid.controller.helpers.dto.QuestionsTest.builder()
        .version(toVersionDTO(domain.getVersion()))
        .questions(toQuestionDTOList(domain.getQuestions()))
        .build();
  }

  private VersionDTO toVersionDTO(Version version) {
    if (version == null) {
      return null;
    }

    return VersionDTO.builder()
        .id(version.getId())
        .releaseDate(version.getFechaAlta())
        .build();
  }

  private List<QuestionDTO> toQuestionDTOList(List<Question> questions) {
    if (CollectionUtils.isEmpty(questions)) {
      return Collections.emptyList();
    }

    return questions.stream()
        .map(this::toQuestionDTO)
        .toList();
  }

  private QuestionDTO toQuestionDTO(Question question) {
    return QuestionDTO.builder()
        .id(question.getId())
        .text(question.getText())
        .familyCode(question.getFamilies())
        .options(toOptionDTOList(question.getAnswers()))
        .subQuestions(toQuestionDTOList(question.getSubQuestions()))
        .build();
  }

  private List<OptionDTO> toOptionDTOList(List<Answer> answers) {
    if (CollectionUtils.isEmpty(answers)) {
      return Collections.emptyList();
    }

    return answers.stream()
        .map(this::toOptionDTO)
        .toList();
  }

  private OptionDTO toOptionDTO(Answer answer) {
    return OptionDTO.builder()
        .id(answer.getId())
        .text(answer.getText())
        .correct(Boolean.TRUE.equals(answer.getCorrect()))
        .build();
  }
}