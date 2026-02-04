package com.singularbank.mifid.controller.status.mapper;

import com.singularbank.mifid.controller.status.response.UpdateTestStatusResponseDTO;
import com.singularbank.mifid.entity.StatusTestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatusTestResponseMapper {

  @Mapping(target = "status", expression = "java(result.getStatus().name())")
  UpdateTestStatusResponseDTO toResponse(StatusTestResult result);
}