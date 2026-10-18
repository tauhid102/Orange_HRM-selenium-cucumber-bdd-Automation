@admin @ui @job
Feature: Admin - Job
  As an OrangeHRM admin
  I want to manage job titles, pay grades, employment statuses and job categories
  So that employees can be given the right job details

  Background:
    Given User is logged in as "standard"
    When User navigates to the Admin menu
    Then User sees the "System Users" section

  @smoke @job001
  Scenario Outline: <page> list is displayed
    When User opens the Job page "<page>"
    Then User sees the "<list title>" section

    Examples:
      | page              | list title        |
      | Job Titles        | Job Titles        |
      | Pay Grades        | Pay Grades        |
      | Employment Status | Employment Status |
      | Job Categories    | Job Categories    |

  @smoke @job002
  Scenario Outline: Admin adds a record to <page>
    When User opens the Job page "<page>"
    And User clicks the Add button
    Then User sees the "<add title>" section
    When User enters a new unique name in the field "<field>"
    And User saves the record
    Then The record is saved
    When User opens the Job page "<page>"
    Then The list shows the record
    When User deletes the record
    Then Success message "Successfully Deleted" is displayed

    Examples:
      | page              | add title              | field     |
      | Job Titles        | Add Job Title          | Job Title |
      | Pay Grades        | Add Pay Grade          | Name      |
      | Employment Status | Add Employment Status  | Name      |
      | Job Categories    | Add Job Category       | Name      |

  @regression @job003
  Scenario Outline: <page> rejects an empty name
    When User opens the Job page "<page>"
    And User clicks the Add button
    And User saves the record
    Then The field "<field>" shows the error "Required"

    Examples:
      | page              | field     |
      | Job Titles        | Job Title |
      | Pay Grades        | Name      |
      | Employment Status | Name      |
      | Job Categories    | Name      |

  @regression @job004
  Scenario Outline: <page> rejects a name that already exists
    When User opens the Job page "<page>"
    And User clicks the Add button
    And User enters a new unique name in the field "<field>"
    And User saves the record
    Then The record is saved
    When User opens the Job page "<page>"
    And User clicks the Add button
    And User enters the same name again in the field "<field>"
    Then The field "<field>" shows the error "Already exists"
    When User clicks the Cancel button
    And User deletes the record
    Then Success message "Successfully Deleted" is displayed

    Examples:
      | page              | field     |
      | Job Titles        | Job Title |
      | Pay Grades        | Name      |
      | Employment Status | Name      |
      | Job Categories    | Name      |

  @regression @job005
  Scenario Outline: Cancel on the Add form of <page> returns to the list
    When User opens the Job page "<page>"
    And User clicks the Add button
    And User clicks the Cancel button
    Then User sees the "<list title>" section

    Examples:
      | page              | list title        |
      | Job Titles        | Job Titles        |
      | Pay Grades        | Pay Grades        |
      | Employment Status | Employment Status |
      | Job Categories    | Job Categories    |

  @regression @job006
  Scenario Outline: Admin edits a record in <page>
    When User opens the Job page "<page>"
    And User clicks the Add button
    And User enters a new unique name in the field "<field>"
    And User saves the record
    Then The record is saved
    When User opens the Job page "<page>"
    And User opens the record for editing
    And User renames the record in the field "<field>"
    And User saves the record
    Then Success message "Successfully Updated" is displayed
    When User opens the Job page "<page>"
    Then The list shows the record
    When User deletes the record
    Then Success message "Successfully Deleted" is displayed

    Examples:
      | page              | field     |
      | Job Titles        | Job Title |
      | Pay Grades        | Name      |
      | Employment Status | Name      |
      | Job Categories    | Name      |

  @regression @job007
  Scenario Outline: Admin deletes a record from <page>
    When User opens the Job page "<page>"
    And User clicks the Add button
    And User enters a new unique name in the field "<field>"
    And User saves the record
    Then The record is saved
    When User opens the Job page "<page>"
    And User deletes the record
    Then Success message "Successfully Deleted" is displayed
    And The list does not show the record

    Examples:
      | page              | field     |
      | Job Titles        | Job Title |
      | Pay Grades        | Name      |
      | Employment Status | Name      |
      | Job Categories    | Name      |
