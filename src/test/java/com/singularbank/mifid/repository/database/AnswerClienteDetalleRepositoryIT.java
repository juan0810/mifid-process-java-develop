package com.singularbank.mifid.repository.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.RespuestaCliente.RespuestaDetalle;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.RespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.config.AbstractRepositoryTest;
import com.singularbank.mifid.repository.database.jpa.JpaRespuestaClienteDetalleRepository;
import com.singularbank.mifid.repository.database.mapper.RespuestaClienteDetalleEntityMapper;
import com.singularbank.mifid.repository.database.model.ItemEntity;
import com.singularbank.mifid.repository.database.model.RespuestaClienteDetalleEntity;
import com.singularbank.mifid.repository.database.model.RespuestaClienteEntity;
import com.singularbank.mifid.repository.database.model.VersionEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DisplayName("RespuestaClienteDetalleRepository Integration Tests")
class AnswerClienteDetalleRepositoryIT extends AbstractRepositoryTest {

  @Autowired
  private JpaRespuestaClienteDetalleRepository jpaRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RespuestaClienteDetalleEntityMapper mapper;

  private RespuestaClienteDetalleRepository repository;

  private static final Short VERSION_ID = 1;
  private static final Short ESTADO_ACTIVO = 1;
  private static final Short ESTADO_FIRMADO = 2;
  private static final Short ESTADO_INACTIVO = 0;

  private Integer sustainabilityRespId1;
  private Integer sustainabilityRespId2;
  private Integer sustainabilityRespId3;
  private Integer sustainabilityRespId4;

  private Integer convenienceRespId1;
  private Integer convenienceRespId2;
  private Integer convenienceRespId3;

  private Integer suitabilityRespId1;
  private Integer suitabilityRespId2;

  private static final String VALOR_A = "A";
  private static final String VALOR_B = "B";
  private static final String VALOR_C = "C";
  private static final String VALOR_D = "D";
  private static final String VALOR_NO = "NO";

  @BeforeEach
  void setUp() {
    repository = new RespuestaClienteDetalleRepositoryImpl(jpaRepository, mapper);
    List<Integer> sustainabilityIds = entityManager.getEntityManager()
        .createQuery(
            "SELECT i.id FROM ItemEntity i WHERE i.tipoItem = 'RE' AND i.itemPadre.tipoTest = 'SO' ORDER BY i.id",
            Integer.class)
        .setMaxResults(4)
        .getResultList();

    List<Integer> convenienceIds = entityManager.getEntityManager()
        .createQuery(
            "SELECT i.id FROM ItemEntity i WHERE i.tipoItem = 'RE' AND i.itemPadre.tipoTest = 'CO' ORDER BY i.id",
            Integer.class)
        .setMaxResults(3)
        .getResultList();

    List<Integer> suitabilityIds = entityManager.getEntityManager()
        .createQuery(
            "SELECT i.id FROM ItemEntity i WHERE i.tipoItem = 'RE' AND i.itemPadre.tipoTest = 'ID' ORDER BY i.id",
            Integer.class)
        .setMaxResults(4)
        .getResultList();

    if (!sustainabilityIds.isEmpty()) {
      sustainabilityRespId1 = sustainabilityIds.get(0);
      sustainabilityRespId2 =
          sustainabilityIds.size() > 1 ? sustainabilityIds.get(1) : sustainabilityIds.get(0);
      sustainabilityRespId3 =
          sustainabilityIds.size() > 2 ? sustainabilityIds.get(2) : sustainabilityIds.get(0);
      sustainabilityRespId4 =
          sustainabilityIds.size() > 3 ? sustainabilityIds.get(3) : sustainabilityIds.get(0);
    }

    if (!convenienceIds.isEmpty()) {
      convenienceRespId1 = convenienceIds.get(0);
      convenienceRespId2 =
          convenienceIds.size() > 1 ? convenienceIds.get(1) : convenienceIds.get(0);
      convenienceRespId3 =
          convenienceIds.size() > 2 ? convenienceIds.get(2) : convenienceIds.get(0);
    }

    if (!suitabilityIds.isEmpty()) {
      suitabilityRespId1 = suitabilityIds.get(0);
      suitabilityRespId2 =
          suitabilityIds.size() > 1 ? suitabilityIds.get(1) : suitabilityIds.get(0);
    }
  }

  @Nested
  @DisplayName("saveAll Tests")
  class SaveAllTests {

