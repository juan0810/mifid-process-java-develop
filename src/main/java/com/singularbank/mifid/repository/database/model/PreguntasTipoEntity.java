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
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MKTMPREGUNTASTIPO")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PreguntasTipoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "PTID")
  private Short id;

  @NotNull
  @Size(max = 50)
  @Column(name = "PTTIPO", nullable = false, length = 50)
  private String tipo;

  @Size(max = 250)
  @Column(name = "PTDESCRIPCION", length = 250)
  private String descripcion;

  @NotNull
  @Size(max = 50)
  @Column(name = "PTUSUALTA", nullable = false, length = 50)
  private String usuarioAlta;

  @Size(max = 50)
  @Column(name = "PTUSUMOD", length = 50)
  private String usuarioModificacion;

  @OneToMany(mappedBy = "tipoPregunta", fetch = FetchType.LAZY)
  private List<ItemEntity> items;
}