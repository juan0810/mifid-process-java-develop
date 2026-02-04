package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MKTMRELVERSIONPREGUNTA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class VersionPreguntaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "VPID")
  private Integer id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VPVERSIONID", nullable = false)
  private VersionEntity version;

  // CAMBIO IMPORTANTE: Apunta a ItemEntity (tabla MKTMITEMS)
// aunque la columna se llame VPPREGUNTAID
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VPPREGUNTAID", nullable = false)
  private ItemEntity item;

  @NotNull
  @Size(max = 50)
  @Column(name = "VPUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @CreationTimestamp
  @Column(name = "VPFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

}