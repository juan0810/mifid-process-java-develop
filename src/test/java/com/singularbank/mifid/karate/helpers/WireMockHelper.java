package com.singularbank.mifid.karate.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class WireMockHelper {

  public static void stubPdfSuccess() {
    stubPdfSuccess("JVBERi0xLjQKJeLjz9MK");
  }

  public static void stubPdfSuccess(String pdfBase64Content) {
    resetAllRequests();
    stubFor(post(urlEqualTo("/generate-pdf"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "success": true,
                  "message": "PDF generated successfully",
                  "pdfBase64": "%s"
                }
                """.formatted(pdfBase64Content))));
  }

  public static void stubPdfFailure(String errorMessage) {
    resetAllRequests();
    stubFor(post(urlEqualTo("/generate-pdf"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "success": false,
                  "message": "%s",
                  "pdfBase64": null
                }
                """.formatted(errorMessage))));
  }

  public static void stubPdfError(int status) {
    resetAllRequests();
    stubFor(post(urlEqualTo("/generate-pdf"))
        .willReturn(aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\": \"Service unavailable\"}")));
  }

  private static void resetAllRequests() {
    removeAllMappings();
  }
}
