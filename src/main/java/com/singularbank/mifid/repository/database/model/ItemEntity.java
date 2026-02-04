package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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
@Table(name = "MKTMITEMS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ITID")
  private Integer id;

  @NotNull
  @Size(max = 2)
  @Column(name = "ITTIPOITEM", nullable = false, length = 2)
  private String tipoItem;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITTIPOPREGUNTAID", nullable = false)
  private PreguntasTipoEntity tipoPregunta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ITDEPENDEDE")
  private ItemEntity itemPadre;

  @NotNull
  @Size(max = 500)
  @Column(name = "ITTEXTO", nullable = false, length = 500)
  private String texto;

  @NotNull
  @Column(name = "ITORDEN", nullable = false)
  private Short orden;

  @Size(max = 500)
  @Column(name = "ITVALOR", length = 500)
  private String valor;

  @Column(name = "ITTEXTOLIBRE")
  private Short textoLibre;

  @Size(max = 2)
  @Column(name = "ITTIPOTEST", length = 2)
  private String tipoTest;

  @Size(max = 50)
  @Column(name = "ITFAMILIAS", length = 50)
  private String familias;

  @Column(name = "ITTIENERESPCORRECTA")
  private Short tieneRespuestaCorrecta;

  @Column(name = "ITESCORRECTA")
  private Short esCorrecta;

  @NotNull
  @Column(name = "ITFECINICIO", nullable = false)
  private LocalDate fechaInicio;

  @Column(name = "ITFECFIN")
  private LocalDate fechaFin;

  @NotNull
  @Size(max = 50)
  @Column(name = "ITUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @CreationTimestamp
  @Column(name = "ITFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @Size(max = 50)
  @Column(name = "ITUSUMOD", length = 50)
  private String usuarioModificacion;

  @Column(name = "ITFECMOD")
  private LocalDateTime fechaModificacion;

  @OneToMany(mappedBy = "itemPadre", fetch = FetchType.LAZY)
  private List<ItemEntity> subItems;

  @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
  private List<RelCombinacionItemEntity> relCombinaciones;

  @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
  private List<VersionPreguntaEntity> versiones;
}