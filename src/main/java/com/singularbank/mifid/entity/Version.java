package com.singularbank.mifid.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Version {

  private Short id;

  private String descripcion;

  private LocalDate fechaInicio;

  private LocalDate fechaFin;

  private Boolean activa;

  private String usuarioAlta;

  private LocalDateTime fechaAlta;

  private String usuarioModificacion;
}