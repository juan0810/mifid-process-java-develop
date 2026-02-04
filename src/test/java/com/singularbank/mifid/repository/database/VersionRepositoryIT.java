package com.singularbank.mifid.repository.database;

import static org.assertj.core.api.Assertions.assertThat;

import com.singularbank.mifid.entity.Version;
import com.singularbank.mifid.repository.VersionRepository;
import com.singularbank.mifid.repository.config.AbstractRepositoryTest;
import com.singularbank.mifid.repository.database.jpa.JpaVersionRepository;
import com.singularbank.mifid.repository.database.mapper.VersionEntityMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VersionRepositoryIT extends AbstractRepositoryTest {

  @Autowired
  private JpaVersionRepository jpaVersionRepository;

  @Autowired
  private VersionEntityMapper versionEntityMapper;

  private VersionRepository versionRepository;

  private static final String SERVICE_ONBOARDING = "ONBOARDING";
  private static final String SERVICE_NON_EXISTENT = "NON_EXISTENT";
  private static final Short VERSION_ID_1 = 1;

  @BeforeEach
  void setUp() {
    versionRepository = new VersionRepositoryImpl(
        jpaVersionRepository,
        versionEntityMapper
    );
  }

  @Test
  void shouldFindVersionesActivasById() {
    // When
    Version version = versionRepository.findVersionesActivasById(
        VERSION_ID_1,
        SERVICE_ONBOARDING
    );

    // Then
    assertThat(version)
        .isNotNull()
        .extracting(Version::getId)
        .isEqualTo(VERSION_ID_1);
  }

  @Test
  void shouldReturnNullWhenVersionNotActiveOrNotExists() {
    // When
    Version version = versionRepository.findVersionesActivasById(
        (short) 999,
        SERVICE_ONBOARDING
    );

    // Then
    assertThat(version).isNull();
  }

  @Test
  void shouldFindActiveVersionIdByServicioNombre() {
    // When
    Optional<Integer> versionId = versionRepository
        .findActiveVersionIdByServicioNombre(SERVICE_ONBOARDING);

    // Then
    assertThat(versionId)
        .isPresent()
        .get()
        .satisfies(id -> assertThat(id).isPositive());
  }

  @Test
  void shouldReturnEmptyWhenServiceHasNoActiveVersion() {
    // When
    Optional<Integer> versionId = versionRepository
        .findActiveVersionIdByServicioNombre(SERVICE_NON_EXISTENT);

    // Then
    assertThat(versionId).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNullServicioNombre() {
    // When
    Optional<Integer> versionId = versionRepository
        .findActiveVersionIdByServicioNombre(null);

    // Then
    assertThat(versionId).isEmpty();
  }

  @Test
  void shouldHandleCaseInsensitiveServiceName() {
    // When
    Optional<Integer> versionIdUpperCase = versionRepository
        .findActiveVersionIdByServicioNombre(SERVICE_ONBOARDING);

    // Then
    assertThat(versionIdUpperCase).isPresent();
  }
}
