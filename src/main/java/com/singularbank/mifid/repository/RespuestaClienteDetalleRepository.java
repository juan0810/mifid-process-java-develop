package com.singularbank.mifid.repository;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.entity.TypeTest;
import java.util.List;

public interface RespuestaClienteDetalleRepository {

  void saveAll(Integer respuestaClienteId, List<RespuestaDetalle> detalles);

  AnswersTest findAnswersByTestIdAndTestType(Integer testId, TypeTest typeTest);

  AnswersTest findAnswersByIdentityAndTestType(String identity, TypeTest typeTest);

  AnswersTest findAnswersByTestId(Integer testId, TypeTest typeTest);
}










