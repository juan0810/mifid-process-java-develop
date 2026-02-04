package com.singularbank.mifid.client.pdf;

import com.singularbank.lib.rest.adapter.model.RestResponse;
import com.singularbank.lib.rest.adapter.service.GenericRestService;
import com.singularbank.mifid.client.pdf.config.PdfGeneratorOperation;
import com.singularbank.mifid.client.pdf.response.GenerationPdfResponse;
import com.singularbank.mifid.client.pdf.response.PdfResult;
import com.singularbank.mifid.exception.ExternalServiceException;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratorPdfClient {

  private final GenericRestService genericRestService;
  private static final String SERVICE_NAME = "pdf-generator";

  public <T> PdfResult<T> executePost(PdfGeneratorOperation operation, Object requestBody,
      Class<T> responseType) {

    log.debug("Executing POST operation: {}", operation.operationName());

    try {
      RestResponse<T> response = genericRestService.invoke(SERVICE_NAME, operation.endpoint(),
          requestBody, operation.pathParams(), Collections.emptyMap(), Collections.emptyMap(),
          responseType);

      return processResponse(response, operation.operationName());

    } catch (Exception ex) {
      log.error("Error executing POST operation {}: {}", operation.operationName(), ex.getMessage(),
          ex);
      return new PdfResult.Error<>("Failed to execute " + operation.operationName(), ex);
    }
  }

  public String generatePdf(Map<String, Object> parameters) {
    log.debug("Generating PDF with parameters");

    PdfResult<GenerationPdfResponse> result = executePost(
        PdfGeneratorOperation.post("generate-pdf", "GeneratePDF"),
        createPdfRequest(parameters),
        GenerationPdfResponse.class
    );

    return switch (result) {
      case PdfResult.Success<GenerationPdfResponse>(var data) -> data.getPdfBase64();
      case PdfResult.Empty<GenerationPdfResponse>(var reason) ->
          throw new ExternalServiceException("PDF generation failed: " + reason);
      case PdfResult.Error<GenerationPdfResponse>(var message, var cause) ->
          throw new ExternalServiceException("PDF generation error: " + message, cause);
    };
  }

  private Map<String, Object> createPdfRequest(Map<String, Object> parameters) {
    return Map.of(
        "templateName", "mifid-convenience/MIFID",
        "parameters", Map.of("mifid", parameters)
    );
  }

  private <T> PdfResult<T> processResponse(RestResponse<T> response, String operationName) {
    if (!response.isSuccess()) {
      String reason = "PDF generator service failed for operation: " + operationName;
      log.warn(reason);
      return new PdfResult.Empty<>(reason);
    }

    if (response.getData() == null) {
      String reason = "PDF generator service returned no data for operation: " + operationName;
      log.warn(reason);
      return new PdfResult.Empty<>(reason);
    }

    if (response.getData() instanceof GenerationPdfResponse pdfResponse) {
      if (!Boolean.TRUE.equals(pdfResponse.getSuccess())) {
        String reason = "PDF generation failed: " + pdfResponse.getMessage();
        log.error(reason);
        return new PdfResult.Empty<>(reason);
      }

      if (pdfResponse.getPdfBase64() == null || pdfResponse.getPdfBase64().isBlank()) {
        String reason = "PDF generation returned empty base64 content";
        log.error(reason);
        return new PdfResult.Empty<>(reason);
      }
    }

    log.info("Operation {} completed successfully", operationName);
    return new PdfResult.Success<>(response.getData());
  }
}
