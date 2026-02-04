package com.singularbank.mifid.client.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.lib.rest.adapter.model.RestResponse;
import com.singularbank.lib.rest.adapter.service.GenericRestService;
import com.singularbank.mifid.client.pdf.response.GenerationPdfResponse;
import com.singularbank.mifid.exception.ExternalServiceException;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratorPdfClient Tests")
class GeneratorPdfClientTest {

  @Mock
  private GenericRestService genericRestService;

  @InjectMocks
  private GeneratorPdfClient generatorPdfClient;

  private Map<String, Object> testParameters;

  @BeforeEach
  void setUp() {
    testParameters = Map.of(
        "nombre_denominacion", "JUAN GARCÍA",
        "nif", "12345678A"
    );
  }

  @Test
  @DisplayName("Should generate PDF successfully")
  void shouldGeneratePdfSuccessfully() {
    // Given
    String expectedBase64 = "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC9D";
    GenerationPdfResponse pdfResponse = GenerationPdfResponse.builder()
        .success(true)
        .message("PDF generado exitosamente")
        .pdfBase64(expectedBase64)
        .build();

    RestResponse<GenerationPdfResponse> restResponse = RestResponse.<GenerationPdfResponse>builder()
        .success(true)
        .data(pdfResponse)
        .build();

    when(genericRestService.invoke(
        eq("pdf-generator"),
        anyString(),
        any(),
        anyMap(),
        anyMap(),
        anyMap(),
        eq(GenerationPdfResponse.class)
    )).thenReturn(restResponse);

    // When
    String result = generatorPdfClient.generatePdf(testParameters);

    // Then
    assertNotNull(result);
    assertEquals(expectedBase64, result);
    verify(genericRestService, times(1)).invoke(
        eq("pdf-generator"),
        anyString(),
        any(),
        anyMap(),
        anyMap(),
        anyMap(),
        eq(GenerationPdfResponse.class)
    );
  }

  @Test
  @DisplayName("Should throw exception when service returns unsuccessful response")
  void shouldThrowExceptionWhenServiceReturnsUnsuccessfulResponse() {
    // Given
    RestResponse<GenerationPdfResponse> restResponse = RestResponse.<GenerationPdfResponse>builder()
        .success(false)
        .build();

    when(genericRestService.invoke(
        eq("pdf-generator"),
        anyString(),
        any(),
        anyMap(),
        anyMap(),
        anyMap(),
        eq(GenerationPdfResponse.class)
    )).thenReturn(restResponse);

    // When & Then
    ExternalServiceException exception = assertThrows(
        ExternalServiceException.class,
        () -> generatorPdfClient.generatePdf(testParameters)
    );

    assertTrue(exception.getMessage().contains("PDF generation failed"));
  }

  @Test
  @DisplayName("Should throw exception when service returns null data")
  void shouldThrowExceptionWhenServiceReturnsNullData() {
    // Given
    RestResponse<GenerationPdfResponse> restResponse = RestResponse.<GenerationPdfResponse>builder()
        .success(true)
        .data(null)
        .build();

    when(genericRestService.invoke(
        eq("pdf-generator"),
        anyString(),
        any(),
        anyMap(),
        anyMap(),
        anyMap(),
        eq(GenerationPdfResponse.class)
    )).thenReturn(restResponse);

    // When & Then
    ExternalServiceException exception = assertThrows(
        ExternalServiceException.class,
        () -> generatorPdfClient.generatePdf(testParameters)
    );

    assertTrue(exception.getMessage().contains("PDF generation failed"));
  }

  @ParameterizedTest(name = "Should throw exception when {0}")
  @MethodSource("invalidPdfResponseProvider")
  @DisplayName("Should throw exception for invalid PDF responses")
  void shouldThrowExceptionForInvalidPdfResponse(Boolean success,
      String pdfBase64) {
    // Given
    GenerationPdfResponse pdfResponse = GenerationPdfResponse.builder()
        .success(success)
        .message("PDF response")
        .pdfBase64(pdfBase64)
        .build();

    RestResponse<GenerationPdfResponse> restResponse = RestResponse.<GenerationPdfResponse>builder()
        .success(true)
        .data(pdfResponse)
        .build();

    when(genericRestService.invoke(
        eq("pdf-generator"),
        anyString(),
        any(),
        anyMap(),
        anyMap(),
        anyMap(),
        eq(GenerationPdfResponse.class)
    )).thenReturn(restResponse);

    // When & Then
    ExternalServiceException exception = assertThrows(
        ExternalServiceException.class,
        () -> generatorPdfClient.generatePdf(testParameters)
    );

    assertTrue(exception.getMessage().contains("PDF generation failed"));
  }

  @Test
  @DisplayName("Should throw exception when generic service throws exception")
  void shouldThrowExceptionWhenGenericServiceThrowsException() {
    // Given
    when(genericRestService.invoke(
        eq("pdf-generator"),
        anyString(),
        any(),
        anyMap(),
        anyMap(),
        anyMap(),
        eq(GenerationPdfResponse.class)
    )).thenThrow(new RuntimeException("Connection timeout"));

    // When & Then
    ExternalServiceException exception = assertThrows(
        ExternalServiceException.class,
        () -> generatorPdfClient.generatePdf(testParameters)
    );

    assertTrue(exception.getMessage().contains("PDF generation error"));
    assertNotNull(exception.getCause());
  }

  private static Stream<Arguments> invalidPdfResponseProvider() {
    return Stream.of(
        Arguments.of(false, null),
        Arguments.of(true, null),
        Arguments.of(true, "   ")
    );
  }
}
