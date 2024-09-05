Feature: Browse device files

  @browse-category-files
  Scenario Outline: Device files browsed by category
    Given user opened browser screen
    Then app should list device root folder files
    When user select to browse device "<category>" files
    Then app should list "<category>" files
    Examples:
      | category   |
      | Audio      |
      | Video      |
      | Download   |

  @browser-nav
  Scenario: Navigate back from browsed folder
    Given user has opened browser screen
    When he open existing folder in root folder
    Then browser should list folder files
    When user returns to root folder
    Then browser should list root folder content

  @files-detail-shown
  Scenario: Browsed file detail shown
    Given user has opened browser screen
    When he select to view file detail in root folder
    Then browser should show file detail

  @browser-errors-handled
  Scenario Outline: Browser feature error handles
    Given user has opened browser screen
    When feature fail due to "<error>"
    Then browser should "<error_handle>"
    Examples:
      |  error                |  error_handle                        |
      | path not recognized   | show dir not exist message           |
      | internal feature fail | show feature fail  message           |
      | access restricted     | show show access restricted message  |
