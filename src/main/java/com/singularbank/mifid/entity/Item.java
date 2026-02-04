package com.singularbank.mifid.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

  private Integer id;
  private String tipoItem;
  private Short tipoPreguntaId;
  private Integer dependeDe;
  private String texto;
  private Short orden;
  private String valor;
  private Boolean textoLibre;
  private String tipoTest;
  private String familias;
  private Boolean tieneRespuestaCorrecta;
  private Boolean esCorrecta;
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private LocalDateTime fechaAlta;
  private LocalDateTime fechaModificacion;
  private String usuarioAlta;
  private String usuarioModificacion;
  private List<Item> subItems;

  public enum TipoItem {
    PREGUNTA("PR"),
    RESPUESTA("RE"),
    AMBAS("AM");

    private final String codigo;

    TipoItem(String codigo) {
      this.codigo = codigo;
    }

    public String getCodigo() {
      return codigo;
    }

    public static TipoItem fromCodigo(String codigo) {
      return switch (codigo) {
        case "PR" -> PREGUNTA;
        case "RE" -> RESPUESTA;
        case "AM" -> AMBAS;
        default -> throw new IllegalArgumentException("Tipo de item inválido: " + codigo);
      };
    }
  }

  public enum TipoTest {
    CONVENIENCIA("CO"),
    IDONEIDAD("ID"),
    SOSTENIBILIDAD("SO"),
    NO_APLICA("NA");

    private final String codigo;

    TipoTest(String codigo) {
      this.codigo = codigo;
    }

    public String getCodigo() {
      return codigo;
    }

    public static TipoTest fromCodigo(String codigo) {
      return switch (codigo) {
        case "CO" -> CONVENIENCIA;
        case "ID" -> IDONEIDAD;
        case "SO" -> SOSTENIBILIDAD;
        case "NA" -> NO_APLICA;
        default -> throw new IllegalArgumentException("Tipo de test inválido: " + codigo);
      };
    }
  }
}
