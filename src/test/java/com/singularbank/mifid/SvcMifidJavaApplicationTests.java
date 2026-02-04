package com.singularbank.mifid;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class SvcMifidJavaApplicationTests extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    assertDoesNotThrow(() -> {
    }, "Spring context should load without throwing exceptions");
  }
}