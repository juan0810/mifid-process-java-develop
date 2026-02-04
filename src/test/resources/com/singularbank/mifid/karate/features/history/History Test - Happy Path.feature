@test-history @happy-path
Feature: Get MiFID Test History - Happy Path

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Get test history without filters
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.totalElements == '#number'
    And match response.totalPages == '#number'
    And match response.currentPage == 0
    And match response.pagination == 20
    And match response.tests == '#array'

  Scenario: Get test history with type filter
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param type = 'SUSTAINABILITY'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.tests == '#array'
    And match each response.tests[*].type == 'SUSTAINABILITY'

  Scenario: Get test history with state filter
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param state = 'SIGNED'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.tests == '#array'
    And match each response.tests[*].state == 'SIGNED'

  Scenario: Get test history with date range filter
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param from = '2025-01-01'
    And param to = '2025-12-31'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.tests == '#array'

  Scenario: Get test history with pagination
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param page = 0
    And param size = 2
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.currentPage == 0
    And match response.pagination == 2
    And match response.tests == '#[_ <= 2]'

  Scenario: Get test history with all filters combined
    Given path 'test-mifid', testData.documentNumber, 'tests'
    And param type = 'SUSTAINABILITY'
    And param state = 'SIGNED'
    And param from = '2025-01-01'
    And param to = '2025-12-31'
    And param page = 0
    And param size = 10
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.tests == '#array'

  Scenario: Get empty history for customer without tests
    Given path 'test-mifid', '99999999Z', 'tests'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.totalElements == 0
    And match response.tests == '#[0]'