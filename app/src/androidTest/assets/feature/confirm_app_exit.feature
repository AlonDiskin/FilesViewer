Feature: User journey scenario to confirm exit from app

  Scenario: User confirms app exit
    Given user open app from device home
    When he choose to leave app
    Then app should show exit dialog
    When user confirm exit
    Then app should close