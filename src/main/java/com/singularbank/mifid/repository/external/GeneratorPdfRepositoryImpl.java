package com.singularbank.mifid.repository.external;

import com.singularbank.mifid.client.pdf.GeneratorPdfClient;
import com.singularbank.mifid.repository.GeneratorPdfRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GeneratorPdfRepositoryImpl implements GeneratorPdfRepository {

  private final GeneratorPdfClient pdfGeneratorClient;

  @Override
  public String generatePdfBase64(Map<String, Object> parameters) {
    log.debug("Generating PDF via external service");
    return pdfGeneratorClient.generatePdf(parameters);
  }
}