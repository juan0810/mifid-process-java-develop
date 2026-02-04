package com.singularbank.mifid.controller.history.mapper;

import com.singularbank.mifid.controller.history.response.HistoryTestResponseDTO;
import com.singularbank.mifid.controller.history.response.HistoryTestResponseDTO.TestItemDTO;
import com.singularbank.mifid.entity.HistoryTestItem;
import com.singularbank.mifid.entity.HistoryTestPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoryTestResponseMapper {

  @Mapping(target = "pagination", source = "pageSize")
  HistoryTestResponseDTO toResponse(HistoryTestPage page);

  @Mapping(target = "type", expression = "java(item.getType().name())")
  @Mapping(target = "state", expression = "java(item.getState().name())")
  TestItemDTO toItemDTO(HistoryTestItem item);
}
