@suitability @get-test @validation
Feature: Get Suitability Test Questions - Validation Errors

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @validation
  Scenario: Should return 400 when version is zero
    * def application = 'ONBOARDING'
    Given path 'test-mifid', 'suitability', application
    And param version = 0
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'version'

  @validation
  Scenario: Should return 400 when version is negative
    * def application = 'ONBOARDING'
    Given path 'test-mifid', 'suitability', application
    And param version = -1
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'version'

  @validation
  Scenario: Should return 400 when application is invalid
    * def application = 'INVALID_APP'
    Given path 'test-mifid', 'suitability', application
    And param version = 1
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
