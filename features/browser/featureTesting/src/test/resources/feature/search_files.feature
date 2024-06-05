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
