package com.singularbank.mifid.controller.helpers;

import com.singularbank.mifid.controller.helpers.dto.ReadinessResponseDTO;
import com.singularbank.mifid.controller.helpers.dto.StatusResponseDTO;
import com.singularbank.mifid.service.helpers.CheckDatabaseHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

  private final CheckDatabaseHealthService checkDatabaseHealthService;

  @Tag(name = "Health", description = "Health check endpoints")
  @Operation(summary = "Check liveness",
      description = "Verifies that the service is alive")
  @ApiResponse(responseCode = "200", description = "Service operational")
  @GetMapping("/liveness")
  public ResponseEntity<StatusResponseDTO> getLiveness() {
    return ResponseEntity.ok(
        StatusResponseDTO.builder()
            .status("OK")
            .build()
    );
  }

  @Operation(summary = "Check readiness",
      description = "Verifies that the service is ready to receive traffic",
      responses = {
          @ApiResponse(responseCode = "200", description = "Service ready"),
          @ApiResponse(responseCode = "503", description = "Service unavailable")
      }
  )
  @GetMapping("/readiness")
  public ResponseEntity<ReadinessResponseDTO> getReadiness() {
    var healthResult = checkDatabaseHealthService.check();

    var response = ReadinessResponseDTO.builder()
        .status(healthResult.isHealthy() ? "ready" : "not_ready")
        .dependencies(Map.of("database", healthResult.message()))
        .build();

    return healthResult.isHealthy()
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }
}