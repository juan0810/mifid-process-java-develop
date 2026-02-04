package com.singularbank.mifid.repository;

import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.CompleteTestInfo;
import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestPage;
import com.singularbank.mifid.entity.StateTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RespuestaClienteRepository {

  Optional<RespuestaCliente> findById(Integer id);
  Optional<RespuestaCliente> findAnswersById(Integer id);

  Optional<RespuestaCliente> findAnswersByIdentity(String identification);

  Optional<RespuestaCliente> findConvenienceTestByIdentityAndVersion(
      String identification,
      Short versionId);

  RespuestaCliente save(RespuestaCliente respuestaCliente);

  void updateStatus(Integer id, StateTest status, LocalDateTime signatureDate,
                    LocalDateTime cancellationDate, LocalDateTime modificationDate, LocalDate caducityDate);

  List<CompleteTestInfo> findCurrentActiveTestsByIdentity(String identification);

  List<CompleteTestInfo> findCurrentTestsById(Integer id);

  HistoryTestPage findTestHistory(String documentNumber, HistoryTestFilter filter);
}