@convenience @get-answers @validation
Feature: Get Convenience Answers - Validation Errors

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @validation
  Scenario: Should return 400 when test ID is zero
    Given path 'test-mifid', 'convenience-responses', 0
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'Test ID'

  @validation
  Scenario: Should return 400 when test ID is negative
    Given path 'test-mifid', 'convenience-responses', -5
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'Test ID'
