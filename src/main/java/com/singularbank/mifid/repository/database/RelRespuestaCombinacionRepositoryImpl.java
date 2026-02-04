package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.repository.RelRespuestaCombinacionRepository;
import com.singularbank.mifid.repository.database.jpa.JpaRelRespuestaCombinacionRepository;
import com.singularbank.mifid.repository.database.model.CombinacionRespuestaEntity;
import com.singularbank.mifid.repository.database.model.RelRespuestaCombinacionEntity;
import com.singularbank.mifid.repository.database.model.RespuestaClienteEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RelRespuestaCombinacionRepositoryImpl implements RelRespuestaCombinacionRepository {

  private final JpaRelRespuestaCombinacionRepository jpaRepository;

  @Override
  @Transactional
  public void save(Integer respuestaClienteId, Integer combinacionId) {
    log.debug("Saving combination relation: responseId={}, combinationId={}", 
        respuestaClienteId, combinacionId);

    RelRespuestaCombinacionEntity entity = RelRespuestaCombinacionEntity.builder()
        .respuesta(createRespuestaRef(respuestaClienteId))
        .combinacion(createCombinacionRef(combinacionId))
        .build();

    jpaRepository.save(entity);
    
    log.debug("✓ Saved combination relation: responseId={}, combinationId={}", 
        respuestaClienteId, combinacionId);
  }

  @Override
  @Transactional
  public void saveAll(Integer respuestaClienteId, List<Integer> combinacionIds) {
    if (combinacionIds == null || combinacionIds.isEmpty()) {
      log.debug("No combinations to save for response {}", respuestaClienteId);
      return;
    }

    log.debug("Saving {} combination relations for response {}", 
        combinacionIds.size(), respuestaClienteId);

    List<RelRespuestaCombinacionEntity> entities = combinacionIds.stream()
        .map(combinacionId -> RelRespuestaCombinacionEntity.builder()
            .respuesta(createRespuestaRef(respuestaClienteId))
            .combinacion(createCombinacionRef(combinacionId))
            .build())
        .toList();

    jpaRepository.saveAll(entities);
    
    log.debug("✓ Saved {} combination relations for response {}", 
        entities.size(), respuestaClienteId);
  }

  private RespuestaClienteEntity createRespuestaRef(Integer id) {
    return RespuestaClienteEntity.builder()
        .id(id)
        .build();
  }

  private CombinacionRespuestaEntity createCombinacionRef(Integer id) {
    return CombinacionRespuestaEntity.builder()
        .id(id)
        .build();
  }
}
