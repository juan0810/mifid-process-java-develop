package com.singularbank.mifid.service.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singularbank.mifid.entity.FileDownload;
import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.GeneratorPdfRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.pdf.impl.GeneratorPdfServiceImpl;
import com.singularbank.mifid.service.pdf.mapper.MifidConvenienceParametersMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneratorPdfServiceTest {

  @Mock
  private RespuestaClienteRepository respuestaClienteRepository;

  @Mock
  private MifidConvenienceParametersMapper pdfParametersMapper;

  @Mock
  private GeneratorPdfRepository pdfGeneratorRepository;

  @InjectMocks
  private GeneratorPdfServiceImpl generatorPdfService;

  private static final String REFERENCE_ID = "REF-12345";
  private static final Integer TEST_ID = 12345;
  private static final String CLIENT_DNI = "12345678A";
  private static final Short VERSION_ID = 1;
  private static final String CONVENIENCE_RESULT = "Conveniente para familias: A, B";
  private static final String SUITABILITY_RESULT = "Perfil Conservador";


  @Nested
  class GeneratorPdfByIdentity {
    @Test
    void shouldGeneratePdfSuccessfully() {
      // Given
      RespuestaCliente respuestaCliente = createRespuestaCliente();
      Map<String, Object> mifidParams = createMifidParametersMap();
      String base64Pdf = "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC9UeXBlIC9DYXRhbG9nCi9QYWdlcyAyIDAgUgo+PgplbmRvYmoKMiAwIG9iago8PC9UeXBlIC9QYWdlcwo=";

      when(respuestaClienteRepository.findAnswersByIdentity(REFERENCE_ID))
              .thenReturn(Optional.of(respuestaCliente));
      when(pdfParametersMapper.mapToMifidParameters(respuestaCliente))
              .thenReturn(mifidParams);
      when(pdfGeneratorRepository.generatePdfBase64(mifidParams))
              .thenReturn(base64Pdf);

      // When
      FileDownload result = generatorPdfService.generate(REFERENCE_ID);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent())
              .isNotNull()
              .hasSizeGreaterThan(0);

      verify(respuestaClienteRepository).findAnswersByIdentity(REFERENCE_ID);
      verify(pdfParametersMapper).mapToMifidParameters(respuestaCliente);
      verify(pdfGeneratorRepository).generatePdfBase64(mifidParams);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAnswersNotFound() {
      // Given
      when(respuestaClienteRepository.findAnswersByIdentity(REFERENCE_ID))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> generatorPdfService.generate(REFERENCE_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Customer answers not found")
              .hasMessageContaining(REFERENCE_ID);

      verify(respuestaClienteRepository).findAnswersByIdentity(REFERENCE_ID);
    }
  }


  @Nested
  class GeneratorPdfById {
    @Test
    void shouldGeneratePdfSuccessfully() {
      // Given
      RespuestaCliente respuestaCliente = createRespuestaCliente();
      Map<String, Object> mifidParams = createMifidParametersMap();
      String base64Pdf = "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC9UeXBlIC9DYXRhbG9nCi9QYWdlcyAyIDAgUgo+PgplbmRvYmoKMiAwIG9iago8PC9UeXBlIC9QYWdlcwo=";

      when(respuestaClienteRepository.findAnswersById(TEST_ID))
              .thenReturn(Optional.of(respuestaCliente));
      when(pdfParametersMapper.mapToMifidParameters(respuestaCliente))
              .thenReturn(mifidParams);
      when(pdfGeneratorRepository.generatePdfBase64(mifidParams))
              .thenReturn(base64Pdf);

      // When
      FileDownload result = generatorPdfService.generateByID(TEST_ID);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent())
              .isNotNull()
              .hasSizeGreaterThan(0);

      verify(respuestaClienteRepository).findAnswersById(TEST_ID);
      verify(pdfParametersMapper).mapToMifidParameters(respuestaCliente);
      verify(pdfGeneratorRepository).generatePdfBase64(mifidParams);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAnswersNotFound() {
      // Given
      when(respuestaClienteRepository.findAnswersById(TEST_ID))
              .thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> generatorPdfService.generateByID(TEST_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessageContaining("Customer answers not found")
              .hasMessageContaining(TEST_ID.toString());

      verify(respuestaClienteRepository).findAnswersById(TEST_ID);
    }
  }


  private RespuestaCliente createRespuestaCliente() {
    return RespuestaCliente.builder()
        .id(TEST_ID)
        .clienteDni(CLIENT_DNI)
        .versionId(VERSION_ID)
        .estado(StateTest.SIGNED)
        .resultadoConveniencia(CONVENIENCE_RESULT)
        .resultadoIdoneidad(SUITABILITY_RESULT)
        .assessmentReference(REFERENCE_ID)
        .fechaAlta(LocalDateTime.of(2025, 10, 20, 16, 30, 0))
        .build();
  }

  private Map<String, Object> createMifidParametersMap() {
    return Map.of(
        "nif", CLIENT_DNI,
        "nombre_denominacion", "Juan García",
        "perfil_obtenido", "ARRIESGADO"
    );
  }
}