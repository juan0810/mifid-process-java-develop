package com.singularbank.mifid.repository;

import java.util.List;

public interface RelRespuestaCombinacionRepository {

  void save(Integer respuestaClienteId, Integer combinacionId);

  void saveAll(Integer respuestaClienteId, List<Integer> combinacionIds);
}
