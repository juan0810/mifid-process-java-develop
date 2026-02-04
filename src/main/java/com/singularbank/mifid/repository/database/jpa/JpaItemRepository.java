package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.ItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaItemRepository extends JpaRepository<ItemEntity, Integer>,
    JpaSpecificationExecutor<ItemEntity> {

  @Query("""
    SELECT MAX(vp.version.id)
    FROM VersionPreguntaEntity vp
    JOIN vp.item i
    WHERE i.tipoTest = :typeTest
      AND vp.version.servicio.nombre = :application
    """)
  Short findLastVersion(@Param("typeTest") String typeTest,
      @Param("application") String application);

  @EntityGraph(attributePaths = {"subItems", "subItems.subItems"})
  @Query("""
      SELECT DISTINCT i
      FROM ItemEntity i
      JOIN VersionPreguntaEntity vp ON vp.item.id = i.id
      WHERE vp.version.servicio.nombre = :application
        AND vp.version.id = :version
        AND i.tipoTest = :typeTest
        AND i.tipoItem IN ('PR', 'AM')
        AND i.itemPadre IS NULL
        AND i.fechaInicio <= CURRENT_DATE
        AND (i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE)
      ORDER BY i.orden
      """)
  List<ItemEntity> findQuestionsWithAnswersByTestType(
      @Param("typeTest") String typeTest,
      @Param("application") String application,
      @Param("version") Short version);

  @EntityGraph(attributePaths = {"subItems", "subItems.subItems"})
  @Query("""
      SELECT DISTINCT i
      FROM ItemEntity i
      JOIN VersionPreguntaEntity vp ON vp.item.id = i.id
      WHERE vp.version.servicio.nombre = :application
        AND vp.version.id = :version
        AND i.tipoTest = :typeTest
        AND i.tipoItem IN ('PR', 'AM')
        AND i.fechaInicio <= CURRENT_DATE
        AND (i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE)
      ORDER BY i.orden
      """)
  List<ItemEntity> findAllQuestionsByTestType(
      @Param("typeTest") String typeTest,
      @Param("application") String application,
      @Param("version") Short version);

  @Query("""
    SELECT i FROM ItemEntity i
    LEFT JOIN FETCH i.itemPadre p
    WHERE i.id IN :ids
    AND i.tipoItem = 'RE'
    AND p.tipoTest = :typeTest
    """)
  List<ItemEntity> findAnswersByIdsAndTestType(
      @Param("ids") List<Integer> ids,
      @Param("typeTest") String typeTest);

  @Query("""
    SELECT i FROM ItemEntity i
    LEFT JOIN FETCH i.itemPadre
    WHERE i.id IN :ids
    AND i.tipoItem = 'RE'
    """)
  List<ItemEntity> findAnswersByIds(@Param("ids") List<Integer> ids);

  @EntityGraph(attributePaths = {"subItems"})
  @Query("""
      SELECT DISTINCT i
      FROM ItemEntity i
      JOIN VersionPreguntaEntity vp ON vp.item.id = i.id
      WHERE vp.version.id = :versionId
        AND i.tipoTest = :typeTest
        AND i.tipoItem IN ('PR', 'AM')
        AND i.fechaInicio <= CURRENT_DATE
        AND (i.fechaFin IS NULL OR i.fechaFin >= CURRENT_DATE)
      ORDER BY i.orden
      """)
  List<ItemEntity> findQuestionsByVersionIdAndTestType(
      @Param("versionId") Short versionId,
      @Param("typeTest") String typeTest);
}