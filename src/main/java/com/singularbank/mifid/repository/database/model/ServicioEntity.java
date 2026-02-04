package com.singularbank.mifid.repository.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MKTMSERVICIOS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ServicioEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "SEID")
  private Short id;

  @NotNull
  @Size(max = 100)
  @Column(name = "SENOMBRE", nullable = false, length = 100)
  private String nombre;

  @Size(max = 500)
  @Column(name = "SEDESCRIPCION", length = 500)
  private String descripcion;

  @Size(max = 10)
  @Column(name = "SETIPOSTEST", length = 10)
  private String tiposTest;

  @Size(max = 50)
  @Column(name = "SEFAMILIAS", length = 50)
  private String familias;

  @NotNull
  @Size(max = 50)
  @Column(name = "SEUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @CreationTimestamp
  @Column(name = "SEFECALTA", nullable = false, updatable = false)
  private LocalDateTime fechaAlta;

  @Size(max = 50)
  @Column(name = "SEUSUMOD", length = 50)
  private String usuarioModificacion;

  @Column(name = "SEFECMOD")
  private LocalDateTime fechaModificacion;

  @OneToMany(mappedBy = "servicio", fetch = FetchType.LAZY)
  private List<VersionEntity> versiones;
}