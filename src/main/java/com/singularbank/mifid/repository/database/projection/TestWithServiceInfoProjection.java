package com.singularbank.mifid.repository.database.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TestWithServiceInfoProjection {
  Integer getId();
  String getIdentity();
  String getResultadoConveniencia();
  String getResultadoIdoneidad();
  LocalDateTime getFechaAlta();
  LocalDateTime getFechaModificacion();
  LocalDate getFechaCaducidad();
  LocalDateTime getFechaFirma();
  Short getVersionId();
  String getVersionDescripcion();
  LocalDate getVersionFechaFin();
  Boolean getVersionActiva();
  String getServicioNombre();
  String getServicioDescripcion();
  String getTipoTest(); // C, I, S
}