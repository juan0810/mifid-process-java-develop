package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.repository.CombinacionMatchingRepository;
import com.singularbank.mifid.repository.database.jpa.JpaRelCombinacionItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CombinacionMatchingRepositoryImpl implements CombinacionMatchingRepository {

  private final JpaRelCombinacionItemRepository jpaRelCombinacionItemRepository;

  @Override
  public Optional<Integer> findBestMatchingCombination(List<Integer> respuestasUsuario) {
    if (respuestasUsuario == null || respuestasUsuario.isEmpty()) {
      log.warn("Empty or null respuestasUsuario provided");
      return Optional.empty();
    }

    try {
      Integer crid = jpaRelCombinacionItemRepository.findBestMatchingCombination(respuestasUsuario);
      log.debug("Found best matching CRID: {} for {} user answers", crid, respuestasUsuario.size());
      return Optional.ofNullable(crid);
    } catch (RuntimeException e) {
      log.debug("No matching combination found for user answers: {}", respuestasUsuario, e);
      return Optional.empty();
    }
  }

  @Override
  public List<Integer> findAllMatchingCombinations(List<Integer> respuestasUsuario) {
    if (respuestasUsuario == null || respuestasUsuario.isEmpty()) {
      log.warn("Empty or null respuestasUsuario provided");
      return List.of();
    }

    try {
      List<Integer> crids = jpaRelCombinacionItemRepository.findAllMatchingCombinations(respuestasUsuario);
      log.debug("Found {} matching CRIDs for {} user answers", crids.size(), respuestasUsuario.size());
      return crids;
    } catch (RuntimeException e) {
      log.error("Error finding matching combinations", e);
      return List.of();
    }
  }
}

