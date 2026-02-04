package com.singularbank.mifid.client.pdf.config;

import java.util.Map;

public record PdfGeneratorOperation(
    String endpoint, String operationName, Map<String, String> pathParams) {

  public static PdfGeneratorOperation post(String endpoint, String operationName) {
    return new PdfGeneratorOperation(endpoint, operationName, Map.of());
  }
}
