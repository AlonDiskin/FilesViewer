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

  @show-detail
  Scenario: Show file detail
    Given user has an audio file on device
    When he find it via search
    And select to view its detail info
    Then app should show file detail

  @browse-result
  Scenario Outline: Folder result is browsed
    Given user has a folder named "<folder_name>" with "<file_name>" file in it on device
    When he find folder via search
    And select folder form results
    Then app should list folder in browser
    Examples:
    | folder_name | file_name  |
    | my folder   | file_0.txt |

