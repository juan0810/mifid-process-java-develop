@sustainability @get-questions @happy-path
Feature: Get Sustainability Test Questions - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()
    * def version = testData.version

  @smoke
  Scenario: Get sustainability questions successfully for ONBOARDING
    * def application = 'ONBOARDING'
    Given path 'test-mifid', 'sustainability', application
    And param version = version
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.version != null
    And match response.version.id == version
    And match response.questions != null
    And match response.questions == '#array'
    And match response.questions[0].id == '#number'
    And match response.questions[0].text == '#string'
    And match response.questions[0].options == '#array'
