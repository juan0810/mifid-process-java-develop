package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.CompleteTestInfo;
import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestPage;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.repository.database.jpa.JpaRespuestaClienteRepository;
import com.singularbank.mifid.repository.database.mapper.RespuestaClienteEntityMapper;
import com.singularbank.mifid.repository.database.mapper.TestHistoryProjectionMapper;
import com.singularbank.mifid.repository.database.mapper.TestProjectionMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RespuestaClienteRepositoryImpl implements RespuestaClienteRepository {

  private final JpaRespuestaClienteRepository jpaRespuestaClienteRepository;
  private final RespuestaClienteEntityMapper respuestaClienteEntityMapper;
  private final TestProjectionMapper testProjectionMapper;
  private final TestHistoryProjectionMapper testHistoryProjectionMapper;

  @Override
  public Optional<RespuestaCliente> findById(Integer id) {
    return jpaRespuestaClienteRepository.findById(id)
        .map(respuestaClienteEntityMapper::toDomain);
  }

  @Override
  public Optional<RespuestaCliente> findAnswersById(Integer id) {
    return jpaRespuestaClienteRepository.findAnswersById(id)
        .map(respuestaClienteEntityMapper::toDomain);
  }

  @Override
  public Optional<RespuestaCliente> findAnswersByIdentity(String identification) {
    return jpaRespuestaClienteRepository
        .findAnswersByIdentity(identification)
        .map(respuestaClienteEntityMapper::toDomain);
  }

  @Override
  public Optional<RespuestaCliente> findConvenienceTestByIdentityAndVersion(
      String identification,
      Short versionId) {
    return jpaRespuestaClienteRepository
        .findConvenienceTestByIdentityAndVersion(identification, versionId)
        .map(respuestaClienteEntityMapper::toDomain);
  }

  @Override
  public RespuestaCliente save(RespuestaCliente respuestaCliente) {
    return respuestaClienteEntityMapper.toDomain(jpaRespuestaClienteRepository.save(
        respuestaClienteEntityMapper.toEntity(respuestaCliente)));
  }

  @Override
  public void updateStatus(Integer id, StateTest status, LocalDateTime signatureDate,
      LocalDateTime cancellationDate, LocalDateTime modificationDate, LocalDate caducityDate) {
    jpaRespuestaClienteRepository.updateStatus(
        id,
        status.getCode(),
        signatureDate,
        cancellationDate,
        modificationDate,
        caducityDate
    );
  }

  @Override
  public List<CompleteTestInfo> findCurrentActiveTestsByIdentity(String identification) {
    return jpaRespuestaClienteRepository.findLatestActiveTestsByIdentity(identification)
        .stream()
        .map(testProjectionMapper::toDomain)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public List<CompleteTestInfo> findCurrentTestsById(Integer id) {
    return jpaRespuestaClienteRepository.findLatestActiveTestsById(id)
            .stream()
            .map(testProjectionMapper::toDomain)
            .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public HistoryTestPage findTestHistory(String documentNumber, HistoryTestFilter filter) {
    var pageable = PageRequest.of(
        filter.getPage(),
        filter.getSize(),
        Sort.by(Sort.Direction.DESC, "rcfecalta")
    );

    String testType = filter.getType() != null ? filter.getType().getCode() : null;
    Integer state = filter.getState() != null ? filter.getState().getCode() : null;

    log.info("Filter values - testType: {}, state: {}, from: {}, to: {}",
        testType, state, filter.getFrom(), filter.getTo());

    var page = jpaRespuestaClienteRepository.findTestHistory(
        documentNumber,
        testType,
        state,
        filter.getFrom() != null ? filter.getFrom().atStartOfDay() : null,
        filter.getTo() != null ? filter.getTo().atTime(23, 59, 59) : null,
        pageable
    );

    log.info("Query returned {} elements", page.getTotalElements());

    return HistoryTestPage.builder()
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .currentPage(page.getNumber())
        .pageSize(filter.getSize())
        .tests(page.getContent().stream()
            .map(testHistoryProjectionMapper::toDomain)
            .toList())
        .build();
  }
}