package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MKTMVERSIONES")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class VersionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "VEID")
  private Short id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "VESERVICIOID", nullable = false)
  private ServicioEntity servicio;

  @NotNull
  @Size(max = 500)
  @Column(name = "VEDESCRIPCION", nullable = false, length = 500)
  private String descripcion;

  @NotNull
  @Column(name = "VEFECINICIO", nullable = false)
  private LocalDate fechaInicio;

  @Column(name = "VEFECFIN")
  private LocalDate fechaFin;

  @NotNull
  @Column(name = "VEACTIVA", nullable = false)
  private Boolean activa = true;

  @NotNull
  @Size(max = 50)
  @Column(name = "VEUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @CreationTimestamp
  @Column(name = "VEFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @Size(max = 50)
  @Column(name = "VEUSUMOD", length = 50)
  private String usuarioModificacion;

  @Column(name = "VEFECMOD")
  private LocalDateTime fechaModificacion;

  @OneToMany(mappedBy = "version", fetch = FetchType.LAZY)
  private List<VersionPreguntaEntity> versionPreguntas;
}