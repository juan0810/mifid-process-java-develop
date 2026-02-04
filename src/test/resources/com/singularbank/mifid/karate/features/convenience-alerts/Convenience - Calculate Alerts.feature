@convenience-alerts @happy-path
Feature: Calculate Convenience Test Alerts - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Calculate alerts with valid answers
    # Get convenience questions
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def q1 = response.questions[0]
    
    # Build request
    * def requestBody =
    """
    {
      service: 'ONBOARDING',
      version: 1,
      questionResponses: [
        { questionId: 0, selectedOptionId: 0 }
      ]
    }
    """
    * set requestBody.questionResponses[0].questionId = q1.id
    * set requestBody.questionResponses[0].selectedOptionId = q1.options[0].id
    
    # Calculate alerts
    Given path 'test-mifid', 'convenience', 'alerts', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request requestBody
    When method POST
    Then status 200
    And match response.result == '#string'
    And match response.alerts == '#array'
