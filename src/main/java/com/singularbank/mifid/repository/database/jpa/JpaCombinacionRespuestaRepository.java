package com.singularbank.mifid.repository.database.jpa;

import com.singularbank.mifid.repository.database.model.CombinacionRespuestaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCombinacionRespuestaRepository extends JpaRepository<CombinacionRespuestaEntity, Integer> {

  @Query("SELECT c FROM CombinacionRespuestaEntity c WHERE c.resultado = :resultado")
  List<CombinacionRespuestaEntity> findByResultado(@Param("resultado") String resultado);

  @Query("SELECT DISTINCT c FROM CombinacionRespuestaEntity c " +
      "JOIN FETCH c.relItems ci " +
      "WHERE ci.item.id IN :itemIds")
  List<CombinacionRespuestaEntity> findByItemIds(@Param("itemIds") List<Integer> itemIds);

  @Query("SELECT DISTINCT c FROM CombinacionRespuestaEntity c " +
      "LEFT JOIN FETCH c.relItems " +
      "WHERE c.resultado IN :resultados")
  List<CombinacionRespuestaEntity> findByResultadoIn(@Param("resultados") List<String> resultados);

  @Query("SELECT DISTINCT c FROM CombinacionRespuestaEntity c " +
      "LEFT JOIN FETCH c.relItems " +
      "WHERE c.resultado IN ('AL', 'NC', 'CO')")
  List<CombinacionRespuestaEntity> findConvenienceCombinations();

  @Query("SELECT DISTINCT c FROM CombinacionRespuestaEntity c " +
      "LEFT JOIN FETCH c.relItems " +
      "WHERE c.resultado IN ('MC', 'CO', 'MO', 'DI', 'AR')")
  List<CombinacionRespuestaEntity> findSuitabilityProfiles();
}
