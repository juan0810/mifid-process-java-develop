package com.singularbank.mifid.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

  private Integer id;
  private Integer parentId;
  private Short typeId;
  private String text;
  private Short order;
  private String testType;
  private String families;
  private Boolean hasCorrectAnswer;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
  private List<Answer> answers;
  private List<Question> subQuestions;
}