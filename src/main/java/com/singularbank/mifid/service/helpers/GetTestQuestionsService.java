package com.singularbank.mifid.service.helpers;

import com.singularbank.mifid.entity.QuestionsTest;
import com.singularbank.mifid.entity.TypeTest;

public interface GetTestQuestionsService {

  QuestionsTest getTest(
      String application,
      TypeTest typeTest,
      Short version
  );
}