package com.singularbank.mifid.karate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.singularbank.mifid.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

class KarateRunnerIT extends AbstractIntegrationTest {

  private static final String FEATURES_PATH = "classpath:com/singularbank/mifid/karate/features";

  @LocalServerPort
  private int port;

  @BeforeAll
  static void beforeAll() {
    System.setProperty("karate.config.dir", "classpath:com/singularbank/mifid/karate/config");
  }

  @BeforeEach
  void setupWireMockStubs() {
    stubPdfGeneratorSuccess();
  }

  @Test
  void testAll() {
    System.setProperty("karate.port", String.valueOf(port));

    Results results = Runner.path(FEATURES_PATH)
        .outputCucumberJson(true)
        .parallel(1);

    assertEquals(0, results.getFailCount(), results.getErrorMessages());
  }
}