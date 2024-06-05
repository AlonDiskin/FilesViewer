package com.alon.filesviewer.browser.featuretesting.search

import android.os.Environment
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.browser.featuretesting.util.FakeMediaContentProvider
import com.alon.filesviewer.browser.featuretesting.util.TestMediaFile
import com.alon.filesviewer.browser.featuretesting.util.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.featuretesting.util.withRecyclerViewSize
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.alon.filesviewer.browser.ui.controller.SearchResultsAdapter.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.hamcrest.CoreMatchers
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import java.io.File

class DeviceFilesSearchSteps(private val mediaContentProvider: FakeMediaContentProvider) :GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<SearchActivity>

    init {
        mockkStatic(Environment::class)
    }

    @Given("^user device has files named \"([^\"]*)\"$")
    fun userHasFiles(files: String) {
        // Create test folder
        val rootMockFile = mockk<File>()
        val rootDir = TemporaryFolder()
        rootDir.create()

        when(files) {
            "none" -> {
                // Do nothing
            }

            else -> {
                // Add files to test file system & media store
                val fileNames = files.split(",")

                if (fileNames.isEmpty()) {
                    throw IllegalArgumentException("Unknown scenario arg:$files")
                } else {
                    fileNames.forEach { name ->
                        val file = rootDir.newFile(name)
                        addFileToFakeMediaStore(file)
                    }
                }
            }
        }

        every { Environment.getExternalStorageDirectory() } returns rootMockFile
        every { rootMockFile.path } returns rootDir.root.path
    }

    @When("^he open search screen$")
    fun userOpenSearchScreen() {
        scenario = ActivityScenario.launch(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^perform search with \"([^\"]*)\" and filter \"([^\"]*)\" selected$")
    fun userPerformSearch(query: String, filter: String) {
        val filterId = when(filter) {
            "all" -> R.id.chipFilterAll
            "image" -> R.id.chipFilterImages
            "audio" -> R.id.chipFilterAudio
            "video" -> R.id.chipFilterVideos
            else -> throw IllegalArgumentException("Unknown scenario arg:$filter")
        }

        onView(withId(filterId))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withId(com.google.android.material.R.id.search_src_text))
            .perform(typeText(query))
            .perform(pressImeActionButton())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should list \"([^\"]*)\" sorted alphabetically$")
    fun appShowResults(results: String) {
        val expectedUiSearchResults = mutableListOf<String>()

        when(results) {
            "none" -> {
                // Check no results are shown
                onView(withId(R.id.searchResults))
                    .check(matches(withRecyclerViewSize(0)))
            }
            else -> {
                // Extract expected results, and verify they are shown
                val fileNames = results.split(",")

                if (fileNames.isEmpty()) {
                    throw IllegalArgumentException("Unknown scenario arg:$results")
                } else {
                    fileNames.forEach { name -> expectedUiSearchResults.add(name) }
                    expectedUiSearchResults.sortDescending()
                }

                onView(withId(R.id.searchResults))
                    .check(matches(withRecyclerViewSize(expectedUiSearchResults.size)))
                expectedUiSearchResults.forEachIndexed { index, result ->
                    onView(withId(R.id.searchResults))
                        .perform(
                            scrollToPosition<FileViewHolder>(
                                index
                            )
                        )

                    Shadows.shadowOf(Looper.getMainLooper()).idle()

                    onView(withRecyclerView(R.id.searchResults).atPosition(index))
                        .check(
                            matches(
                                hasDescendant(
                                    CoreMatchers.allOf(
                                        withId(R.id.fileName),
                                        withText(result)
                                    )
                                )
                            )
                        )
                }
            }
        }
    }

    private fun addFileToFakeMediaStore(file: File) {
        val format = file.name.split(".")[1]
        when(format) {
            "png" -> {
                mediaContentProvider.addTestMediaFiles(
                    TestMediaFile(file.path,file.name, FakeMediaContentProvider.IMAGE_FILE)
                )
            }
            "mp3" -> {
                mediaContentProvider.addTestMediaFiles(
                    TestMediaFile(file.path,file.name, FakeMediaContentProvider.AUDIO_FILE)
                )
            }
            else -> {
                println("Skipped adding file format $format to fake media store")
            }
        }
    }
}