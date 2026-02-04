package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "MKTMRESPUESTACLIENTEDETALLE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RespuestaClienteDetalleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "respuesta_detalle_seq")
  @SequenceGenerator(
      name = "respuesta_detalle_seq",
      sequenceName = "MKTMRESPUESTACLIENTEDETALLE_RDID_SEQ",
      allocationSize = 50
  )
  @Column(name = "RDID")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RDRESPUESTACLIENTEID", nullable = false)
  private RespuestaClienteEntity respuestaCliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RDRESPUESTAID", nullable = false)
  private ItemEntity item;

  @Column(name = "RDVALOR", columnDefinition = "TEXT")
  private String valor;

  @CreationTimestamp
  @Column(name = "RDFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @Column(name = "RDFECMOD")
  private LocalDateTime fechaModificacion;
}