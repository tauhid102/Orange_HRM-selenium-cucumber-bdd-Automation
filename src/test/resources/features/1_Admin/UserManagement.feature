@admin @ui @usermanagement
Feature: Admin - User Management
  As an OrangeHRM admin
  I want to view, search, validate, edit and delete system users
  So that I can control who can sign in to the system

  Background:
    Given User is logged in as "standard"
    When User navigates to the Admin menu
    Then User sees the "System Users" section

  # ---------- Users list ----------

  @smoke @um001
  Scenario: Users list shows the records and columns
    Then The records message "Found" is displayed
    And The users table is displayed with the columns "Username, User Role, Employee Name, Status, Actions"

  # ---------- Add user: validation ----------

  @regression @um002
  Scenario: Add user is rejected when the required fields are empty
    When User clicks the Add button
    And User saves the new user
    Then The field "User Role" shows the error "Required"
    And The field "Employee Name" shows the error "Required"
    And The field "Status" shows the error "Required"
    And The field "Username" shows the error "Required"
    And The field "Password" shows the error "Required"

  @regression @um003
  Scenario: Add user is rejected for a username shorter than 5 characters
    When User clicks the Add button
    And User enters the username "abc"
    Then The field "Username" shows the error "Should be at least 5 characters"

  @regression @um004
  Scenario: Add user is rejected for a password shorter than 7 characters
    When User clicks the Add button
    And User enters the password "ab1" and confirm password "ab1"
    Then The field "Password" shows the error "Should have at least 7 characters"

  @regression @um005
  Scenario: Add user is rejected when the passwords do not match
    When User clicks the Add button
    And User enters the password "Pass1234567" and confirm password "Different1234"
    Then The field "Confirm Password" shows the error "Passwords do not match"

  @regression @um006
  Scenario: Add user is rejected for a username that already exists
    When User clicks the Add button
    And User enters the username "Admin"
    Then The field "Username" shows the error "Already exists"

  @regression @um007
  Scenario: Add user is rejected for an employee that does not exist
    When User clicks the Add button
    And User types the employee name "zzzznoemployee" without selecting a suggestion
    And User saves the new user
    Then The field "Employee Name" shows the error "Invalid"

  @regression @um008
  Scenario: Cancel on the Add User form returns to the users list
    When User clicks the Add button
    Then User sees the "Add User" section
    When User clicks the Cancel button
    Then User sees the "System Users" section

  # ---------- Search ----------

  @smoke @um009
  Scenario: Search users by username
    When User searches for the username "Admin"
    Then The table lists the user "Admin"

  @regression @um010
  Scenario: Search users by user role
    When User searches for the user role "Admin"
    Then Every listed user has the role "Admin"

  @regression @um011
  Scenario: Search users by status
    When User searches for the status "Disabled"
    Then Every listed user has the status "Disabled"

  @regression @um012
  Scenario: Search shows no records for an unknown username
    When User searches for the username "nouser_zzz999"
    Then The records message "No Records Found" is displayed

  @regression @um013
  Scenario: Reset clears the search filters
    When User searches for the user role "Admin"
    And User clicks the Reset button
    Then The search fields are cleared

  # ---------- Edit and delete ----------

  @regression @um014
  Scenario: Admin edits the status of a user
    Given A new user exists with role "ESS" and status "Enabled"
    When User edits the new user and sets the status "Disabled"
    Then Success message "Successfully Updated" is displayed
    When User searches for the new user by username
    Then The new user is available in the table with role "ESS" and status "Disabled"

  @regression @um015
  Scenario: Admin deletes a user
    Given A new user exists with role "ESS" and status "Enabled"
    When User deletes the new user
    Then Success message "Successfully Deleted" is displayed
    When User searches for the new user by username
    Then The records message "No Records Found" is displayed
