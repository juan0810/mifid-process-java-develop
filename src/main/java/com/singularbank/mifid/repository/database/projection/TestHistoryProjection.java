package com.singularbank.mifid.repository.database.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TestHistoryProjection {
  Integer getId();
  Integer getEstado();
  String getTipoTest();
  String getResultadoConveniencia();
  String getResultadoIdoneidad();
  LocalDateTime getFechaAlta();
  LocalDateTime getFechaFirma();
  LocalDate getFechaCaducidad();  // Es DATE, no DATETIME2
}