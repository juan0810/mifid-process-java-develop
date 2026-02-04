package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "MKTMRELCOMBINACIONITEM")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RelCombinacionItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "CIID")
  private Integer id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CIIDCOMBINACION", nullable = false)
  private CombinacionRespuestaEntity combinacion;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CIIDITEM", nullable = false)
  private ItemEntity item;

  @NotNull
  @Size(max = 50)
  @Column(name = "CIUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @CreationTimestamp
  @Column(name = "CIFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @Size(max = 50)
  @Column(name = "CIUSUMOD", length = 50)
  private String usuarioModificacion;

  @Column(name = "CIFECMOD")
  private LocalDateTime fechaModificacion;
}
