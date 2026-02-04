package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.RespuestaClienteDetalleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRespuestaClienteDetalleRepository extends
    JpaRepository<RespuestaClienteDetalleEntity, Integer> {

  @Query("""
      SELECT rcd
      FROM RespuestaClienteDetalleEntity rcd
      JOIN FETCH rcd.respuestaCliente rc
      JOIN FETCH rcd.item i
      JOIN FETCH i.itemPadre p
      WHERE rc.identity = :identity
      AND p.tipoTest = :typeTest
      AND rc.estado = 2
      AND rc.id = (
          SELECT MAX(rc2.id)
          FROM RespuestaClienteEntity rc2
          JOIN rc2.detalles dt
          JOIN dt.item it
          JOIN it.itemPadre preg
          WHERE rc2.identity = :identity
          AND preg.tipoTest = :typeTest
          AND rc2.estado = 2
      )
      ORDER BY p.orden
      """)
  List<RespuestaClienteDetalleEntity> findAnswersByIdentityAndTestType(
      @Param("identity") String identity, @Param("typeTest") String typeTest);

  @Query("""
      SELECT rcd
      FROM RespuestaClienteDetalleEntity rcd
      JOIN FETCH rcd.respuestaCliente rc
      JOIN FETCH rcd.item i
      JOIN FETCH i.itemPadre p
      WHERE rc.id = :testId
      AND p.tipoTest = :typeTest
      AND rc.estado = 2
      ORDER BY p.orden
      """)
  List<RespuestaClienteDetalleEntity> findAnswersByTestIdAndTestType(
      @Param("testId") Integer testId, @Param("typeTest") String typeTest);

  @Query("""
      SELECT rcd
      FROM RespuestaClienteDetalleEntity rcd
      JOIN FETCH rcd.respuestaCliente rc
      JOIN FETCH rcd.item i
      JOIN FETCH i.itemPadre p
      WHERE rc.id = :testId
      AND p.tipoTest = :typeTest
      ORDER BY p.orden
      """)
  List<RespuestaClienteDetalleEntity> findAnswersByTestId(
          @Param("testId") Integer testId, @Param("typeTest") String typeTest);
}