package com.singularbank.mifid.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class BasePostgresContainer {

  private static final String POSTGRES_IMAGE = "postgres:15.13";

  private static PostgreSQLContainer<?> container;

  public static PostgreSQLContainer<?> getInstance() {
    if (container == null) {
      container = new PostgreSQLContainer<>(POSTGRES_IMAGE)
          .withDatabaseName("test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(true);

      container.start();
    }
    return container;
  }
}