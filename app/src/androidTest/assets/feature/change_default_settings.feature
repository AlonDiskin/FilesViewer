Feature: User journey scenario to change the default app settings, and check
  the affect ion app behaviour

  Scenario: User change app settings
    Given user open app from device home
    And navigates to app settings screen
    When he change default theme pref
    And change default files sorting pref
    When he navigates to browser screen
    Then app should change theme to selected one
    And list app files according to selected sorting