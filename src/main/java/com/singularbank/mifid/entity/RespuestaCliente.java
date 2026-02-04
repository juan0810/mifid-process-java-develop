package com.singularbank.mifid.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaCliente implements Serializable {

  private Integer id;
  private String clienteDni;
  private Short versionId;
  private StateTest estado;
  private String resultadoConveniencia;
  private String resultadoIdoneidad;
  private String assessmentReference;
  private LocalDateTime fechaAlta;
  private LocalDateTime fechaModificacion;
  private LocalDateTime fechaFirma;
  private LocalDateTime fechaAnulacion;
  private LocalDate fechaCaducidad;
  private List<RespuestaDetalle> detalles;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RespuestaDetalle implements Serializable {

    private Integer respuestaId;
    private String valor;
    private Boolean correcta;

    private transient Answer answer;
  }
}