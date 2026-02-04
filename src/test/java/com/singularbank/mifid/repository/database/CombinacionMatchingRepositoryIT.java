package com.singularbank.mifid.repository.database;

import static org.assertj.core.api.Assertions.assertThat;

import com.singularbank.mifid.repository.CombinacionMatchingRepository;
import com.singularbank.mifid.repository.config.AbstractRepositoryTest;
import com.singularbank.mifid.repository.database.jpa.JpaRelCombinacionItemRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DisplayName("Combinacion Subset Matching Integration Tests")
class CombinacionMatchingRepositoryIT extends AbstractRepositoryTest {

  @Autowired
  private JpaRelCombinacionItemRepository jpaRelCombinacionItemRepository;

  @Autowired
  private TestEntityManager entityManager;

  private CombinacionMatchingRepository matchingRepository;

  @BeforeEach
  void setUp() {
    matchingRepository = new CombinacionMatchingRepositoryImpl(jpaRelCombinacionItemRepository);
  }

  @Test
  @DisplayName("Should find combination when user has required items - Familia A")
  void shouldFindCombinacionFamiliaA() {
    // Obtener CRID 20 (Familia A) y sus items requeridos de Liquibase
    Integer p3d = getItemIdByQuery("P3", "D");
    Integer p10aA = getItemIdByQuery("P10-A", "A");
    
    List<Integer> userAnswers = List.of(p3d, p10aA, 999);
    
    Optional<Integer> result = matchingRepository.findBestMatchingCombination(userAnswers);
    
    assertThat(result).hasValue(20);
  }

  @Test
  @DisplayName("Should find most specific combination - Familia E has 3 conditions")
  void shouldPrioritizeMostSpecificFamiliaE() {
    Integer p3d = getItemIdByQuery("P3", "D");
    Integer p4c = getItemIdByQuery("P4", "C");
    Integer p10eA = getItemIdByQuery("P10-E", "A");
    
    List<Integer> userAnswers = List.of(p3d, p4c, p10eA, 999);
    
    Optional<Integer> result = matchingRepository.findBestMatchingCombination(userAnswers);
    
    assertThat(result).hasValue(24);
  }

  @Test
  @DisplayName("Should return empty when no combination matches")
  void shouldReturnEmptyWhenNoMatch() {
    List<Integer> userAnswers = List.of(999999, 888888);
    
    Optional<Integer> result = matchingRepository.findBestMatchingCombination(userAnswers);
    
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should return empty when user missing required items")
  void shouldReturnEmptyWhenMissingRequired() {
    Integer p3d = getItemIdByQuery("P3", "D");
    
    List<Integer> userAnswers = List.of(p3d);
    
    Optional<Integer> result = matchingRepository.findBestMatchingCombination(userAnswers);
    
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should return empty for empty user answers")
  void shouldReturnEmptyForEmptyAnswers() {
    List<Integer> userAnswers = List.of();
    
    Optional<Integer> result = matchingRepository.findBestMatchingCombination(userAnswers);
    
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should find all matching combinations when user answers cover multiple families")
  void shouldFindAllMatchingCombinations() {
    Integer p3d = getItemIdByQuery("P3", "D");
    Integer p10aA = getItemIdByQuery("P10-A", "A");
    Integer p10bA = getItemIdByQuery("P10-B", "A");
    
    List<Integer> userAnswers = List.of(p3d, p10aA, p10bA, 999);
    
    List<Integer> results = matchingRepository.findAllMatchingCombinations(userAnswers);
    
    assertThat(results)
        .isNotEmpty()
        .contains(20, 21);
  }

  @Test
  @DisplayName("Should handle subset matching - user has more answers than required")
  void shouldHandleSubsetMatching() {
    Integer p3d = getItemIdByQuery("P3", "D");
    Integer p10aA = getItemIdByQuery("P10-A", "A");
    
    List<Integer> userAnswers = List.of(p3d, p10aA, 100, 200, 300, 999);
    
    Optional<Integer> result = matchingRepository.findBestMatchingCombination(userAnswers);
    
    assertThat(result).hasValue(20);
  }

  private Integer getItemIdByQuery(String preguntaRef, String respuestaValor) {
    String jpql;
    if (preguntaRef.startsWith("P10-")) {
      String familia = preguntaRef.substring(4);
      int orden = 10 + (familia.charAt(0) - 'A') + 1;
      jpql = """
          SELECT i.id FROM ItemEntity i 
          WHERE i.tipoItem = 'RE' 
          AND i.valor = :valor
          AND i.tipoTest = 'CO'
          AND i.itemPadre.orden = :orden
          """;
      return entityManager.getEntityManager()
          .createQuery(jpql, Integer.class)
          .setParameter("valor", respuestaValor)
          .setParameter("orden", orden)
          .setMaxResults(1)
          .getSingleResult();
    } else {
      int orden = Integer.parseInt(preguntaRef.substring(1));
      jpql = """
          SELECT i.id FROM ItemEntity i 
          WHERE i.tipoItem = 'RE' 
          AND i.valor = :valor
          AND i.tipoTest = 'CO'
          AND i.itemPadre.orden = :orden
          """;
      return entityManager.getEntityManager()
          .createQuery(jpql, Integer.class)
          .setParameter("valor", respuestaValor)
          .setParameter("orden", orden)
          .setMaxResults(1)
          .getSingleResult();
    }
  }
}

