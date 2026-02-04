@convenience @get-questions @validation
Feature: Get Convenience Questions - Validation Errors

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @validation
  Scenario: Should return 400 when version is zero
    * def serviceName = 'ONBOARDING'
    Given path 'test-mifid', 'convenience', serviceName
    And param version = 0
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'version'

  @validation
  Scenario: Should return 400 when version is negative
    * def serviceName = 'ONBOARDING'
    Given path 'test-mifid', 'convenience', serviceName
    And param version = -5
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
    And match response.message contains 'version'

  @validation
  Scenario: Should return 400 when service name is invalid
    * def serviceName = 'INVALID_SERVICE'
    Given path 'test-mifid', 'convenience', serviceName
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400
