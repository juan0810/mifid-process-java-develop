package com.singularbank.mifid.controller.helpers.mapper;

import com.singularbank.mifid.controller.helpers.dto.AlertsTestResponseDTO;
import com.singularbank.mifid.controller.helpers.dto.AlertsTestResponseDTO.AlertDTO;
import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.entity.ProductFamily;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConvenienceResultMapper {

  @Mapping(target = "result", expression = "java(convenienceResult.formatResultText())")
  @Mapping(target = "alerts", source = "notConvenientFamilies")
  AlertsTestResponseDTO toDto(ConvenienceResult convenienceResult);

  default List<AlertDTO> map(Set<String> notConvenientFamilies) {
    if (notConvenientFamilies == null || notConvenientFamilies.isEmpty()) {
      return Collections.emptyList();
    }

    return notConvenientFamilies.stream()
        .sorted()
        .map(ProductFamily::fromCode)
        .filter(Objects::nonNull)
        .map(this::toAlertDTO)
        .toList();
  }

  default AlertDTO toAlertDTO(ProductFamily family) {
    return AlertDTO.builder()
        .code(family.name())
        .description(family.getDescription())
        .build();
  }
}