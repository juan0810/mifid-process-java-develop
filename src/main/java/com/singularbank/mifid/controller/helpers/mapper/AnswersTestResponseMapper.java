package com.singularbank.mifid.controller.helpers.mapper;

import com.singularbank.mifid.controller.helpers.dto.AnswersTestResponseDTO;
import com.singularbank.mifid.entity.AnswersTest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswersTestResponseMapper {

  @Mapping(target = "questions", source = "questions")
  AnswersTestResponseDTO toDto(AnswersTest response);

  AnswersTestResponseDTO.QuestionDTO toQuestionDTO(
      AnswersTest.Question question);

  AnswersTestResponseDTO.OptionDTO toOptionDTO(AnswersTest.Option option);
}