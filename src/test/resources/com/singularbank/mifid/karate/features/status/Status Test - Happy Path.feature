@test-status @happy-path
Feature: Update MiFID Test Status - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Update status to SIGNED
    # First create a test to get a valid ID
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def convQ1 = response.questions[0]

    * def saveRequest =
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
          }
        ]
      }
      """
    * set saveRequest.tests[0].questionResponses[0].questionId = convQ1.id
    * set saveRequest.tests[0].questionResponses[0].selectedOptionId = convQ1.options[0].id

    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request saveRequest
    When method POST
    Then status 201
    * def testId = response.responseClientId

    # Now update the status
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'SIGNED' }
    When method PUT
    Then status 200
    And match response.testId == testId
    And match response.status == 'SIGNED'
    And match response.signatureDate == '#notnull'

  Scenario: Update status to CANCELLED
    # Create a test first
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def convQ1 = response.questions[0]

    * def saveRequest =
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
          }
        ]
      }
      """
    * set saveRequest.tests[0].questionResponses[0].questionId = convQ1.id
    * set saveRequest.tests[0].questionResponses[0].selectedOptionId = convQ1.options[0].id

    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request saveRequest
    When method POST
    Then status 201
    * def testId = response.responseClientId

    # Update to CANCELLED
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'CANCELLED' }
    When method PUT
    Then status 200
    And match response.testId == testId
    And match response.status == 'CANCELLED'
    And match response.cancellationDate == '#notnull'

  Scenario: Update status to PENDING
    # Create a test first
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def convQ1 = response.questions[0]

    * def saveRequest =
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
          }
        ]
      }
      """
    * set saveRequest.tests[0].questionResponses[0].questionId = convQ1.id
    * set saveRequest.tests[0].questionResponses[0].selectedOptionId = convQ1.options[0].id

    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request saveRequest
    When method POST
    Then status 201
    * def testId = response.responseClientId

    # Update to PENDING
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'PENDING' }
    When method PUT
    Then status 200
    And match response.testId == testId
    And match response.status == 'PENDING'

  Scenario: Idempotent - Update to same status returns success
    # Create and sign a test
    Given path 'test-mifid', 'convenience', 'ONBOARDING'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    * def convQ1 = response.questions[0]

    * def saveRequest =
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
          }
        ]
      }
      """
    * set saveRequest.tests[0].questionResponses[0].questionId = convQ1.id
    * set saveRequest.tests[0].questionResponses[0].selectedOptionId = convQ1.options[0].id

    Given path 'test-mifid', 'answers', testData.documentNumber
    And header X-Correlation-ID = correlationId
    And request saveRequest
    When method POST
    Then status 201
    * def testId = response.responseClientId

    # First update to SIGNED
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'SIGNED' }
    When method PUT
    Then status 200

    # Second update to SIGNED (idempotent)
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'SIGNED' }
    When method PUT
    Then status 200
    And match response.status == 'SIGNED'