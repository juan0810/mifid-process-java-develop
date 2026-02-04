@customer @current-tests @happy-path
Feature: Get Current Customer Tests - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Get active tests for existing customer
    Given path 'test-mifid', 'current-customer-tests', testData.documentNumber
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.customerIdentity == testData.documentNumber
    And match response.activeTests == '#array'
    And match response.activeTests[0].testId == '#number'
    And match response.activeTests[0].serviceName == '#string'
    And match response.activeTests[0].testType == '#string'
    And match response.activeTests[0].testResult == '#string'
    And match response.activeTests[0].creationDate == '#string'
    And match response.activeTests[0].signatureDate == '#string'
    And match response.activeTests[0].expirationDate == '#string'
