package com.singularbank.mifid.controller.status.mapper;

import com.singularbank.mifid.controller.status.request.StateTestRequest;
import com.singularbank.mifid.controller.status.response.UpdateTestStatusResponseDTO;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.StatusTestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring")
public interface StateTestMapper {

  @ValueMapping(source = "DRAFT", target = "DRAFT")
  @ValueMapping(source = "PENDING", target = "PENDING")
  @ValueMapping(source = "SIGNED", target = "SIGNED")
  @ValueMapping(source = "CANCELLED", target = "CANCELLED")
  StateTest toDomain(StateTestRequest request);

  @Mapping(target = "status", expression = "java(result.getStatus().name())")
  UpdateTestStatusResponseDTO toResponse(StatusTestResult result);
}