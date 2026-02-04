package com.singularbank.mifid.controller.download;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.singularbank.mifid.config.AbstractIntegrationTest;
import com.singularbank.mifid.entity.FileDownload;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.service.pdf.GeneratorPdfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser
class GeneratorPdfControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/test-mifid/pdf";
  private static final String VALID_DNI = "12345678A";
  private static final byte[] MOCK_PDF_CONTENT = "Mock PDF Content".getBytes();

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GeneratorPdfService generatorPdfService;

  @Test
  void shouldDownloadPdfSuccessfully() throws Exception {
    // Given
    FileDownload fileDownload = FileDownload.builder()
        .content(MOCK_PDF_CONTENT)
        .build();
    when(generatorPdfService.generate(VALID_DNI)).thenReturn(fileDownload);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DNI))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(header().exists("Content-Disposition"))
        .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
        .andExpect(header().string("Pragma", "no-cache"))
        .andExpect(header().exists("Expires"))
        .andExpect(header().exists("Content-Length"));
  }

  @Test
  void shouldReturnBadRequestForInvalidDocumentNumber() throws Exception {
    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", "INVALID"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnNotFoundWhenTestDoesNotExist() throws Exception {
    // Given
    when(generatorPdfService.generate("99999999Z"))
        .thenThrow(new ResourceNotFoundException("Test not found"));

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", "99999999Z"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldSetCorrectContentDispositionHeader() throws Exception {
    // Given
    FileDownload fileDownload = FileDownload.builder()
        .content(MOCK_PDF_CONTENT)
        .build();
    when(generatorPdfService.generate(VALID_DNI)).thenReturn(fileDownload);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DNI))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/pdf"));
  }

  @Test
  void shouldReturnCorrectContentLength() throws Exception {
    // Given
    FileDownload fileDownload = FileDownload.builder()
        .content(MOCK_PDF_CONTENT)
        .build();
    when(generatorPdfService.generate(VALID_DNI)).thenReturn(fileDownload);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{document-number}", VALID_DNI))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Length", String.valueOf(MOCK_PDF_CONTENT.length)));
  }
}
