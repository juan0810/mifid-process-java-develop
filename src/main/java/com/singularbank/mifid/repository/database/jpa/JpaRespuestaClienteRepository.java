package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.RespuestaClienteEntity;
import com.singularbank.mifid.repository.database.projection.TestHistoryProjection;
import com.singularbank.mifid.repository.database.projection.TestWithServiceInfoProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRespuestaClienteRepository extends
    JpaRepository<RespuestaClienteEntity, Integer> {

  @EntityGraph(attributePaths = {"detalles", "detalles.item"})
  @Query("""
      SELECT rc FROM RespuestaClienteEntity rc
      WHERE rc.identity = :identity
      AND rc.version.id = :versionId
      AND rc.estado = 2
      AND rc.resultadoConveniencia IS NOT NULL
      AND rc.resultadoConveniencia != ''
      ORDER BY rc.fechaAlta DESC
      LIMIT 1
      """)
  Optional<RespuestaClienteEntity> findConvenienceTestByIdentityAndVersion(
      @Param("identity") String identity,
      @Param("versionId") Short versionId);


  @Query("""
      SELECT rc
      FROM RespuestaClienteEntity rc
      JOIN FETCH rc.detalles rcd
      JOIN FETCH rcd.item i
      JOIN FETCH i.itemPadre p
      WHERE rc.id = :id
      AND rc.id = (
          SELECT MAX(rc2.id)
          FROM RespuestaClienteEntity rc2
          WHERE rc2.id = :id
      )
      ORDER BY p.tipoTest, p.orden
      """)
  Optional<RespuestaClienteEntity> findAnswersById(
          @Param("id") Integer id);

  @Query("""
      SELECT rc
      FROM RespuestaClienteEntity rc
      JOIN FETCH rc.detalles rcd
      JOIN FETCH rcd.item i
      JOIN FETCH i.itemPadre p
      WHERE rc.identity = :identity
      AND rc.estado = 2
      AND rc.id = (
          SELECT MAX(rc2.id)
          FROM RespuestaClienteEntity rc2
          WHERE rc2.identity = :identity
          AND rc2.estado = 2
      )
      ORDER BY p.tipoTest, p.orden
      """)
  Optional<RespuestaClienteEntity> findAnswersByIdentity(
      @Param("identity") String identity);

  @Query(value = """
          WITH base_tests AS (
              SELECT 
                  rc.rcid as id,
                  rc.rcidentity as identity,
                  rc.rcresconveniencia as resultadoConveniencia,
                  rc.rcresidoneidad as resultadoIdoneidad,
                  rc.rcfecalta as fechaAlta,
                  rc.rcfecmodificacion as fechaModificacion,
                  rc.rcfeccaducidad as fechaCaducidad,
                  rc.rcfecfirma as fechaFirma,
                  v.veid as versionId,
                  v.vedescripcion as versionDescripcion,
                  v.vefecfin as versionFechaFin,
                  v.veactiva as versionActiva,
                  s.senombre as servicioNombre,
                  s.sedescripcion as servicioDescripcion
              FROM mktmrespuestacliente rc
              JOIN mktmversiones v ON rc.rcversionid = v.veid
              JOIN mktmservicios s ON v.veservicioid = s.seid
              WHERE rc.rcidentity = :identity
              AND rc.rcestado = 2
              AND v.veactiva = true
          ),
          unpivoted_tests AS (
              SELECT id, identity, resultadoConveniencia, resultadoIdoneidad, fechaAlta, 
                     fechaModificacion, fechaCaducidad, fechaFirma, versionId, versionDescripcion, versionFechaFin, 
                     versionActiva, servicioNombre, servicioDescripcion, 'CO' as tipoTest
              FROM base_tests WHERE resultadoConveniencia IS NOT NULL
              UNION ALL
              SELECT id, identity, resultadoConveniencia, resultadoIdoneidad, fechaAlta, 
                     fechaModificacion, fechaCaducidad, fechaFirma, versionId, versionDescripcion, versionFechaFin, 
                     versionActiva, servicioNombre, servicioDescripcion, 'ID' as tipoTest
              FROM base_tests WHERE resultadoIdoneidad IS NOT NULL
              UNION ALL
              SELECT bt.id, bt.identity, bt.resultadoConveniencia, bt.resultadoIdoneidad, bt.fechaAlta, 
                     bt.fechaModificacion, bt.fechaCaducidad, bt.fechaFirma, bt.versionId, bt.versionDescripcion, bt.versionFechaFin, 
                     bt.versionActiva, bt.servicioNombre, bt.servicioDescripcion, 'SO' as tipoTest
              FROM base_tests bt
              WHERE EXISTS (
                  SELECT 1 FROM mktmrespuestaclientedetalle rcd
                  JOIN mktmitems i ON rcd.rdrespuestaid = i.itid
                  WHERE rcd.rdrespuestaclienteid = bt.id
                  AND i.ittipotest = 'SO'
              )
          ),
          ranked_tests AS (
              SELECT *, ROW_NUMBER() OVER (PARTITION BY tipoTest ORDER BY fechaAlta DESC) as rn
              FROM unpivoted_tests
          )
          SELECT 
              id, identity, resultadoConveniencia, resultadoIdoneidad, fechaAlta,
              fechaModificacion, fechaCaducidad, fechaFirma, versionId, versionDescripcion, versionFechaFin,
              versionActiva, servicioNombre, servicioDescripcion, tipoTest
          FROM ranked_tests
          WHERE rn = 1
          ORDER BY fechaAlta DESC
          """, nativeQuery = true)
  List<TestWithServiceInfoProjection> findLatestActiveTestsByIdentity(
      @Param("identity") String identity);


  @Query(value = """
          WITH base_tests AS (
              SELECT 
                  rc.rcid as id,
                  rc.rcidentity as identity,
                  rc.rcresconveniencia as resultadoConveniencia,
                  rc.rcresidoneidad as resultadoIdoneidad,
                  rc.rcfecalta as fechaAlta,
                  rc.rcfecmodificacion as fechaModificacion,
                  rc.rcfeccaducidad as fechaCaducidad,
                  rc.rcfecfirma as fechaFirma,
                  v.veid as versionId,
                  v.vedescripcion as versionDescripcion,
                  v.vefecfin as versionFechaFin,
                  v.veactiva as versionActiva,
                  s.senombre as servicioNombre,
                  s.sedescripcion as servicioDescripcion
              FROM mktmrespuestacliente rc
              JOIN mktmversiones v ON rc.rcversionid = v.veid
              JOIN mktmservicios s ON v.veservicioid = s.seid
              WHERE rc.rcid = :id
              AND v.veactiva = true
          ),
          unpivoted_tests AS (
              SELECT id, identity, resultadoConveniencia, resultadoIdoneidad, fechaAlta, 
                     fechaModificacion, fechaCaducidad, fechaFirma, versionId, versionDescripcion, versionFechaFin, 
                     versionActiva, servicioNombre, servicioDescripcion, 'CO' as tipoTest
              FROM base_tests WHERE resultadoConveniencia IS NOT NULL
              UNION ALL
              SELECT id, identity, resultadoConveniencia, resultadoIdoneidad, fechaAlta, 
                     fechaModificacion, fechaCaducidad, fechaFirma, versionId, versionDescripcion, versionFechaFin, 
                     versionActiva, servicioNombre, servicioDescripcion, 'ID' as tipoTest
              FROM base_tests WHERE resultadoIdoneidad IS NOT NULL
              UNION ALL
              SELECT bt.id, bt.identity, bt.resultadoConveniencia, bt.resultadoIdoneidad, bt.fechaAlta, 
                     bt.fechaModificacion, bt.fechaCaducidad, bt.fechaFirma, bt.versionId, bt.versionDescripcion, bt.versionFechaFin, 
                     bt.versionActiva, bt.servicioNombre, bt.servicioDescripcion, 'SO' as tipoTest
              FROM base_tests bt
              WHERE EXISTS (
                  SELECT 1 FROM mktmrespuestaclientedetalle rcd
                  JOIN mktmitems i ON rcd.rdrespuestaid = i.itid
                  WHERE rcd.rdrespuestaclienteid = bt.id
                  AND i.ittipotest = 'SO'
              )
          ),
          ranked_tests AS (
              SELECT *, ROW_NUMBER() OVER (PARTITION BY tipoTest ORDER BY fechaAlta DESC) as rn
              FROM unpivoted_tests
          )
          SELECT 
              id, identity, resultadoConveniencia, resultadoIdoneidad, fechaAlta,
              fechaModificacion, fechaCaducidad, fechaFirma, versionId, versionDescripcion, versionFechaFin,
              versionActiva, servicioNombre, servicioDescripcion, tipoTest
          FROM ranked_tests
          WHERE rn = 1
          ORDER BY fechaAlta DESC
          """, nativeQuery = true)
  List<TestWithServiceInfoProjection> findLatestActiveTestsById(
          @Param("id") Integer id);

  @Query(value = """
      SELECT DISTINCT
          rc.rcid as id,
          rc.rcestado as estado,
          i.ittipotest as tipoTest,
          rc.rcresidoneidad as resultadoIdoneidad,
          rc.rcfecalta as fechaAlta,
          rc.rcfecfirma as fechaFirma,
          rc.rcfeccaducidad as fechaCaducidad
      FROM mktmrespuestacliente rc
      JOIN mktmrespuestaclientedetalle rcd ON rc.rcid = rcd.rdrespuestaclienteid
      JOIN mktmitems i ON rcd.rdrespuestaid = i.itid
      WHERE rc.rcidentity = :documentNumber
        AND i.ittipotest IS NOT NULL
        AND i.ittipotest <> 'NA'
        AND (COALESCE(:typeTest, '') = '' OR i.ittipotest = :typeTest)
        AND (CAST(:state AS INTEGER) IS NULL OR rc.rcestado = :state)
        AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR rc.rcfecalta >= :fromDate)
        AND (CAST(:toDate AS TIMESTAMP) IS NULL OR rc.rcfecalta <= :toDate)
      ORDER BY rc.rcfecalta DESC
      """,
      countQuery = """
          SELECT COUNT(*)
          FROM (
              SELECT DISTINCT rc.rcid, i.ittipotest
              FROM mktmrespuestacliente rc
              JOIN mktmrespuestaclientedetalle rcd ON rc.rcid = rcd.rdrespuestaclienteid
              JOIN mktmitems i ON rcd.rdrespuestaid = i.itid
              WHERE rc.rcidentity = :documentNumber
                AND i.ittipotest IS NOT NULL
                AND i.ittipotest <> 'NA'
                AND (COALESCE(:typeTest, '') = '' OR i.ittipotest = :typeTest)
                AND (CAST(:state AS INTEGER) IS NULL OR rc.rcestado = :state)
                AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR rc.rcfecalta >= :fromDate)
                AND (CAST(:toDate AS TIMESTAMP) IS NULL OR rc.rcfecalta <= :toDate)
          ) subq
          """,
      nativeQuery = true)
  Page<TestHistoryProjection> findTestHistory(
      @Param("documentNumber") String documentNumber,
      @Param("typeTest") String typeTest,
      @Param("state") Integer state,
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate,
      Pageable pageable
  );

  @Modifying
  @Query("""
      UPDATE RespuestaClienteEntity rc
      SET rc.estado = :status,
          rc.fechaFirma = :signatureDate,
          rc.fechaAnulacion = :cancellationDate,
          rc.fechaModificacion = :modificationDate,
          rc.fechaCaducidad = :caducityDate
      WHERE rc.id = :id
      """)
  void updateStatus(
      @Param("id") Integer id,
      @Param("status") Integer status,
      @Param("signatureDate") LocalDateTime signatureDate,
      @Param("cancellationDate") LocalDateTime cancellationDate,
      @Param("modificationDate") LocalDateTime modificationDate,
      @Param("caducityDate") LocalDate caducityDate

  );
}