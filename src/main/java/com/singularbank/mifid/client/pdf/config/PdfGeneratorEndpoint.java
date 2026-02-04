package com.singularbank.mifid.client.pdf.config;

public enum PdfGeneratorEndpoint {
  GENERATE_PDF("generate-pdf", "generate PDF");

  private final String endpoint;
  private final String displayName;

  PdfGeneratorEndpoint(String endpoint, String displayName) {
    this.endpoint = endpoint;
    this.displayName = displayName;
  }

  public String endpoint() {
    return endpoint;
  }

  public String displayName() {
    return displayName;
  }
}
