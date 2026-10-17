@checkout @ui
Feature: Checkout
  As a customer with products in my cart
  I want to check out
  So that I can place my order

  Background:
    Given User is logged in as "standard"

  @smoke @e2e @checkout001
  Scenario: Customer completes an order end to end
    When User adds the following products to the cart
      | Sauce Labs Backpack |
      | Sauce Labs Onesie   |
    And User opens the cart
    Then Cart contains the following products
      | Sauce Labs Backpack |
      | Sauce Labs Onesie   |
    When User proceeds to checkout with random customer details
    Then Order item total equals the sum of product prices
    And Order total equals item total plus tax
    When User finishes the order
    Then Order confirmation "Thank you for your order!" is displayed

  @regression @checkout002
  Scenario Outline: Checkout form validates required fields - <field>
    When User adds the following products to the cart
      | Sauce Labs Backpack |
    And User opens the cart
    And User proceeds to checkout with first name "<first>" last name "<last>" postal code "<zip>"
    Then Checkout error message "<message>" is displayed

    Examples:
      | field       | first | last | zip  | message                        |
      | first name  |       | Doe  | 1207 | Error: First Name is required  |
      | last name   | John  |      | 1207 | Error: Last Name is required   |
      | postal code | John  | Doe  |      | Error: Postal Code is required |
