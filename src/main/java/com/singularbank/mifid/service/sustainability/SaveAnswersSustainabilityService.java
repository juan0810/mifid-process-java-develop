package com.singularbank.mifid.service.sustainability;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.SaveAnswers;
import com.singularbank.mifid.entity.StoreTestAnswers;

public interface SaveAnswersSustainabilityService {

  TestResponseCreatedDTO saveAnswers(String documentNumber, StoreTestAnswers saveAnswers);
}
