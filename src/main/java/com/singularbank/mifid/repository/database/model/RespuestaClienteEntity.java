package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "MKTMRESPUESTACLIENTE")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RespuestaClienteEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "respuesta_cliente_seq")
  @SequenceGenerator(
      name = "respuesta_cliente_seq",
      sequenceName = "MKTMRESPUESTACLIENTE_RCID_SEQ",
      allocationSize = 1
  )
  @Column(name = "RCID")
  private Integer id;

  @Column(name = "RCIDENTITY", nullable = false, length = 9)
  private String identity;

  @Column(name = "RCESTADO", nullable = false)
  private Short estado;

  @Column(name = "RCRESCONVENIENCIA", length = 50)
  private String resultadoConveniencia;

  @Column(name = "RCRESIDONEIDAD", length = 50)
  private String resultadoIdoneidad;

  @CreatedDate
  @Column(name = "RCFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @LastModifiedDate
  @Column(name = "RCFECMODIFICACION")
  private LocalDateTime fechaModificacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RCVERSIONID", nullable = false)
  private VersionEntity version;

  @OneToMany(mappedBy = "respuestaCliente", fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      orphanRemoval = true)
  private List<RespuestaClienteDetalleEntity> detalles;

  @Column(name = "RCFECCADUCIDAD")
  private LocalDate fechaCaducidad;

  @Column(name = "RCFECFIRMA")
  private LocalDateTime fechaFirma;

  @Column(name = "RCFECANULACION")
  private LocalDateTime fechaAnulacion;

  @CreatedBy
  @Size(max = 50)
  @Column(name = "RCUSUALTA", length = 50, updatable = false)
  private String usuarioAlta;

  @LastModifiedBy
  @Size(max = 50)
  @Column(name = "RCUSUMOD", length = 50)
  private String usuarioModificacion;

  @OneToMany(mappedBy = "respuesta", fetch = FetchType.LAZY)
  private List<RelRespuestaCombinacionEntity> relCombinaciones;
}
