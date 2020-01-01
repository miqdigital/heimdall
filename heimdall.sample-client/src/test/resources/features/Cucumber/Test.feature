@sanity
Feature: Test Client

  Scenario Outline: GET Call
    Given User hits <url>
    Then User checks if all components loaded
    Examples:
      | url                          |
      | http://newtours.demoaut.com/ |

  Scenario Outline: POST Call
    Given User checks if all components loaded
    Then User logs in with <username> and <password>
    Then User clicks submit
    And User asserts home page loads
    Examples:
      | username | password |
      | dummy    | abc@123  |