@customer @current-tests @validations
Feature: Get Current Customer Tests - Validations

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  Scenario: Reject invalid document number format
    Given path 'test-mifid', 'current-customer-tests', '123'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 400

  Scenario: Customer not found returns empty tests
    Given path 'test-mifid', 'current-customer-tests', '99999999Z'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.customerIdentity == '99999999Z'
    And match response.activeTests == '#array'
