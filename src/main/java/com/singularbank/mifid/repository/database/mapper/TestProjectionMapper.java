package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.CompleteTestInfo;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.database.projection.TestWithServiceInfoProjection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TestProjectionMapper {

  @Mapping(target = "testId", source = "id")
  @Mapping(target = "customerIdentity", source = "identity")
  @Mapping(target = "convenienceResult", source = "resultadoConveniencia")
  @Mapping(target = "suitabilityResult", source = "resultadoIdoneidad")
  @Mapping(target = "creationDate", source = "fechaAlta")
  @Mapping(target = "signatureDate", source = "fechaFirma")
  // TODO: Cambiar cuando se añada el campo real
  @Mapping(target = "modificationDate", source = "fechaModificacion")
  @Mapping(target = "versionId", source = "versionId")
  @Mapping(target = "versionDescription", source = "versionDescripcion")
  @Mapping(target = "versionExpirationDate", source = "versionFechaFin")
  @Mapping(target = "versionActive", source = "versionActiva")
  @Mapping(target = "serviceName", source = "servicioNombre")
  @Mapping(target = "serviceDescription", source = "servicioDescripcion")
  @Mapping(target = "expirationDate", source = "fechaCaducidad")
  @Mapping(target = "typeTest", source = "tipoTest", qualifiedByName = "stringToTestType")
  CompleteTestInfo toDomain(TestWithServiceInfoProjection projection);

  List<CompleteTestInfo> toDomain(List<TestWithServiceInfoProjection> projections);

  @Named("stringToTestType")
  default TypeTest stringToTestType(String tipoTest) {
    return tipoTest != null ? TypeTest.fromCode(tipoTest) : null;
  }
}