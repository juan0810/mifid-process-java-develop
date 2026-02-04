package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.database.jpa.JpaRespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.database.mapper.RespuestaClienteDetalleEntityMapper;
import com.singularbank.mifid.repository.database.model.ItemEntity;
import com.singularbank.mifid.repository.database.model.RespuestaClienteDetalleEntity;
import com.singularbank.mifid.repository.database.model.RespuestaClienteEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RespuestaClienteDetalleRepositoryImpl implements RespuestaClienteDetalleRepository {

  private final JpaRespuestaClienteDetalleRepository jpaRespuestaClienteDetalleRepository;
  private final RespuestaClienteDetalleEntityMapper mapper;

  @Override
  public void saveAll(Integer respuestaClienteId, List<RespuestaDetalle> detalles) {
    List<RespuestaClienteDetalleEntity> entities = detalles.stream()
        .map(detalle -> RespuestaClienteDetalleEntity.builder()
            .item(ItemEntity.builder().id(detalle.getRespuestaId()).build())
            .valor(detalle.getValor())
            .respuestaCliente(RespuestaClienteEntity.builder()
                .id(respuestaClienteId)
                .build())
            .build())
        .toList();

    List<RespuestaClienteDetalleEntity> savedEntities = jpaRespuestaClienteDetalleRepository.saveAll(
        entities);

    log.debug("Saved {} answer details for client response ID: {}",
        savedEntities.size(), respuestaClienteId);
  }

  @Override
  public AnswersTest findAnswersByIdentityAndTestType(String identity, TypeTest typeTest) {
    var detalles = jpaRespuestaClienteDetalleRepository.findAnswersByIdentityAndTestType(identity,
        typeTest.getCode());

    if (detalles.isEmpty()) {
      log.debug("No {} answers found for customer: {}", typeTest.getCode(), identity);
      throw new ResourceNotFoundException(
          "No " + typeTest.getCode() + " answers found for customer: " + identity);
    }

    log.debug("Found {} {} answers for customer: {}",
        detalles.size(), typeTest.getCode(), identity);
    return mapper.toDomain(detalles);
  }


  @Override
  public AnswersTest findAnswersByTestId(Integer id, TypeTest typeTest) {
    var detalles = jpaRespuestaClienteDetalleRepository.findAnswersByTestId(id, typeTest.getCode());

    if (detalles.isEmpty()) {
      log.debug("No answers found for id: {}", id);
      throw new ResourceNotFoundException(
              "No answers found for id: " + id);
    }

    log.debug("Found {} answers for id: {}",
            detalles.size(), id);
    return mapper.toDomain(detalles);
  }

  //TODO rename findAnswersByTestIdAndTestType to findActiveAnswersByTestIdAndTestType
  @Override
  public AnswersTest findAnswersByTestIdAndTestType(Integer testId, TypeTest typeTest) {
    var detalles = jpaRespuestaClienteDetalleRepository.findAnswersByTestIdAndTestType(testId,
        typeTest.getCode());

    if (detalles == null || detalles.isEmpty()) {
      log.debug("No {} answers found for test: {}", typeTest.getCode(), testId);
      throw new ResourceNotFoundException(
          "No " + typeTest.getCode() + " answers found for test: " + testId);
    }

    log.debug("Found {} {} answers for test: {}",
        detalles.size(), typeTest.getCode(), testId);
    return mapper.toDomain(detalles);
  }
}