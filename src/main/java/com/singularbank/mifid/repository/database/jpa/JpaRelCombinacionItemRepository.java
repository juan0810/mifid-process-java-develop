package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.RelCombinacionItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRelCombinacionItemRepository extends JpaRepository<RelCombinacionItemEntity, Integer> {

  @Query("SELECT r FROM RelCombinacionItemEntity r WHERE r.combinacion.id = :combinacionId")
  List<RelCombinacionItemEntity> findByCombinacionId(@Param("combinacionId") Integer combinacionId);

  @Query("SELECT r FROM RelCombinacionItemEntity r WHERE r.item.id = :itemId")
  List<RelCombinacionItemEntity> findByItemId(@Param("itemId") Integer itemId);

  @Query("SELECT r FROM RelCombinacionItemEntity r " +
      "WHERE r.combinacion.id = :combinacionId AND r.item.id IN :itemIds")
  List<RelCombinacionItemEntity> findByCombinacionIdAndItemIds(
      @Param("combinacionId") Integer combinacionId,
      @Param("itemIds") List<Integer> itemIds);

  @Query(value = """
      WITH candidate_crids AS (
        SELECT 
          ci.ciidcombinacion AS crid,
          COUNT(*) AS total_items_required,
          COUNT(CASE WHEN ci.ciiditem IN :respuestasUsuario THEN 1 END) AS matched_items
        FROM mktmrelcombinacionitem ci
        GROUP BY ci.ciidcombinacion
      )
      SELECT crid
      FROM candidate_crids
      WHERE matched_items = total_items_required
        AND total_items_required > 0
      ORDER BY total_items_required DESC
      LIMIT 1
      """, nativeQuery = true)
  Integer findBestMatchingCombination(@Param("respuestasUsuario") List<Integer> respuestasUsuario);

  @Query(value = """
      WITH candidate_crids AS (
        SELECT 
          ci.ciidcombinacion AS crid,
          COUNT(*) AS total_items_required,
          COUNT(CASE WHEN ci.ciiditem IN :respuestasUsuario THEN 1 END) AS matched_items
        FROM mktmrelcombinacionitem ci
        GROUP BY ci.ciidcombinacion
      )
      SELECT crid
      FROM candidate_crids
      WHERE matched_items = total_items_required
        AND total_items_required > 0
      ORDER BY total_items_required DESC
      """, nativeQuery = true)
  List<Integer> findAllMatchingCombinations(@Param("respuestasUsuario") List<Integer> respuestasUsuario);
}
