Feature: User journey scenario to browse a folder that was searched for

  Scenario: User browse searched folder
    Given user has a folder named 'my-music' that has files
    When he open app from device home
    And locate folder via app search
    And clicks to browse it
    Then app should list folder in its files browser