@test-status @validations
Feature: Update MiFID Test Status - Validations

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  Scenario: Reject request with invalid status value
    Given path 'test-mifid', 'responses', 1, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'INVALID_STATUS' }
    When method PUT
    Then status 400
    And match response.message contains 'Valid values'

  Scenario: Reject request with EXPIRED status
    Given path 'test-mifid', 'responses', 1, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'EXPIRED' }
    When method PUT
    Then status 400
    And match response.message contains 'Valid values'

  Scenario: Reject request with empty body
    Given path 'test-mifid', 'responses', 1, 'status'
    And header X-Correlation-ID = correlationId
    And request {}
    When method PUT
    Then status 400

  Scenario: Reject request with null status
    Given path 'test-mifid', 'responses', 1, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: null }
    When method PUT
    Then status 400

  Scenario: Return 404 for non-existing test
    Given path 'test-mifid', 'responses', 99999999, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'SIGNED' }
    When method PUT
    Then status 404
    And match response.message contains 'not found'

  Scenario: Reject invalid transition from SIGNED to DRAFT
    # First create and sign a test
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

    # Sign the test
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'SIGNED' }
    When method PUT
    Then status 200

    # Try to transition to DRAFT (should fail)
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'DRAFT' }
    When method PUT
    Then status 400
    And match response.message contains 'final state'

  Scenario: Reject invalid transition from PENDING to DRAFT
    # Create a test
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

    # Set to PENDING
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'PENDING' }
    When method PUT
    Then status 200

    # Try to transition to DRAFT (should fail)
    Given path 'test-mifid', 'responses', testId, 'status'
    And header X-Correlation-ID = correlationId
    And request { status: 'DRAFT' }
    When method PUT
    Then status 400
    And match response.message contains 'Invalid transition'