package com.singularbank.mifid.controller.download;

import com.singularbank.mifid.annotation.validation.EnsureIdentificationClient;
import com.singularbank.mifid.annotation.validation.EnsureTestId;
import com.singularbank.mifid.controller.helpers.dto.ErrorResponse;
import com.singularbank.mifid.entity.FileDownload;
import com.singularbank.mifid.service.pdf.GeneratorPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test-mifid")
@Validated
@Slf4j
public class GeneratorPdfController {

  private final GeneratorPdfService generatorPdfService;

  @Tag(name = "Test PDF Download", description = "Operations to download PDF reports of completed MIFID tests")
  @Operation(
      summary = "Download completed test PDF",
      description = "Generates and downloads a PDF report with the answers of the completed test",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "PDF generated successfully",
              content = @Content(
                  mediaType = "application/pdf",
                  schema = @Schema(type = "string", format = "binary")
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request (validation errors)",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Test not found",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          )
      }
  )
  @GetMapping(value = "/pdf/{document-number}", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<Resource> downloadTestPdf(
      @Parameter(
          description = "Client identity document number",
          required = true,
          example = "12345678A"
      )
      @PathVariable("document-number") @EnsureIdentificationClient String documentNumber
  ) {
    log.info("Generating MIFID test PDF");

    FileDownload fileDownload = generatorPdfService.generate(documentNumber);

    log.info("MIFID test PDF generated successfully");

    ByteArrayResource resource = new ByteArrayResource(fileDownload.getContent());

    HttpHeaders headers = buildPdfHeaders();

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(fileDownload.getContent().length)
        .body(resource);
  }


    @Tag(name = "Test PDF Download", description = "Operations to download PDF reports of completed MIFID tests")
    @Operation(
            summary = "Download completed test PDF",
            description = "Generates and downloads a PDF report with the answers of the completed test",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PDF generated successfully",
                            content = @Content(
                                    mediaType = "application/pdf",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request (validation errors)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Test not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/pdf/{testId}/test", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadTestPdf(
            @PathVariable @Parameter(
                    description = "Test Id",
                    required = true,
                    example = "280"
            )
            @EnsureTestId Integer testId
    ) {
        log.info("Generating MIFID test PDF");

        FileDownload fileDownload = generatorPdfService.generateByID(testId);

        log.info("MIFID test PDF generated successfully");

        ByteArrayResource resource = new ByteArrayResource(fileDownload.getContent());

        HttpHeaders headers = buildPdfHeaders();

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileDownload.getContent().length)
                .body(resource);
    }
  private HttpHeaders buildPdfHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String filename = String.format("test_mifid_%s.pdf", timestamp);

    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("no-cache, no-store, must-revalidate");
    headers.setPragma("no-cache");
    headers.setExpires(0);

    return headers;
  }
}