package com.singularbank.mifid.service.answer;

import com.singularbank.mifid.controller.helpers.dto.TestResponseCreatedDTO;
import com.singularbank.mifid.entity.SaveAnswers;

public interface SaveAnswersService {

  TestResponseCreatedDTO saveAnswers(String documentNumber,
      SaveAnswers saveAnswers);
}
