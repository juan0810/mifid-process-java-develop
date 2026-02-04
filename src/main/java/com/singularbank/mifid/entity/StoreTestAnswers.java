package com.singularbank.mifid.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTestAnswers {

  private String service;
  private Short version;
  private List<QuestionResponse> questionResponses;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuestionResponse {

    private Integer questionId;
    private Integer selectedOptionId;
  }
}