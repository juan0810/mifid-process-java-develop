package com.singularbank.mifid.service.suitability;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.SaveAnswers;
import com.singularbank.mifid.entity.StoreTestAnswers;

public interface SaveAnswersSuitabilityService {

  TestResponseCreatedDTO saveAnswers(String documentNumber, StoreTestAnswers saveAnswers);
}
