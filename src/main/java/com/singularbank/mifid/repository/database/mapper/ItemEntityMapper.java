package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.repository.database.model.ItemEntity;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ItemEntityMapper {

  public Question toDomain(ItemEntity entity) {
    if (entity == null) {
      return null;
    }

    List<Answer> answers = Collections.emptyList();
    List<Question> subQuestions = Collections.emptyList();

    if (entity.getSubItems() != null) {
      answers = entity.getSubItems().stream()
          .filter(item -> "RE".equals(item.getTipoItem()))
          .sorted((a, b) -> Short.compare(a.getOrden(), b.getOrden()))
          .map(this::toAnswer)
          .toList();

      subQuestions = entity.getSubItems().stream()
          .filter(item -> "PR".equals(item.getTipoItem()) || "AM".equals(item.getTipoItem()))
          .sorted((a, b) -> Short.compare(a.getOrden(), b.getOrden()))
          .map(this::toDomain)
          .toList();
    }

    return Question.builder()
        .id(entity.getId())
        .parentId(entity.getItemPadre() != null ? entity.getItemPadre().getId() : null)
        .typeId(entity.getTipoPregunta() != null ? entity.getTipoPregunta().getId() : null)
        .text(entity.getTexto())
        .order(entity.getOrden())
        .testType(entity.getTipoTest())
        .families(entity.getFamilias())
        .hasCorrectAnswer(shortToBoolean(entity.getTieneRespuestaCorrecta()))
        .startDate(entity.getFechaInicio())
        .endDate(entity.getFechaFin())
        .createdAt(entity.getFechaAlta())
        .updatedAt(entity.getFechaModificacion())
        .createdBy(entity.getUsuarioAlta())
        .updatedBy(entity.getUsuarioModificacion())
        .answers(answers)
        .subQuestions(subQuestions)
        .build();
  }

  public Answer toAnswer(ItemEntity entity) {
    if (entity == null) {
      return null;
    }

    Question question = null;
    if (entity.getItemPadre() != null) {
      ItemEntity padre = entity.getItemPadre();
      question = Question.builder()
          .id(padre.getId())
          .text(padre.getTexto())
          .testType(padre.getTipoTest())
          .order(padre.getOrden())
          .build();
    }

    return Answer.builder()
        .id(entity.getId())
        .question(question)
        .text(entity.getTexto())
        .value(entity.getValor())
        .correct(shortToBoolean(entity.getEsCorrecta()))
        .freeText(shortToBoolean(entity.getTextoLibre()))
        .enabledQuestionId(null)
        .createdAt(entity.getFechaAlta())
        .updatedAt(entity.getFechaModificacion())
        .createdBy(entity.getUsuarioAlta())
        .updatedBy(entity.getUsuarioModificacion())
        .build();
  }

  private Boolean shortToBoolean(Short value) {
    return value != null && value == 1;
  }
}