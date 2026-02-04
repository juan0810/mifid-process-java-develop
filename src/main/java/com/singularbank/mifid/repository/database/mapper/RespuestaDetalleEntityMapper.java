package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.repository.database.model.RespuestaClienteDetalleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RespuestaDetalleEntityMapper {

  @Mapping(target = "respuestaId", source = "item.id")
  @Mapping(target = "valor", source = "valor")
  @Mapping(target = "correcta", constant = "false")
  @Mapping(target = "answer", source = "item", qualifiedByName = "itemToAnswer")
  RespuestaDetalle toDomain(RespuestaClienteDetalleEntity entity);

  @Named("itemToAnswer")
  default Answer itemToAnswer(com.singularbank.mifid.repository.database.model.ItemEntity itemEntity) {
    if (itemEntity == null) {
      return null;
    }
    
    com.singularbank.mifid.entity.Question question = null;
    if (itemEntity.getItemPadre() != null) {
      question = com.singularbank.mifid.entity.Question.builder()
          .id(itemEntity.getItemPadre().getId())
          .text(itemEntity.getItemPadre().getTexto())
          .order(itemEntity.getItemPadre().getOrden())
          .testType(itemEntity.getItemPadre().getTipoTest())
          .build();
    }
    
    return Answer.builder()
        .id(itemEntity.getId())
        .text(itemEntity.getTexto())
        .value(itemEntity.getValor())
        .question(question)
        .build();
  }
}
