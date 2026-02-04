package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.AnswersTest.Option;
import com.singularbank.mifid.entity.AnswersTest.Question;
import com.singularbank.mifid.repository.database.model.RespuestaClienteDetalleEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RespuestaClienteDetalleEntityMapper {

  default AnswersTest toDomain(List<RespuestaClienteDetalleEntity> detalles) {
    if (detalles == null || detalles.isEmpty()) {
      return null;
    }
    var firstDetail = detalles.getFirst();
    return AnswersTest.builder()
        .testId(firstDetail.getRespuestaCliente().getId())
        .respuestaClienteId(firstDetail.getRespuestaCliente().getId())
        .version(firstDetail.getRespuestaCliente().getVersion().getId())
        .questions(toQuestions(detalles))
        .build();
  }

  @Mapping(target = "id", source = "item.itemPadre.id")
  @Mapping(target = "text", source = "item.itemPadre.texto")
  @Mapping(target = "option", source = ".")
  Question toQuestion(RespuestaClienteDetalleEntity detalle);

  @Mapping(target = "id", source = "item.id")
  @Mapping(target = "text", source = "item.texto")
  @Mapping(target = "score", source = "item.valor", qualifiedByName = "parseScore")
  @Mapping(target = "value", source = "item.valor")
  Option toOption(RespuestaClienteDetalleEntity detalle);

  List<Question> toQuestions(List<RespuestaClienteDetalleEntity> detalles);

  @Named("parseScore")
  default Integer parseScore(String valor) {
    if (valor == null || valor.isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(valor);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}