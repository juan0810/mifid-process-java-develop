@answers @save-answers @validations
Feature: Save MiFID Test Answers - Validations

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  Scenario: Reject request with missing service
    * def requestBody = { version: 1, tests: [ { type: 'SUITABILITY', questionResponses: [ { questionId: 11, selectedOptionId: 49 } ] } ] }
    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject request with missing version
    * def requestBody = { service: 'ONBOARDING', tests: [ { type: 'SUITABILITY', questionResponses: [ { questionId: 11, selectedOptionId: 49 } ] } ] }
    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject request with empty tests array
    * def requestBody = { service: 'ONBOARDING', version: 1, tests: [] }
    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject request with invalid document number format
    * def requestBody = { service: 'ONBOARDING', version: 1, tests: [ { type: 'SUITABILITY', questionResponses: [ { questionId: 11, selectedOptionId: 49 } ] } ] }
    Given path 'test-mifid', 'answers', '123'
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400

  Scenario: Reject Suitability-only when no previous Convenience exists
    Given path 'test-mifid', 'suitability', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def q1 = response.questions[0]
    
    * def requestBody =
    """
    {
      service: 'ONBOARDING',
      version: 1,
      tests: [
        {
          type: 'SUITABILITY',
          questionResponses: [
            { questionId: 0, selectedOptionId: 0 }
          ]
        }
      ]
    }
    """
    * set requestBody.tests[0].questionResponses[0].questionId = q1.id
    * set requestBody.tests[0].questionResponses[0].selectedOptionId = q1.options[0].id
    
    Given path 'test-mifid', 'answers', '99999999X'
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 400
    And match response.message contains 'Convenience test required'
