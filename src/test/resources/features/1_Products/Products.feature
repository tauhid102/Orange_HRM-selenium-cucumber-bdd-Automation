@products @ui
Feature: Product catalogue
  As a logged in customer
  I want to browse, sort and pick products
  So that I can find what I want to buy

  Background:
    Given User is logged in as "standard"

  @smoke @products001
  Scenario: Product catalogue shows all items
    Then User lands on the "Products" page
    And User sees 6 products on the page

  @regression @products002
  Scenario Outline: Products can be sorted by <sortOption>
    When User sorts products by "<sortOption>"
    Then Products are sorted by "<sortOption>"

    Examples:
      | sortOption          |
      | Name (A to Z)       |
      | Name (Z to A)       |
      | Price (low to high) |
      | Price (high to low) |

  @regression @products003
  Scenario: Add and remove products from the cart
    When User adds the following products to the cart
      | Sauce Labs Backpack   |
      | Sauce Labs Bike Light |
    Then Cart badge shows 2
    When User removes "Sauce Labs Bike Light" from the cart on the products page
    Then Cart badge shows 1
