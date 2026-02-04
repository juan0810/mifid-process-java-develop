package com.singularbank.mifid.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 9999)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = BasePostgresContainer.getInstance();

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    registry.add("spring.liquibase.enabled", () -> "true");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }

  @BeforeEach
  void resetWireMock() {
    WireMock.reset();
  }

  protected void stubPdfGeneratorSuccess() {
    stubPdfGeneratorSuccess("JVBERi0xLjQKJeLjz9MK");
  }

  protected void stubPdfGeneratorSuccess(String pdfBase64Content) {
    stubFor(post(urlEqualTo("/generate-pdf"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "success": true,
                  "message": "PDF generated successfully",
                  "pdfBase64": "%s"
                }
                """.formatted(pdfBase64Content))));
  }

  protected void stubPdfGeneratorFailure(String errorMessage) {
    stubFor(post(urlEqualTo("/generate-pdf"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "success": false,
                  "message": "%s",
                  "pdfBase64": null
                }
                """.formatted(errorMessage))));
  }

  protected void stubPdfGeneratorError(int status) {
    stubFor(post(urlEqualTo("/generate-pdf"))
        .willReturn(aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\": \"Service unavailable\"}")));
  }

  @TestConfiguration
  static class OpenTelemetryTestConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
      OpenTelemetry mockOpenTelemetry = mock(OpenTelemetry.class);
      MeterProvider mockMeterProvider = mock(MeterProvider.class);
      MeterBuilder mockMeterBuilder = mock(MeterBuilder.class);
      Meter mockMeter = mock(Meter.class);

      when(mockOpenTelemetry.getMeterProvider()).thenReturn(mockMeterProvider);
      when(mockMeterProvider.meterBuilder(anyString())).thenReturn(mockMeterBuilder);
      when(mockMeterBuilder.setInstrumentationVersion(anyString())).thenReturn(mockMeterBuilder);
      when(mockMeterBuilder.build()).thenReturn(mockMeter);

      return mockOpenTelemetry;
    }
  }
}