package com.singularbank.mifid.service.convenience;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.StoreTestAnswers;

public interface SaveAnswersConvenienceTestService {

  TestResponseCreatedDTO saveAnswers(String documentNumber,
                                     StoreTestAnswers saveAnswers);
}
