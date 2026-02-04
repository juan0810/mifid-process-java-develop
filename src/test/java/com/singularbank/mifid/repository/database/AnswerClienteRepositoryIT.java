package com.singularbank.mifid.repository.database;

import static org.assertj.core.api.Assertions.assertThat;

import com.singularbank.mifid.entity.CompleteTestInfo;
import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.repository.config.AbstractRepositoryTest;
import com.singularbank.mifid.repository.database.jpa.JpaRespuestaClienteRepository;
import com.singularbank.mifid.repository.database.mapper.RespuestaClienteEntityMapper;
import com.singularbank.mifid.repository.database.mapper.TestHistoryProjectionMapper;
import com.singularbank.mifid.repository.database.mapper.TestProjectionMapper;
import com.singularbank.mifid.repository.database.model.RespuestaClienteEntity;
import com.singularbank.mifid.repository.database.model.VersionEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

class AnswerClienteRepositoryIT extends AbstractRepositoryTest {

  @Autowired
  private JpaRespuestaClienteRepository jpaRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RespuestaClienteEntityMapper respuestaClienteEntityMapper;

  @Autowired
  private TestProjectionMapper testProjectionMapper;

  @Autowired
  private TestHistoryProjectionMapper testHistoryProjectionMapper;

  private RespuestaClienteRepository respuestaClienteRepository;

  private static final String CLIENT_DNI = "12345678A";
  private static final Short VERSION_ID = 1;
  private static final String CONVENIENCE_RESULT = "Conveniente para familias: A, B";
  private static final String SUITABILITY_RESULT = "Perfil Conservador";

  @BeforeEach
  void setUp() {
    respuestaClienteRepository = new RespuestaClienteRepositoryImpl(
        jpaRepository,
        respuestaClienteEntityMapper,
        testProjectionMapper,
        testHistoryProjectionMapper
    );
  }

