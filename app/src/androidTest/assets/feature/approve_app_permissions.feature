Feature: User journey scenario to approve app runtime permissions

  Scenario: User approve permissions
    Given user has not yet approved storage permissions
    When user open app from device home
    Then app should open file access permission settings screen
    When user approve permission
    And go back to app
    Then app should launch home screen