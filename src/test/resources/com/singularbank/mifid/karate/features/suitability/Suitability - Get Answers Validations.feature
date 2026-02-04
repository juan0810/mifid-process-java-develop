@suitability @get-answers @validation  
Feature: Get Suitability Answers - Validation Errors

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @validation
  Scenario: Should return 400 when test ID is zero
    Given path 'test-mifid', 'suitability-responses', 0
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'Test ID'

  @validation
  Scenario: Should return 400 when test ID is negative
    Given path 'test-mifid', 'suitability-responses', -1
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'Test ID'
