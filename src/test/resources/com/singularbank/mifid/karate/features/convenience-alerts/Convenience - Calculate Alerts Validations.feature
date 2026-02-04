@convenience-alerts @validations
Feature: Calculate Convenience Test Alerts - Validations

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()
    # Get convenience test to obtain valid question and option IDs
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def validQuestionId = response.questions[0].id
    * def validOptionId = response.questions[0].options[0].id

  Scenario: Reject request with missing service
    * def requestBody = { version: 1, questionResponses: [ { questionId: '#(validQuestionId)', selectedOptionId: '#(validOptionId)' } ] }
    Given path 'test-mifid', 'convenience', 'alerts', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject request with missing version
    * def requestBody = { service: 'ONBOARDING', questionResponses: [ { questionId: '#(validQuestionId)', selectedOptionId: '#(validOptionId)' } ] }
    Given path 'test-mifid', 'convenience', 'alerts', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject request with empty questionResponses
    * def requestBody = { service: 'ONBOARDING', version: 1, questionResponses: [] }
    Given path 'test-mifid', 'convenience', 'alerts', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject request with invalid document number format
    * def requestBody = { service: 'ONBOARDING', version: 1, questionResponses: [ { questionId: '#(validQuestionId)', selectedOptionId: '#(validOptionId)' } ] }
    Given path 'test-mifid', 'convenience', 'alerts', '123'
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Customer not found returns empty result
    * def requestBody = { service: 'ONBOARDING', version: 1, questionResponses: [ { questionId: '#(validQuestionId)', selectedOptionId: '#(validOptionId)' } ] }
    Given path 'test-mifid', 'convenience', 'alerts', '99999999Z'
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 200
    And match response.result == '#string'
