package com.singularbank.mifid.entity;

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
public class CombinacionRespuesta {

  private Integer id;
  private String resultado;
  private String descripcion;
  private String justificacion;
  private String mensaje;
  private LocalDateTime fechaAlta;
  private LocalDateTime fechaModificacion;
  private String usuarioAlta;
  private String usuarioModificacion;
  private List<Item> items;

  public enum Resultado {
    ALTO("AL"),
    MEDIO("ME"),
    BAJO("BA"),
    NO_APTO("NA"),
    APTO("AP");

    private final String codigo;

    Resultado(String codigo) {
      this.codigo = codigo;
    }

    public String getCodigo() {
      return codigo;
    }

    public static Resultado fromCodigo(String codigo) {
      return switch (codigo) {
        case "AL" -> ALTO;
        case "ME" -> MEDIO;
        case "BA" -> BAJO;
        case "NA" -> NO_APTO;
        case "AP" -> APTO;
        default -> throw new IllegalArgumentException("Resultado inválido: " + codigo);
      };
    }
  }
}
