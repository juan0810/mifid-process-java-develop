@answers @save-answers @happy-path
Feature: Save MiFID Test Answers - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Save Convenience and Suitability together
    Given path 'test-mifid', 'suitability', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def suitQ1 = response.questions[0]
    
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def convQ1 = response.questions[0]
    
    * def requestBody =
    """
    {
      service: 'ONBOARDING',
      version: 1,
      tests: [
        {
          type: 'CONVENIENCE',
          questionResponses: [
            { questionId: 0, selectedOptionId: 0 }
          ]
        },
        {
          type: 'SUITABILITY',
          questionResponses: [
            { questionId: 0, selectedOptionId: 0 }
          ]
        }
      ]
    }
    """
    * set requestBody.tests[0].questionResponses[0].questionId = convQ1.id
    * set requestBody.tests[0].questionResponses[0].selectedOptionId = convQ1.options[0].id
    * set requestBody.tests[1].questionResponses[0].questionId = suitQ1.id
    * set requestBody.tests[1].questionResponses[0].selectedOptionId = suitQ1.options[0].id
    
    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 201
    And match response.responseClientId == '#number'
    And match response.results.suitability.result == '#string'
    And match response.results.convenience.result == '#string'
    And match response.results.sustainability == '#notpresent'

  Scenario: Save all three tests in single call
    Given path 'test-mifid', 'suitability', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def suitQ1 = response.questions[0]
    
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def convQ1 = response.questions[0]
    
    Given path 'test-mifid', 'sustainability', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def sustQ1 = response.questions[0]
    
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
        },
        {
          type: 'CONVENIENCE',
          questionResponses: [
            { questionId: 0, selectedOptionId: 0 }
          ]
        },
        {
          type: 'SUSTAINABILITY',
          questionResponses: [
            { questionId: 0, selectedOptionId: 0 }
          ]
        }
      ]
    }
    """
    * set requestBody.tests[0].questionResponses[0].questionId = suitQ1.id
    * set requestBody.tests[0].questionResponses[0].selectedOptionId = suitQ1.options[0].id
    * set requestBody.tests[1].questionResponses[0].questionId = convQ1.id
    * set requestBody.tests[1].questionResponses[0].selectedOptionId = convQ1.options[0].id
    * set requestBody.tests[2].questionResponses[0].questionId = sustQ1.id
    * set requestBody.tests[2].questionResponses[0].selectedOptionId = sustQ1.options[0].id
    
    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 201
    And match response.responseClientId == '#number'
    And match response.results.suitability.result == '#string'
    And match response.results.convenience.result == '#string'
    And match response.results.sustainability.result == '#string'
