package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "MKTMCOMBINACIONESRESPUESTAS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CombinacionRespuestaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "CRID")
  private Integer id;

  @NotNull
  @Size(max = 2)
  @Column(name = "CRRESULTADO", nullable = false, length = 2)
  private String resultado;

  @Size(max = 255)
  @Column(name = "CRDESCRIPCION", length = 255)
  private String descripcion;

  @NotNull
  @Size(max = 900)
  @Column(name = "CRJUSTIFICACION", nullable = false, length = 900)
  private String justificacion;

  @NotNull
  @Size(max = 255)
  @Column(name = "CRMENSAJE", nullable = false, length = 255)
  private String mensaje;

  @NotNull
  @Size(max = 50)
  @Column(name = "CRUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @CreationTimestamp
  @Column(name = "CRFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @Size(max = 50)
  @Column(name = "CRUSUMOD", length = 50)
  private String usuarioModificacion;

  @Column(name = "CRFECMOD")
  private LocalDateTime fechaModificacion;

  @OneToMany(mappedBy = "combinacion", fetch = FetchType.LAZY)
  private List<RelCombinacionItemEntity> relItems;

  @OneToMany(mappedBy = "combinacion", fetch = FetchType.LAZY)
  private List<RelRespuestaCombinacionEntity> relRespuestas;
}
