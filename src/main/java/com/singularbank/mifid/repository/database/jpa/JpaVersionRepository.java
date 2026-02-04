package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.VersionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVersionRepository extends JpaRepository<VersionEntity, Short> {

  @Query("SELECT v FROM VersionEntity v " +
      "WHERE v.servicio.nombre = :servicio " +
      "AND v.activa = true " +
      "ORDER BY v.fechaAlta DESC")
  List<VersionEntity> findVersionesActivasByServicioId(@Param("servicioId") String servicio);

  @Query("SELECT v FROM VersionEntity v " +
      "WHERE v.servicio.id = :servicioId " +
      "AND v.activa = true " +
      "AND v.fechaInicio <= :fecha " +
      "AND (v.fechaFin IS NULL OR v.fechaFin >= :fecha) " +
      "ORDER BY v.fechaInicio DESC")
  Optional<VersionEntity> findVersionActivaByServicioIdAndFecha(
      @Param("servicioId") Long servicioId,
      @Param("fecha") LocalDate fecha);

  @Query("SELECT v FROM VersionEntity v " +
      "JOIN FETCH v.servicio " +
      "WHERE v.id = :id " +
      "AND v.activa = true")
  Optional<VersionEntity> findByIdAndActivaTrue(@Param("id") Integer id);

  @Query("SELECT v.id FROM VersionEntity v " +
      "INNER JOIN v.servicio s " +
      "WHERE s.nombre = :nombreServicio " +
      "AND v.activa = true " +
      "AND v.fechaInicio <= CURRENT_DATE " +
      "AND (v.fechaFin IS NULL OR v.fechaFin >= CURRENT_DATE) " +
      "ORDER BY v.fechaInicio DESC")
  Optional<Integer> findActiveVersionIdByServicioNombre(
      @Param("nombreServicio") String nombreServicio);

  @Query("SELECT v FROM VersionEntity v " +
      "WHERE v.servicio.nombre = :servicio " +
      "AND v.activa = true " +
      "AND v.id = :id ")
  VersionEntity findVersionesActivasById(@Param("id") Short id, @Param("servicio") String servicio);
}
