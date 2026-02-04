@generator-pdf-download @errors
Feature: Generator PDF - External Service Errors

  Background:
    * url baseUrl
    * def WireMockHelper = Java.type('com.singularbank.mifid.karate.helpers.WireMockHelper')

  @pdf-external-error
  Scenario: Generate PDF - External service unavailable
    * WireMockHelper.stubPdfError(503)
    Given path 'test-mifid/pdf', testData.documentNumber
    When method GET
    Then status 502

  @pdf-external-failure
  Scenario: Generate PDF - External service returns failure
    * WireMockHelper.stubPdfFailure('PDF generation failed')
    Given path 'test-mifid/pdf', testData.documentNumber
    When method GET
    Then status 502