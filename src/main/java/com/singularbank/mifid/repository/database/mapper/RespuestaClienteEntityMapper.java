package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.repository.database.model.RespuestaClienteEntity;
import com.singularbank.mifid.repository.database.model.VersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {RespuestaDetalleEntityMapper.class})
public interface RespuestaClienteEntityMapper {

  @Mapping(target = "clienteDni", source = "identity")
  @Mapping(target = "versionId", source = "version.id")
  @Mapping(target = "estado", source = "estado", qualifiedByName = "shortToEstadoTest")
  @Mapping(target = "detalles", source = "detalles")
  RespuestaCliente toDomain(RespuestaClienteEntity entity);

  @Mapping(target = "identity", source = "clienteDni")
  @Mapping(target = "estado", source = "estado.code")
  @Mapping(target = "version", source = "versionId", qualifiedByName = "versionIdToEntity")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "fechaAlta", ignore = true)
  @Mapping(target = "fechaModificacion", ignore = true)
  @Mapping(target = "detalles", ignore = true)
  RespuestaClienteEntity toEntity(RespuestaCliente domain);

  @Named("shortToEstadoTest")
  default StateTest shortToEstadoTest(Short estado) {
    return StateTest.fromCode(estado);
  }

  @Named("versionIdToEntity")
  default VersionEntity versionIdToEntity(Short versionId) {
    if (versionId == null) {
      return null;
    }
    return VersionEntity.builder()
        .id(versionId)
        .build();
  }
}