@test-history @validations
Feature: Get MiFID Test History - Validations

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  Scenario: Reject request with invalid document number (too short)
    Given path 'test-mifid', '1234567', 'tests'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400

  Scenario: Reject request with invalid document number (too long)
    Given path 'test-mifid', '1234567890', 'tests'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400

  Scenario: Reject request with invalid type filter
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param type = 'INVALID_TYPE'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400

  Scenario: Reject request with invalid state filter
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param state = 'INVALID_STATE'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400

  Scenario: Reject request with invalid date format
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param from = '01-01-2025'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400