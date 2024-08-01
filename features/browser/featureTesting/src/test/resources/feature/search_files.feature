Feature: Search device files

  @device-files-search
  Scenario Outline: Device files searched
    Given user device has files named "<files_name>"
    When he open search screen
    And perform search with "<query>" and filter "<filter>" selected
    Then app should list "<search_result>" sorted alphabetically
    Examples:
      | files_name                       | query        | filter       | search_result                    |
      | metallica.mp3,meta.png,metal.pdf | meta         | all          | metallica.mp3,meta.png,metal.pdf |
      | metallica.mp3,meta.png,metal.pdf | metallica    | all          | metallica.mp3                    |
      | metallica.mp3,meta.png,metal.pdf | lema         | all          | none                             |
      | none                             | meta         | all          | none                             |
      | metallica.mp3,meta.png,metal.pdf | meta         | image        | meta.png                         |
      | metallica.mp3,meta.png,metal.pdf | meta         | audio        | metallica.mp3                    |
      | metallica.mp3,meta.png,metal.pdf | meta         | video        | none                             |

  @open-file
  Scenario Outline: Open searched file
    Given user has an "<file_type>" file on device
    When user find file via search
    And select to open file
    Then app should allow user to open file via device app chooser
    Examples:
      | file_type |
      | image     |
      | audio     |
      | video     |
  
  @open-dir
  Scenario: Open directory result
    Given user has a directory called music
    When he find dir via search
    And select to open it
    Then app should open directory files in browser screen

  @show-detail
  Scenario: Show file detail
    Given user has an audio file on device
    When he find it via search
    And select to view its detail info
    Then app should show file detail

