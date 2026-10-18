@admin @ui
Feature: Admin - User Management
  As an OrangeHRM admin
  I want to add a system user
  So that an employee can sign in to the system

  @smoke @admin001
  Scenario: Admin adds a new user successfully
    Given User is logged in as "standard"
    When User navigates to the Admin menu
    Then User lands on the "Admin" page
    And User sees the "System Users" section
    When User clicks the Add button 
    Then User sees the "Add User" section
    When User fills the new user form with role "ESS", employee starting with "a" and status "Enabled"
    And User saves the new user
    Then Success message "Successfully Saved" is displayed
    Then The new user is available in the table with role "ESS" and status "Enabled"
