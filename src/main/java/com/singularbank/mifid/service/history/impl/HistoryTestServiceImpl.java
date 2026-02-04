package com.singularbank.mifid.service.history.impl;

import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestPage;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.history.HistoryTestService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryTestServiceImpl implements HistoryTestService {

  private final RespuestaClienteRepository respuestaClienteRepository;

  @Override
  @Transactional(readOnly = true)
  public HistoryTestPage getTestHistory(String documentNumber,
      HistoryTestFilter historyTestFilter) {
    log.debug("Fetching test history for: {}", documentNumber);
    Objects.requireNonNull(documentNumber, "documentNumber is required");
    Objects.requireNonNull(historyTestFilter, "testHistoryFilter is required");
    return respuestaClienteRepository.findTestHistory(documentNumber, historyTestFilter);
  }
}
