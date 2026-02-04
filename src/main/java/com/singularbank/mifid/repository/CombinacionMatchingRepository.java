package com.singularbank.mifid.repository;

import java.util.List;
import java.util.Optional;

public interface CombinacionMatchingRepository {

  Optional<Integer> findBestMatchingCombination(List<Integer> respuestasUsuario);

  List<Integer> findAllMatchingCombinations(List<Integer> respuestasUsuario);
}