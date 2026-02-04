@generator-pdf-download @validations
Feature: Generator PDF - Validations

  Background:
    * url baseUrl
    * def WireMockHelper = Java.type('com.singularbank.mifid.karate.helpers.WireMockHelper')

  @pdf-not-found
  Scenario: Generate PDF - Document not found
    * WireMockHelper.stubPdfSuccess()
    Given path 'test-mifid/pdf', 'NOTFOUND9'
    When method GET
    Then status 404

  @pdf-invalid-document
  Scenario: Generate PDF - Invalid document number format
    Given path 'test-mifid/pdf', 'ABC'
    When method GET
    Then status 400
    And match response.message contains 'exactly 9 characters'