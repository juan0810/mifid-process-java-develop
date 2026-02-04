package com.singularbank.mifid.service.status.impl;

import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.StatusTestResult;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.status.StateTransitionValidator;
import com.singularbank.mifid.service.status.StatusAction;
import com.singularbank.mifid.service.status.StatusTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusTestServiceImpl implements StatusTestService {

    private final RespuestaClienteRepository respuestaClienteRepository;
    private final StateTransitionValidator transitionValidator;

    @Override
    @Transactional
    public StatusTestResult updateStatus(Integer testId, StateTest newStatus) {
        Objects.requireNonNull(testId, "testId is required");
        Objects.requireNonNull(newStatus, "newStatus is required");

        log.info("Updating test {} to status {}", testId, newStatus);

        var response = respuestaClienteRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

        var currentState = response.getEstado();

        if (currentState == newStatus) {
            log.info("Test {} already in status {}, no changes needed", testId, newStatus);
            return buildResult(testId, response);
        }

        transitionValidator.validate(currentState, newStatus);

        var now = LocalDateTime.now();
        StatusAction.forState(newStatus).apply(response, now);

        respuestaClienteRepository.updateStatus(
                testId,
                newStatus,
                response.getFechaFirma(),
                response.getFechaAnulacion(),
                now,
                response.getFechaCaducidad()
        );

        log.info("Test {} status updated: {} -> {}", testId, currentState, newStatus);

        return buildResult(testId, newStatus, response);
    }

    private StatusTestResult buildResult(Integer testId,
                                         RespuestaCliente response) {
        return StatusTestResult.builder()
                .testId(testId)
                .status(response.getEstado())
                .signatureDate(response.getFechaFirma())
                .cancellationDate(response.getFechaAnulacion())
                .build();
    }

    private StatusTestResult buildResult(Integer testId, StateTest newStatus,
                                         RespuestaCliente response) {
        return StatusTestResult.builder()
                .testId(testId)
                .status(newStatus)
                .signatureDate(response.getFechaFirma())
                .cancellationDate(response.getFechaAnulacion())
                .caducityDate(response.getFechaCaducidad())
                .build();
    }

}