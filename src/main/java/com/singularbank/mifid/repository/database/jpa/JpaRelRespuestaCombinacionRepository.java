package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.RelRespuestaCombinacionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRelRespuestaCombinacionRepository extends JpaRepository<RelRespuestaCombinacionEntity, Integer> {

  @Query("SELECT r FROM RelRespuestaCombinacionEntity r WHERE r.respuesta.id = :respuestaId")
  List<RelRespuestaCombinacionEntity> findByRespuestaId(@Param("respuestaId") Integer respuestaId);

  @Query("SELECT r FROM RelRespuestaCombinacionEntity r WHERE r.combinacion.id = :combinacionId")
  List<RelRespuestaCombinacionEntity> findByCombinacionId(@Param("combinacionId") Integer combinacionId);

  @Query("SELECT r FROM RelRespuestaCombinacionEntity r " +
      "WHERE r.respuesta.id = :respuestaId AND r.combinacion.id = :combinacionId")
  Optional<RelRespuestaCombinacionEntity> findByRespuestaIdAndCombinacionId(
      @Param("respuestaId") Integer respuestaId,
      @Param("combinacionId") Integer combinacionId);
}
