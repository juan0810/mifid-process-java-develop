package com.singularbank.mifid.controller.helpers.mapper;

import com.singularbank.mifid.controller.helpers.dto.AnswersTestRequestDTO;
import com.singularbank.mifid.entity.StoreTestAnswers;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswersTestRequestMapper {

  @Mapping(source = "version", target = "version")
  StoreTestAnswers toDomain(
      AnswersTestRequestDTO answersTestRequestDTO);

  List<StoreTestAnswers> toDomain(
      List<AnswersTestRequestDTO> answersTestRequestDTOList);
}