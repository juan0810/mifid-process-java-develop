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
public class AnswersTest {

  private Integer testId;
  private Integer respuestaClienteId;
  private Short version;
  private List<Question> questions;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Question {

    private Integer id;
    private String text;
    private Option option;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Option {

    private Integer id;
    private String text;
    private Integer score;
    private String value;
  }
}
