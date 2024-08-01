Feature: User journey scenario to locate image file stored on device
  and open it via dedicated device app

  Scenario: User open image from device storage
    Given user has a image file named 'sun' stored in device
    When he open app from device home
    And locate image via app search
    And clicks on it
    Then app should show device apps chooser to open image file