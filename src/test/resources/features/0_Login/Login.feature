@login @ui
Feature: Login
  As a Swag Labs customer
  I want to sign in with my credentials
  So that I can shop for products

  Background:
    Given User is on the login page

  @smoke @login001
  Scenario: Standard user logs in successfully
    When User logs in as "standard"
    Then User lands on the "Dashboard" page

  @regression @login002
  Scenario Outline: Login is rejected for invalid credentials - <case>
    When User logs in with username "<username>" and password "<password>"
    Then Login error message "<message>" is displayed

    Examples:
      | case             | username | password   | message             |
      | wrong password   | Admin    | wrong_pass | Invalid credentials |
      | wrong username   | wrong    | admin123   | Invalid credentials |

  @regression @login003
  Scenario: User logs out
    When User logs in as "standard"
    And User logs out
    Then User is on the login page
