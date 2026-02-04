package com.singularbank.mifid.repository.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.client.pdf.GeneratorPdfClient;
import com.singularbank.mifid.exception.ExternalServiceException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratorPdfRepository Tests")
class GeneratorPdfRepositoryImplTest {

  @Mock
  private GeneratorPdfClient pdfGeneratorClient;

  @InjectMocks
  private GeneratorPdfRepositoryImpl generatorPdfRepository;

  @Test
  @DisplayName("Should generate PDF base64 successfully")
  void shouldGeneratePdfBase64Successfully() {
    // Given
    Map<String, Object> parameters = Map.of(
        "nif", "12345678A",
        "nombre_denominacion", "Juan García",
        "perfil_obtenido", "ARRIESGADO"
    );
    
    String expectedBase64 = "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC9UeXBlIC9DYXRhbG9nCi9QYWdlcyAyIDAgUgo+PgplbmRvYmoK";

    when(pdfGeneratorClient.generatePdf(parameters))
        .thenReturn(expectedBase64);

    // When
    String result = generatorPdfRepository.generatePdfBase64(parameters);

    // Then
    assertThat(result)
        .isNotNull()
        .isEqualTo(expectedBase64);

    verify(pdfGeneratorClient).generatePdf(parameters);
  }

  @Test
  @DisplayName("Should propagate exception when client fails")
  void shouldPropagateExceptionWhenClientFails() {
    // Given
    Map<String, Object> parameters = Map.of(
        "nif", "12345678A",
        "nombre_denominacion", "Juan García"
    );

    when(pdfGeneratorClient.generatePdf(any()))
        .thenThrow(new ExternalServiceException("PDF generation failed"));

    // When & Then
    assertThatThrownBy(() -> generatorPdfRepository.generatePdfBase64(parameters))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("PDF generation failed");

    verify(pdfGeneratorClient).generatePdf(parameters);
  }

  @Test
  @DisplayName("Should handle empty parameters")
  void shouldHandleEmptyParameters() {
    // Given
    Map<String, Object> emptyParameters = Map.of();
    String expectedBase64 = "emptyPdfBase64";

    when(pdfGeneratorClient.generatePdf(emptyParameters))
        .thenReturn(expectedBase64);

    // When
    String result = generatorPdfRepository.generatePdfBase64(emptyParameters);

    // Then
    assertThat(result)
        .isNotNull()
        .isEqualTo(expectedBase64);

    verify(pdfGeneratorClient).generatePdf(emptyParameters);
  }
}