package com.alon.filesviewer.browser.featuretesting.search

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.alon.filesviewer.browser.featuretesting.util.FakeMediaContentProvider
import com.alon.filesviewer.browser.featuretesting.util.TestMediaFile
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.alon.filesviewer.browser.ui.controller.SearchResultsAdapter.FileViewHolder
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import java.io.File

class OpenFileSteps(private val mediaContentProvider: FakeMediaContentProvider) :GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<SearchActivity>
    private var searchFilterId: Int = -1
    private var query = ""
    private var expectedUri = Uri.EMPTY
    private var expectedMime = ""

    init {
        mockkStatic(Environment::class)
        mockkStatic(FileProvider::class)
    }

    @Given("^user has an \"([^\"]*)\" file on device$")
    fun userHasFile(file: String) {
        // Create test folder, set scenario file to folder and fake media store provider
        val rootMockFile = mockk<File>()
        val rootDir = TemporaryFolder()
        val name: String
        val path: String
        val type: Int

        rootDir.create()
        when(file) {
            "image" -> {
                name = "mona_liza.png"
                path = rootDir.newFile(name).path
                type = FakeMediaContentProvider.IMAGE_FILE
                searchFilterId = R.id.chipFilterImages
                query = "mo"
                expectedMime = "image/*"
                Shadows.shadowOf(MimeTypeMap.getSingleton())
                    .addExtensionMimeTypeMapping("png",expectedMime)
            }
            "audio" -> {
                name = "mozart.mp3"
                path = rootDir.newFile(name).path
                type = FakeMediaContentProvider.AUDIO_FILE
                searchFilterId = R.id.chipFilterAudio
                query = "mo"
                expectedMime = "audio/*"
                Shadows.shadowOf(MimeTypeMap.getSingleton())
                    .addExtensionMimeTypeMapping("mp3",expectedMime)
            }
            "video" -> {
                name = "horse_ride.mp4"
                path = rootDir.newFile(name).path
                type = FakeMediaContentProvider.VIDEO_FILE
                searchFilterId = R.id.chipFilterVideos
                query = "ho"
                expectedMime = "video/*"
                Shadows.shadowOf(MimeTypeMap.getSingleton())
                    .addExtensionMimeTypeMapping("mp4",expectedMime)
            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$file")
        }

        expectedUri = Uri.fromFile(File(path))
        mediaContentProvider.addTestMediaFiles(TestMediaFile(path,name, type))
        every { Environment.getExternalStorageDirectory() } returns rootMockFile
        every { rootMockFile.path } returns rootDir.root.path
        every { FileProvider.getUriForFile(any(),any(),any()) } returns expectedUri
    }

    @When("^user find file via search$")
    fun userFindFile() {
        // Launch search activity
        scenario = ActivityScenario.launch(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Perform search
        onView(withId(searchFilterId))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(com.google.android.material.R.id.search_src_text))
            .perform(ViewActions.typeText(query))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^select to open file$")
    fun userSelectFile() {
        // Click on first result
        Intents.init()
        onView(withId(R.id.searchResults))
            .perform(
                actionOnItemAtPosition<FileViewHolder>(
                    0,
                    click()
                )
            )
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should allow user to open file via device app chooser$")
    fun appOpenFile() {
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertThat(intent.data).isEqualTo(expectedUri)
        assertThat(intent.type).isEqualTo(expectedMime)

        Intents.release()
    }
}