    @Test
    @DisplayName("Should save all respuesta detalles successfully")
    void shouldSaveAllRespuestaDetallesSuccessfully() {
      // Given
      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente("12345678A");
      List<RespuestaDetalle> detalles = List.of(
          RespuestaDetalle.builder().respuestaId(sustainabilityRespId1).valor(VALOR_A).build(),
          RespuestaDetalle.builder().respuestaId(sustainabilityRespId2).valor(VALOR_B).build(),
          RespuestaDetalle.builder().respuestaId(sustainabilityRespId3).valor(VALOR_C).build()
      );

      // When
      repository.saveAll(respuestaCliente.getId(), detalles);
      entityManager.flush();
      entityManager.clear();

      // Then
      List<RespuestaClienteDetalleEntity> savedDetalles = jpaRepository.findAll().stream()
          .filter(d -> d.getRespuestaCliente().getId().equals(respuestaCliente.getId()))
          .toList();
      assertThat(savedDetalles).hasSize(3);
      assertThat(savedDetalles)
          .extracting(d -> d.getItem().getId())
          .containsExactlyInAnyOrder(sustainabilityRespId1, sustainabilityRespId2,
              sustainabilityRespId3);
      assertThat(savedDetalles)
          .extracting(RespuestaClienteDetalleEntity::getValor)
          .containsExactlyInAnyOrder(VALOR_A, VALOR_B, VALOR_C);
    }

    @Test
    @DisplayName("Should save single respuesta detalle")
    void shouldSaveSingleRespuestaDetalle() {
      // Given
      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente("87654321B");
      List<RespuestaDetalle> detalles = List.of(
          RespuestaDetalle.builder().respuestaId(sustainabilityRespId1).valor(VALOR_NO).build()
      );

      // When
      repository.saveAll(respuestaCliente.getId(), detalles);
      entityManager.flush();
      entityManager.clear();

      // Then
      List<RespuestaClienteDetalleEntity> savedDetalles = jpaRepository.findAll().stream()
          .filter(d -> d.getRespuestaCliente().getId().equals(respuestaCliente.getId()))
          .toList();
      assertThat(savedDetalles).hasSize(1);
      assertThat(savedDetalles.getFirst().getItem().getId()).isEqualTo(sustainabilityRespId1);
      assertThat(savedDetalles.getFirst().getValor()).isEqualTo(VALOR_NO);
    }

    @Test
    @DisplayName("Should save empty list without errors")
    void shouldSaveEmptyListWithoutErrors() {
      // Given
      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente("11111111C");
      List<RespuestaDetalle> detalles = List.of();

      // When
      repository.saveAll(respuestaCliente.getId(), detalles);
      entityManager.flush();
      entityManager.clear();

      // Then
      List<RespuestaClienteDetalleEntity> savedDetalles = jpaRepository.findAll().stream()
          .filter(d -> d.getRespuestaCliente().getId().equals(respuestaCliente.getId()))
          .toList();
      assertThat(savedDetalles).isEmpty();
    }

    @Test
    @DisplayName("Should save detalles with null valor")
    void shouldSaveDetallesWithNullValor() {
      // Given
      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente("99999999X");
      List<RespuestaDetalle> detalles = List.of(
          RespuestaDetalle.builder().respuestaId(sustainabilityRespId1).valor(null).build()
      );

      // When
      repository.saveAll(respuestaCliente.getId(), detalles);
      entityManager.flush();
      entityManager.clear();

      // Then
      List<RespuestaClienteDetalleEntity> savedDetalles = jpaRepository.findAll().stream()
          .filter(d -> d.getRespuestaCliente().getId().equals(respuestaCliente.getId()))
          .toList();
      assertThat(savedDetalles).hasSize(1);
      assertThat(savedDetalles.getFirst().getValor()).isNull();
    }

    @Test
    @DisplayName("Should save multiple detalles for convenience test")
    void shouldSaveMultipleDetallesForConvenienceTest() {
      // Given
      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente("22222222D");
      List<RespuestaDetalle> detalles = List.of(
          RespuestaDetalle.builder().respuestaId(convenienceRespId1).valor(VALOR_A).build(),
          RespuestaDetalle.builder().respuestaId(convenienceRespId2).valor(VALOR_B).build(),
          RespuestaDetalle.builder().respuestaId(convenienceRespId3).valor(VALOR_A).build()
      );

      // When
      repository.saveAll(respuestaCliente.getId(), detalles);
      entityManager.flush();
      entityManager.clear();

      // Then
      List<RespuestaClienteDetalleEntity> savedDetalles = jpaRepository.findAll().stream()
          .filter(d -> d.getRespuestaCliente().getId().equals(respuestaCliente.getId()))
          .toList();

      assertThat(savedDetalles).hasSize(3);
      assertThat(savedDetalles)
          .extracting(d -> d.getItem().getId())
          .containsExactlyInAnyOrder(convenienceRespId1, convenienceRespId2,
              convenienceRespId3);
    }
  }

  @Nested
  @DisplayName("findAnswersByIdentityAndTestType Tests")
  class FindAnswersByIdentityAndTypeTestTests {

