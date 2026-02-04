package com.singularbank.mifid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

  private Integer id;
  private Question question;
  private String text;
  private String value;
  private Boolean correct;
  private Boolean freeText;
  private Integer enabledQuestionId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
}