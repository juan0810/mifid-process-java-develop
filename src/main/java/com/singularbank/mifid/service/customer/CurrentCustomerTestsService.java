package com.singularbank.mifid.service.customer;

import com.singularbank.mifid.entity.CustomerActiveTests;

public interface CurrentCustomerTestsService {

  CustomerActiveTests getCurrentCustomerTestsByIdClient(String identificationClient);

  CustomerActiveTests getCurrentCustomerTestsById(Integer id);
}
