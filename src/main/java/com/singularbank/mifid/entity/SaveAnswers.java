package com.singularbank.mifid.entity;

import java.util.List;

public record SaveAnswers(
    String service,
    Short version,
    List<TestBlock> tests
) {

  public record TestBlock(
      TypeTest type,
      List<QuestionResponse> questionResponses
  ) {

  }

  public record QuestionResponse(
      Integer questionId,
      Integer selectedOptionId
  ) {

  }
}
