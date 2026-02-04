@convenience @get-questions @happy-path
Feature: Get Convenience Test Questions - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Get convenience questions for ONBOARDING service
    * def serviceName = 'ONBOARDING'
    Given path 'test-mifid', 'convenience', serviceName
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.version != null
    And match response.questions != null
    And match response.questions == '#array'
    And match response.questions[0].id == '#number'
    And match response.questions[0].text == '#string'
