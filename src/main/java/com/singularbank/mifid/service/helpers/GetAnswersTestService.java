package com.singularbank.mifid.service.helpers;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.TypeTest;

public interface GetAnswersTestService {

  AnswersTest get(
      Integer testId, TypeTest typeTest);
}