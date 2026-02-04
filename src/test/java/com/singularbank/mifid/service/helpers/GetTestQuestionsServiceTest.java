package com.singularbank.mifid.service.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.entity.Version;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.repository.VersionRepository;
import com.singularbank.mifid.service.helpers.impl.GetTestQuestionsServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTestQuestionsServiceTest {

  @Mock
  private VersionRepository versionRepository;

  @Mock
  private ItemRepository itemRepository;

  @InjectMocks
  private GetTestQuestionsServiceImpl getTestQuestionsService;

  private static final String APPLICATION = "ONBOARDING";
  private static final Short VERSION_ID = 1;
  private static final Short LATEST_VERSION = 5;
  private static final String TEST_USER = "test-user";
  private static final String ERROR_APPLICATION_NULL = "application is required";
  private static final String ERROR_TEST_TYPE_NULL = "testType is required";

  private static final Integer PREGUNTA_ID = 1;
  private static final Integer RESPUESTA_ID = 10;

  private static final String PREGUNTA_TEXTO = "Pregunta de prueba";
  private static final String RESPUESTA_TEXTO = "Respuesta de prueba";
  private static final String VERSION_DESCRIPCION = "Version de prueba";
  private static final String FAMILIAS = "A,B";
  private static final Short ORDEN = 1;

  @ParameterizedTest
  @EnumSource(TypeTest.class)
  void shouldThrowExceptionWhenNoVersionFoundForAnyTestType(TypeTest typeTest) {
    // Given
    when(itemRepository.getLastVersion(typeTest, APPLICATION))
        .thenReturn(null);

    // When & Then
    assertThatThrownBy(() -> getTestQuestionsService.getTest(APPLICATION, typeTest, null))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No versions found")
        .hasMessageContaining(typeTest.name().toLowerCase())
        .hasMessageContaining(APPLICATION);

    verify(itemRepository).getLastVersion(typeTest, APPLICATION);
    verify(itemRepository, never()).findQuestionsWithAnswersByTestType(
        typeTest, APPLICATION, null);
  }

  @ParameterizedTest
  @EnumSource(TypeTest.class)
  void shouldThrowExceptionWhenNoQuestionsFoundForAnyTestType(TypeTest typeTest) {
    // Given
    when(itemRepository.findQuestionsWithAnswersByTestType(
        typeTest, APPLICATION, VERSION_ID))
        .thenReturn(Collections.emptyList());

    // When & Then
    assertThatThrownBy(() -> getTestQuestionsService.getTest(APPLICATION, typeTest, VERSION_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No " + typeTest.name().toLowerCase() + " test found");
  }

  @Test
  void shouldWorkForConvenienceTest() {
    // Given
    when(versionRepository.findVersionesActivasById(VERSION_ID, APPLICATION))
        .thenReturn(createMockVersion(VERSION_ID));

    when(itemRepository.findQuestionsWithAnswersByTestType(
        TypeTest.CONVENIENCE, APPLICATION, VERSION_ID))
        .thenReturn(List.of(createMockQuestion()));

    // When
    var result = getTestQuestionsService.getTest(APPLICATION, TypeTest.CONVENIENCE, VERSION_ID);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getVersion()).isNotNull();
    assertThat(result.getVersion().getId()).isEqualTo(VERSION_ID);
    assertThat(result.getQuestions()).isNotEmpty().hasSize(1);

    verify(itemRepository).findQuestionsWithAnswersByTestType(
        TypeTest.CONVENIENCE, APPLICATION, VERSION_ID);
  }

  @Test
  void shouldWorkForSuitabilityTest() {
    // Given
    when(versionRepository.findVersionesActivasById(VERSION_ID, APPLICATION))
        .thenReturn(createMockVersion(VERSION_ID));

    when(itemRepository.findQuestionsWithAnswersByTestType(
        TypeTest.SUITABILITY, APPLICATION, VERSION_ID))
        .thenReturn(List.of(createMockQuestion()));

    // When
    var result = getTestQuestionsService.getTest(APPLICATION, TypeTest.SUITABILITY, VERSION_ID);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getVersion()).isNotNull();
    assertThat(result.getQuestions()).isNotEmpty();

    verify(itemRepository).findQuestionsWithAnswersByTestType(
        TypeTest.SUITABILITY, APPLICATION, VERSION_ID);
  }

  @Test
  void shouldWorkForSustainabilityTest() {
    // Given
    when(versionRepository.findVersionesActivasById(VERSION_ID, APPLICATION))
        .thenReturn(createMockVersion(VERSION_ID));

    when(itemRepository.findQuestionsWithAnswersByTestType(
        TypeTest.SUSTAINABILITY, APPLICATION, VERSION_ID))
        .thenReturn(List.of(createMockQuestion()));

    // When
    var result = getTestQuestionsService.getTest(APPLICATION, TypeTest.SUSTAINABILITY, VERSION_ID);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getVersion()).isNotNull();
    assertThat(result.getQuestions()).isNotEmpty();

    verify(itemRepository).findQuestionsWithAnswersByTestType(
        TypeTest.SUSTAINABILITY, APPLICATION, VERSION_ID);
  }

  @Test
  void shouldUseLatestVersionWhenVersionIsNull() {
    // Given
    when(itemRepository.getLastVersion(TypeTest.CONVENIENCE, APPLICATION))
        .thenReturn(LATEST_VERSION);

    when(versionRepository.findVersionesActivasById(LATEST_VERSION, APPLICATION))
        .thenReturn(createMockVersion(LATEST_VERSION));

    when(itemRepository.findQuestionsWithAnswersByTestType(
        TypeTest.CONVENIENCE, APPLICATION, LATEST_VERSION))
        .thenReturn(List.of(createMockQuestion()));

    // When
    var result = getTestQuestionsService.getTest(APPLICATION, TypeTest.CONVENIENCE, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getVersion()).isNotNull();

    verify(itemRepository).getLastVersion(TypeTest.CONVENIENCE, APPLICATION);
    verify(itemRepository).findQuestionsWithAnswersByTestType(
        TypeTest.CONVENIENCE, APPLICATION, LATEST_VERSION);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenApplicationIsNull() {
    try {
      getTestQuestionsService.getTest(APPLICATION, null, null);
      fail();
    } catch (NullPointerException e) {
      assertEquals(ERROR_TEST_TYPE_NULL, e.getMessage());
      verifyNoInteractions(itemRepository);
    }
  }

  @Test
  void shouldThrowNullPointerExceptionWhenTestTypeIsNull() {
    try {
      getTestQuestionsService.getTest(null, null, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals(ERROR_APPLICATION_NULL, e.getMessage());
      verifyNoInteractions(itemRepository);
    }
  }

  @Test
  void shouldThrowNullPointerExceptionWhenBothParametersAreNull() {
    try {
      getTestQuestionsService.getTest(null, null, null);
      fail();
    } catch (NullPointerException e) {
      assertEquals(ERROR_APPLICATION_NULL, e.getMessage());
      verifyNoInteractions(itemRepository);
    }
  }

  private Question createMockQuestion() {
    return Question.builder()
        .id(PREGUNTA_ID)
        .typeId((short) 1)
        .text(PREGUNTA_TEXTO)
        .families(FAMILIAS)
        .order(ORDEN)
        .testType("CO")
        .hasCorrectAnswer(true)
        .createdBy(TEST_USER)
        .createdAt(LocalDateTime.now())
        .startDate(LocalDate.now())
        .answers(List.of(createMockAnswer()))
        .build();
  }

  private Answer createMockAnswer() {
    return Answer.builder()
        .id(RESPUESTA_ID)
        .text(RESPUESTA_TEXTO)
        .value("A")
        .correct(true)
        .freeText(false)
        .createdBy(TEST_USER)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private Version createMockVersion(Short versionId) {
    return Version.builder()
        .id(versionId)
        .descripcion(VERSION_DESCRIPCION)
        .activa(true)
        .fechaInicio(LocalDate.now())
        .fechaAlta(LocalDateTime.now())
        .build();
  }
}