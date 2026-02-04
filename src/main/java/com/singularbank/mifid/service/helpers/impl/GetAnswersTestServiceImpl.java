package com.singularbank.mifid.service.helpers.impl;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.service.helpers.GetAnswersTestService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetAnswersTestServiceImpl implements
    GetAnswersTestService {

  private final RespuestaClienteDetalleRepository respuestaClienteDetalleRepository;

  @Override
  @Transactional(readOnly = true)
  public AnswersTest get(
     Integer testId, TypeTest typeTest) {
    log.info("Getting test answers for reference: {}",
        testId);
    Objects.requireNonNull(testId, "testId is required");
    Objects.requireNonNull(typeTest, "testType is required");
    return respuestaClienteDetalleRepository
        .findAnswersByTestIdAndTestType(testId, typeTest);
  }
}