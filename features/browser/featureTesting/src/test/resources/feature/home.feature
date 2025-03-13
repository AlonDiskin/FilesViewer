Feature: App home

  @exit-dialog
  Scenario Outline: User confirm app exit
    Given user opened app home screen
    When he select to exit app
    Then app should show exit confirmation dialog
    When user select to "<user_selection>" from dialog options
    Then app should "<app_action>"
    Examples:
      | user_selection | app_action          |
      | confirm        | close app           |
      | decline        | do not close app    |