  @Test
  void shouldReturnEmptyWhenAnswersByIdentityNotFound() {
    // When
    Optional<RespuestaCliente> result = respuestaClienteRepository.findAnswersByIdentity(
        "NON_EXISTENT_DNI");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenConvenienceTestNotFound() {
    // When
    Optional<RespuestaCliente> result = respuestaClienteRepository
        .findConvenienceTestByIdentityAndVersion("NON_EXISTENT_DNI", VERSION_ID);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldNotFindConvenienceTestWhenResultIsNull() {
    // Given
    String uniqueDni = "11111111A";
    createRespuestaCliente(uniqueDni, VERSION_ID, StateTest.SIGNED.getCode(), null, null);
    entityManager.flush();
    entityManager.clear();

    // When
    Optional<RespuestaCliente> result = respuestaClienteRepository
        .findConvenienceTestByIdentityAndVersion(uniqueDni, VERSION_ID);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldNotFindConvenienceTestWhenStateIsBorrador() {
    // Given - Test en estado BORRADOR
    String uniqueDni = "22222222B";
    createRespuestaCliente(
        uniqueDni,
        VERSION_ID,
        StateTest.DRAFT.getCode(),
        CONVENIENCE_RESULT,
        null
    );
    entityManager.flush();
    entityManager.clear();

    // When
    Optional<RespuestaCliente> result = respuestaClienteRepository
        .findConvenienceTestByIdentityAndVersion(uniqueDni, VERSION_ID);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldSaveNewRespuestaCliente() {
    // Given
    RespuestaCliente newRespuestaCliente = RespuestaCliente.builder()
        .clienteDni(CLIENT_DNI)
        .versionId(VERSION_ID)
        .estado(StateTest.SIGNED)
        .resultadoConveniencia(CONVENIENCE_RESULT)
        .resultadoIdoneidad(SUITABILITY_RESULT)
        .build();

    // When
    RespuestaCliente savedRespuestaCliente = respuestaClienteRepository.save(newRespuestaCliente);

    entityManager.flush();
    entityManager.clear();

    // Then
    assertThat(savedRespuestaCliente.getId()).isNotNull();
    assertThat(savedRespuestaCliente)
        .extracting("clienteDni", "versionId", "estado")
        .containsExactly(CLIENT_DNI, VERSION_ID, StateTest.SIGNED);

    assertThat(savedRespuestaCliente)
        .extracting("resultadoConveniencia", "resultadoIdoneidad")
        .containsExactly(CONVENIENCE_RESULT, SUITABILITY_RESULT);

    // Verify
    RespuestaClienteEntity entityInDb = entityManager.find(
        RespuestaClienteEntity.class,
        savedRespuestaCliente.getId()
    );
    assertThat(entityInDb).isNotNull();
    assertThat(entityInDb.getIdentity()).isEqualTo(CLIENT_DNI);
  }

  @Test
  void shouldSaveRespuestaClienteWithBorradorState() {
    // Given
    RespuestaCliente newRespuestaCliente = RespuestaCliente.builder()
        .clienteDni(CLIENT_DNI)
        .versionId(VERSION_ID)
        .estado(StateTest.DRAFT)
        .build();

    // When
    RespuestaCliente savedRespuestaCliente = respuestaClienteRepository.save(newRespuestaCliente);

    entityManager.flush();
    entityManager.clear();

    // Then
    assertThat(savedRespuestaCliente.getId()).isNotNull();
    assertThat(savedRespuestaCliente.getEstado()).isEqualTo(StateTest.DRAFT);
  }

  @Test
  void shouldSaveRespuestaClienteWithNullResults() {
    // Given
    RespuestaCliente newRespuestaCliente = RespuestaCliente.builder()
        .clienteDni(CLIENT_DNI)
        .versionId(VERSION_ID)
        .estado(StateTest.SIGNED)
        .resultadoConveniencia(null)
        .resultadoIdoneidad(null)
        .build();

    // When
    RespuestaCliente savedRespuestaCliente = respuestaClienteRepository.save(newRespuestaCliente);

    entityManager.flush();
    entityManager.clear();

    // Then
    assertThat(savedRespuestaCliente.getId()).isNotNull();
    assertThat(savedRespuestaCliente)
        .extracting("resultadoConveniencia", "resultadoIdoneidad")
        .containsExactly(null, null);
  }

  @Test
  void shouldReturnEmptyListWhenNoActiveTests() {
    // When
    List<CompleteTestInfo> activeTests = respuestaClienteRepository
        .findCurrentActiveTestsByIdentity("NON_EXISTENT_DNI");

    // Then
    assertThat(activeTests).isEmpty();
  }

  @Test
  void shouldReturnAllThreeTestTypesWhenAllSavedInSameRecord() {
    // Given
    String uniqueDni = "TEST12345";
    String convenienceResult = "A,B,C";
    String suitabilityResult = "Conservador";

    RespuestaClienteEntity respuestaCliente = createRespuestaCliente(
        uniqueDni,
        VERSION_ID,
        StateTest.SIGNED.getCode(),
        convenienceResult,
        suitabilityResult
    );
    createSustainabilityDetails(respuestaCliente.getId());

    entityManager.flush();
    entityManager.clear();

    // When
    List<CompleteTestInfo> activeTests = respuestaClienteRepository
        .findCurrentActiveTestsByIdentity(uniqueDni);

    // Then
    assertThat(activeTests).hasSize(3);
    assertThat(activeTests)
        .extracting(CompleteTestInfo::getTypeTest)
        .containsExactlyInAnyOrder(
            TypeTest.CONVENIENCE,
            TypeTest.SUITABILITY,
            TypeTest.SUSTAINABILITY
        );

    // Verify
    CompleteTestInfo convenienceTest = activeTests.stream()
        .filter(t -> t.getTypeTest() == TypeTest.CONVENIENCE)
        .findFirst()
        .orElseThrow();
    assertThat(convenienceTest.getConvenienceResult()).isEqualTo(convenienceResult);

    CompleteTestInfo suitabilityTest = activeTests.stream()
        .filter(t -> t.getTypeTest() == TypeTest.SUITABILITY)
        .findFirst()
        .orElseThrow();
    assertThat(suitabilityTest.getSuitabilityResult()).isEqualTo(suitabilityResult);

    CompleteTestInfo sustainabilityTest = activeTests.stream()
        .filter(t -> t.getTypeTest() == TypeTest.SUSTAINABILITY)
        .findFirst()
        .orElseThrow();
    assertThat(sustainabilityTest).isNotNull();
  }

  @Test
  void shouldReturnOnlyConvenienceWhenOnlyConvenienceIsSaved() {
    // Given
    String uniqueDni = "CONV12345";
    String convenienceResult = "D,E,F";

    createRespuestaCliente(
        uniqueDni,
        VERSION_ID,
        StateTest.SIGNED.getCode(),
        convenienceResult,
        null  // No suitability
    );

    entityManager.flush();
    entityManager.clear();

    // When
    List<CompleteTestInfo> activeTests = respuestaClienteRepository
        .findCurrentActiveTestsByIdentity(uniqueDni);

    // Then
    assertThat(activeTests).hasSize(1);
    assertThat(activeTests.getFirst().getTypeTest())
        .isEqualTo(TypeTest.CONVENIENCE);
    assertThat(activeTests.getFirst().getConvenienceResult()).isEqualTo(convenienceResult);
  }

  @Test
  void shouldReturnOnlySuitabilityWhenOnlySuitabilityIsSaved() {
    // Given
    String uniqueDni = "SUIT12345";
    String suitabilityResult = "Arriesgado";

    createRespuestaCliente(
        uniqueDni,
        VERSION_ID,
        StateTest.SIGNED.getCode(),
        null,
        suitabilityResult
    );

    entityManager.flush();
    entityManager.clear();

    // When
    List<CompleteTestInfo> activeTests = respuestaClienteRepository
        .findCurrentActiveTestsByIdentity(uniqueDni);

    // Then
    assertThat(activeTests).hasSize(1);
    assertThat(activeTests.getFirst().getTypeTest())
        .isEqualTo(TypeTest.SUITABILITY);
    assertThat(activeTests.getFirst().getSuitabilityResult()).isEqualTo(suitabilityResult);
  }

  @Test
  void shouldReturnConvenienceAndSuitabilityWhenBothSaved() {
    // Given
    String uniqueDni = "BOTH12345";
    String convenienceResult = "A,B";
    String suitabilityResult = "Moderado";

    createRespuestaCliente(
        uniqueDni,
        VERSION_ID,
        StateTest.SIGNED.getCode(),
        convenienceResult,
        suitabilityResult
    );

    entityManager.flush();
    entityManager.clear();

    // When
    List<CompleteTestInfo> activeTests = respuestaClienteRepository
        .findCurrentActiveTestsByIdentity(uniqueDni);

    // Then
    assertThat(activeTests).hasSize(2);
    assertThat(activeTests)
        .extracting(CompleteTestInfo::getTypeTest)
        .containsExactlyInAnyOrder(
            TypeTest.CONVENIENCE,
            TypeTest.SUITABILITY
        );
  }

  private void createSustainabilityDetails(Integer respuestaClienteId) {
    Integer sustainabilityAnswerId = entityManager.getEntityManager()
        .createQuery("""
            SELECT i.id FROM ItemEntity i 
            WHERE i.tipoItem = 'RE' 
            AND i.itemPadre.tipoTest = 'SO'
            ORDER BY i.id
            """, Integer.class)
        .setMaxResults(1)
        .getSingleResult();

    String insertSql = """
        INSERT INTO mktmrespuestaclientedetalle 
        (rdrespuestaclienteid, rdrespuestaid, rdvalor, rdfecalta) 
        VALUES (?, ?, 'NO', CURRENT_TIMESTAMP)
        """;

    entityManager.getEntityManager()
        .createNativeQuery(insertSql)
        .setParameter(1, respuestaClienteId)
        .setParameter(2, sustainabilityAnswerId)
        .executeUpdate();
  }

  private RespuestaClienteEntity createRespuestaCliente(
      String identity,
      Short versionId,
      Integer estado,
      String resultadoConveniencia,
      String resultadoIdoneidad) {
    RespuestaClienteEntity entity = RespuestaClienteEntity.builder()
        .identity(identity)
        .version(VersionEntity.builder()
            .id(versionId)
            .build())
        .estado(estado.shortValue())
        .resultadoConveniencia(resultadoConveniencia)
        .resultadoIdoneidad(resultadoIdoneidad)
        .fechaAlta(LocalDateTime.now())
        .build();
    return entityManager.persistAndFlush(entity);
  }
}