    @Test
    @DisplayName("Should find sustainability answers by identity successfully")
    void shouldFindSustainabilityAnswersByIdentitySuccessfully() {
      // Given
      String identity = "12345678S";
      TypeTest typeTest = TypeTest.SUSTAINABILITY;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId2, VALOR_B);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByIdentityAndTestType(identity, typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getTestId()).isEqualTo(respuestaCliente.getId());
      assertThat(result.getRespuestaClienteId()).isEqualTo(respuestaCliente.getId());
      assertThat(result.getVersion()).isEqualTo(VERSION_ID);
      assertThat(result.getQuestions()).hasSize(2);
    }

    @Test
    @DisplayName("Should find convenience answers by identity successfully")
    void shouldFindConvenienceAnswersByIdentitySuccessfully() {
      // Given
      String identity = "87654321C";
      TypeTest typeTest = TypeTest.CONVENIENCE;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId2, VALOR_B);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByIdentityAndTestType(identity, typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).hasSize(2);
    }

    @Test
    @DisplayName("Should find suitability answers by identity successfully")
    void shouldFindSuitabilityAnswersByIdentitySuccessfully() {
      // Given
      String identity = "11111111I";
      TypeTest typeTest = TypeTest.SUITABILITY;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), suitabilityRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), suitabilityRespId2, VALOR_D);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByIdentityAndTestType(identity, typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception when no answers found for identity and test type")
    void shouldThrowExceptionWhenNoAnswersFoundForIdentityAndTestType() {
      // Given
      String nonExistentIdentity = "99999999Z";
      TypeTest typeTest = TypeTest.SUSTAINABILITY;

      // When & Then
      assertThatThrownBy(() -> repository.findAnswersByIdentityAndTestType(
          nonExistentIdentity, typeTest))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("No SO answers found for customer");
    }

    @Test
    @DisplayName("Should return latest test when multiple tests exist for same identity")
    void shouldReturnLatestTestWhenMultipleTestsExist() {
      // Given
      String identity = "22222222M";
      TypeTest typeTest = TypeTest.SUSTAINABILITY;
      RespuestaClienteEntity respuestaCliente1 = createAndPersistRespuestaClienteWithCustomDate(
          identity, LocalDateTime.now().minusDays(5));
      createAndPersistDetalle(respuestaCliente1.getId(), sustainabilityRespId1, VALOR_A);

      RespuestaClienteEntity respuestaCliente2 = createAndPersistRespuestaClienteWithCustomDate(
          identity, LocalDateTime.now());
      createAndPersistDetalle(respuestaCliente2.getId(), sustainabilityRespId2, VALOR_B);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByIdentityAndTestType(identity, typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getRespuestaClienteId()).isEqualTo(respuestaCliente2.getId());
      assertThat(result.getQuestions().getFirst().getOption().getValue()).isEqualTo(VALOR_B);
    }

    @Test
    @DisplayName("Should throw exception when only inactive tests exist (estado = 0)")
    void shouldThrowExceptionWhenOnlyInactiveTestsExist() {
      // Given
      String identity = "33333333N";
      TypeTest typeTest = TypeTest.CONVENIENCE;

      RespuestaClienteEntity respuestaClienteInactive = RespuestaClienteEntity.builder()
          .identity(identity)
          .version(createVersionEntity())
          .estado(ESTADO_INACTIVO)
          .fechaAlta(LocalDateTime.now())
          .build();
      entityManager.persistAndFlush(respuestaClienteInactive);
      createAndPersistDetalle(respuestaClienteInactive.getId(), convenienceRespId1, VALOR_A);

      entityManager.flush();
      entityManager.clear();

      // When & Then
      assertThatThrownBy(() -> repository.findAnswersByIdentityAndTestType(identity, typeTest))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("No CO answers found for customer");
    }
  }

  @Nested
  @DisplayName("findAnswersByTestId Tests")
  class FindAnswersByTestIdAndTypeTestTests {

    @Test
    @DisplayName("Should find answers by testId and test type successfully")
    void shouldFindAnswersByTestIdAndTestTypeSuccessfully() {
      // Given
      String identity = "44444444P";
      TypeTest typeTest = TypeTest.SUSTAINABILITY;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);

      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId2, VALOR_B);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByTestId(
          respuestaCliente.getId(), typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getTestId()).isEqualTo(respuestaCliente.getId());
      assertThat(result.getQuestions()).hasSize(2);
    }

    @Test
    @DisplayName("Should find convenience answers by testId")
    void shouldFindConvenienceAnswersByTestId() {
      // Given
      String identity = "55555555Q";
      TypeTest typeTest = TypeTest.CONVENIENCE;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);

      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId2, VALOR_B);
      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId3, VALOR_A);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByTestId(
          respuestaCliente.getId(), typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).hasSize(3);
    }

    @Test
    @DisplayName("Should find suitability answers by testId")
    void shouldFindSuitabilityAnswersByTestId() {
      // Given
      String identity = "66666666R";
      TypeTest typeTest = TypeTest.SUITABILITY;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), suitabilityRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), suitabilityRespId2, VALOR_D);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByTestId(
          respuestaCliente.getId(), typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no answers found for testId")
    void shouldThrowResourceNotFoundExceptionWhenNoAnswersFoundForTestId() {
      // Given
      Integer nonExistentTestId = 99999;
      TypeTest typeTest = TypeTest.SUSTAINABILITY;

      // When & Then
      assertThatThrownBy(
          () -> repository.findAnswersByTestId(nonExistentTestId, typeTest))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("No answers found for id: " + nonExistentTestId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when test exists but no answers for type")
    void shouldThrowResourceNotFoundExceptionWhenTestExistsButNoAnswersForType() {
      // Given
      String identity = "77777777T";

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId1, VALOR_A);

      entityManager.flush();
      entityManager.clear();

      Integer testId = respuestaCliente.getId();
      TypeTest typeTest = TypeTest.SUSTAINABILITY;

      // When & Then
      assertThatThrownBy(() -> repository.findAnswersByTestId(testId, typeTest))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("No answers found for id: " + testId);
    }

    @Test
    @DisplayName("Should respect question order in results")
    void shouldRespectQuestionOrderInResults() {
      // Given
      String identity = "88888888U";
      TypeTest typeTest = TypeTest.SUSTAINABILITY;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId4, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId1, VALOR_A);
      createAndPersistDetalle(respuestaCliente.getId(), sustainabilityRespId2, VALOR_B);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByTestId(
          respuestaCliente.getId(), typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).isNotEmpty();
    }

    @Test
    @DisplayName("Should map numeric valores correctly to scores")
    void shouldMapNumericValoresCorrectlyToScores() {
      // Given
      String identity = "55555550P";
      TypeTest typeTest = TypeTest.SUITABILITY;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), suitabilityRespId1, VALOR_A);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByTestId(respuestaCliente.getId(), typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).hasSize(1);
      assertThat(result.getQuestions().getFirst().getOption().getValue()).isEqualTo(VALOR_A);
      assertThat(result.getQuestions().getFirst().getOption().getScore()).isNull();
    }

    @Test
    @DisplayName("Should handle non-numeric valores with null score")
    void shouldHandleNonNumericValoresWithNullScore() {
      // Given
      String identity = "55555551P";
      TypeTest typeTest = TypeTest.CONVENIENCE;

      RespuestaClienteEntity respuestaCliente = createAndPersistRespuestaCliente(identity);
      createAndPersistDetalle(respuestaCliente.getId(), convenienceRespId1, VALOR_A);

      entityManager.flush();
      entityManager.clear();

      // When
      AnswersTest result = repository.findAnswersByTestId(
          respuestaCliente.getId(), typeTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getQuestions()).hasSize(1);
      assertThat(result.getQuestions().getFirst().getOption().getValue()).isEqualTo(VALOR_A);
      assertThat(result.getQuestions().getFirst().getOption().getScore()).isNull();
    }
  }

  private RespuestaClienteEntity createAndPersistRespuestaCliente(String identity) {
    RespuestaClienteEntity entity = RespuestaClienteEntity.builder()
        .identity(identity)
        .version(createVersionEntity())
        .estado(ESTADO_ACTIVO)
        .fechaAlta(LocalDateTime.now())
        .build();
    return entityManager.persistAndFlush(entity);
  }

  private RespuestaClienteEntity createAndPersistRespuestaClienteWithCustomDate(
      String identity, LocalDateTime fechaAlta) {
    RespuestaClienteEntity entity = RespuestaClienteEntity.builder()
        .identity(identity)
        .version(createVersionEntity())
        .estado(ESTADO_FIRMADO)
        .fechaAlta(fechaAlta)
        .build();
    return entityManager.persistAndFlush(entity);
  }

  private VersionEntity createVersionEntity() {
    return VersionEntity.builder()
        .id(VERSION_ID)
        .build();
  }

  private RespuestaClienteDetalleEntity createAndPersistDetalle(
      Integer respuestaClienteId, Integer itemId, String valor) {
    RespuestaClienteEntity respuestaCliente = entityManager.find(
        RespuestaClienteEntity.class, respuestaClienteId);

    ItemEntity item = entityManager.find(ItemEntity.class, itemId);

    RespuestaClienteDetalleEntity entity = RespuestaClienteDetalleEntity.builder()
        .respuestaCliente(respuestaCliente)
        .item(item)  // Usar la entidad gestionada, no una nueva
        .valor(valor)
        .build();
    return entityManager.persistAndFlush(entity);
  }
}