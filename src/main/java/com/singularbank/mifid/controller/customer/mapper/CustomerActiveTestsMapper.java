package com.singularbank.mifid.controller.customer.mapper;

import com.singularbank.mifid.controller.customer.dto.CurrentCustomerTestsResponseDTO;
import com.singularbank.mifid.controller.customer.dto.CurrentCustomerTestsResponseDTO.ActiveTestDTO;
import com.singularbank.mifid.controller.customer.dto.CurrentCustomerTestsResponseDTO.FamilyDTO;
import com.singularbank.mifid.entity.CustomerActiveTests;
import com.singularbank.mifid.entity.CustomerActiveTests.ActiveTest;
import com.singularbank.mifid.entity.ProductFamily;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerActiveTestsMapper {

  CurrentCustomerTestsResponseDTO toDto(CustomerActiveTests customerActiveTests);

  ActiveTestDTO toDto(ActiveTest activeTest);

  default FamilyDTO toDto(ProductFamily family) {
    if (family == null) {
      return null;
    }
    return FamilyDTO.builder()
        .code(family.name())
        .description(family.getDescription())
        .build();
  }
}