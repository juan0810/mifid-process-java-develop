@suitability @get-test @happy-path
Feature: Get Suitability Test Questions - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Get suitability questions for ONBOARDING application
    * def application = 'ONBOARDING'
    Given path 'test-mifid', 'suitability', application
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.version != null
    And match response.version.id == '#number'
    And match response.questions != null
    And match response.questions == '#array'
    And match response.questions[0].id == '#number'
    And match response.questions[0].text == '#string'
    And match response.questions[0].options == '#array'
