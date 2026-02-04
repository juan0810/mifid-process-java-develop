package com.singularbank.mifid.service.pdf.impl;

import com.singularbank.mifid.entity.FileDownload;
import com.singularbank.mifid.entity.RespuestaCliente;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.GeneratorPdfRepository;
import com.singularbank.mifid.repository.RespuestaClienteRepository;
import com.singularbank.mifid.service.pdf.GeneratorPdfService;
import com.singularbank.mifid.service.pdf.mapper.MifidConvenienceParametersMapper;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratorPdfServiceImpl implements GeneratorPdfService {

  private final RespuestaClienteRepository respuestaClienteRepository;
  private final GeneratorPdfRepository generatorPdfRepository;
  private final MifidConvenienceParametersMapper pdfParametersMapper;

  private static final String PDF_TEMPLATE_NAME = "mifid-convenience/MIFID";

  @Override
  public FileDownload generate(String documentNumber) {
    log.debug("Generating PDF for documentNumber: {}", documentNumber);
    Objects.requireNonNull(documentNumber, "documentNumber is required");

    RespuestaCliente respuestaCliente = respuestaClienteRepository.findAnswersByIdentity(
            documentNumber)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Customer answers not found: " + documentNumber));

    log.info("Calling PDF generator service for documentNumber: {}", documentNumber);
    byte[] pdfContent = generatePdfFromService(respuestaCliente);

    return FileDownload.builder()
        .content(pdfContent)
        .build();
  }

  @Override
  public FileDownload generateByID(Integer testId) {
    log.debug("Generating PDF for test ID: {}", testId);
    Objects.requireNonNull(testId, "testId is required");

    RespuestaCliente respuestaCliente = respuestaClienteRepository.findAnswersById(testId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Customer answers not found: " + testId));

    log.info("Calling PDF generator service for test ID: {}", testId);
    byte[] pdfContent = generatePdfFromService(respuestaCliente);

    return FileDownload.builder()
            .content(pdfContent)
            .build();
  }




  private byte[] generatePdfFromService(RespuestaCliente respuestaCliente) {
    Map<String, Object> mifidParams = pdfParametersMapper.mapToMifidParameters(respuestaCliente);

    log.info("Calling PDF generator repository");
    String base64Pdf = generatorPdfRepository.generatePdfBase64(mifidParams);

    log.info("PDF generated successfully");
    return Base64.getDecoder().decode(base64Pdf);
  }
}
