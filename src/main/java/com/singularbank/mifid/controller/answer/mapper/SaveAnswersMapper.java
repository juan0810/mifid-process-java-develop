package com.singularbank.mifid.controller.answer.mapper;

import com.singularbank.mifid.controller.answer.request.SaveAnswersRequestDTO;
import com.singularbank.mifid.entity.SaveAnswers;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SaveAnswersMapper {

  SaveAnswers toDomain(SaveAnswersRequestDTO dto);
}