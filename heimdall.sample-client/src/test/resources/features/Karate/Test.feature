@regression
Feature: Test Client

  Background:
    * header Content-Type = 'application/json'

  @jira_prefix-124
  Scenario: Making Get call
    Given url 'http://dummy.restapiexample.com/api/v1/employees'
    When method Get
    Then status 200
    * print response

  @jira_prefix-123
  Scenario: Making Post call
    Given url 'http://dummy.restapiexample.com/api/v1/create'
    And request {"name":"test09","salary":"123","age":"25"}
    When method Post
    Then status 200
    * print response
  
