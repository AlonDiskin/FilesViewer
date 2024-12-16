Feature: User journey scenario to browse to video file stored on device
  and open it via dedicated device app

  Scenario: User open image from device storage
    Given user has a image file named 'sun' stored on device
    When he open app from device home
    And open image via app browser
    Then app should show device apps chooser view it