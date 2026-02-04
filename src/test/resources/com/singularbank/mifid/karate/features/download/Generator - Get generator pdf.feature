@generator-pdf-download
Feature: Generator PDF - Get Generator PDF

  Background:
    * url baseUrl
    * def WireMockHelper = Java.type('com.singularbank.mifid.karate.helpers.WireMockHelper')

  @pdf-success
  Scenario: Generate PDF successfully
    * WireMockHelper.stubPdfSuccess()
    Given path 'test-mifid/pdf', testData.documentNumber
    When method GET
    Then status 200
    And match responseHeaders['Content-Type'][0] contains 'application/pdf'