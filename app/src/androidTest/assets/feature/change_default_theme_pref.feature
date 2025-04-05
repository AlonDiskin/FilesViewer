Feature: User journey scenario to change the default visual app theme preference

  Scenario: User change app settings
    Given user open app from device home
    And navigates to app settings screen
    When he change default theme pref
    Then app should change theme to selected one