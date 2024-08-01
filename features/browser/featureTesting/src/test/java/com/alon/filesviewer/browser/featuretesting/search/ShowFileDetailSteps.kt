package com.alon.filesviewer.browser.featuretesting.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.featuretesting.util.FakeMediaContentProvider
import com.alon.filesviewer.browser.featuretesting.util.TestMediaFile
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.apache.commons.io.FileUtils
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import java.io.File

class ShowFileDetailSteps(private val mediaContentProvider: FakeMediaContentProvider) :GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<SearchActivity>
    private val query = "meta"
    private lateinit var audioFile: File

    init {
        mockkStatic(Environment::class)
        mockkStatic(FileProvider::class)

        // Stub storage permission as granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            every { Environment.isExternalStorageManager() } returns true
        } else {
            mockkStatic(ContextCompat::class)
            every {
                ContextCompat.checkSelfPermission(
                    any(), // activity context not yet available
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } returns PackageManager.PERMISSION_GRANTED
        }
    }

    @Given("^user has an audio file on device$")
    fun userHasAudioFile() {
        // Create test file system and test audio file
        val rootMockFile = mockk<File>()
        val rootDir = TemporaryFolder()

        rootDir.create()

        audioFile = rootDir.newFile("metallica.mp3")

        mediaContentProvider.addTestMediaFiles(
            TestMediaFile(audioFile.path,audioFile.name,FakeMediaContentProvider.AUDIO_FILE)
        )
        every { Environment.getExternalStorageDirectory() } returns rootMockFile
        every { rootMockFile.path } returns rootDir.root.path
        every { FileProvider.getUriForFile(any(),any(),any()) } returns Uri.EMPTY
    }

    @When("^he find it via search$")
    fun userFindFile() {
        // Launch search activity
        scenario = ActivityScenario.launch(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Perform search
        onView(withId(R.id.chipFilterAudio))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withId(com.google.android.material.R.id.search_src_text))
            .perform(ViewActions.typeText(query))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^select to view its detail info$")
    fun userSelectViewDetail() {
        onView(withId(R.id.fileDetail))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.title_action_detail))
            .inRoot(isPlatformPopup())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should show file detail$")
    fun appShowFileDetail() {
        val fileSize = FileUtils.byteCountToDisplaySize(audioFile.length())

        onView(
            withText(
                ApplicationProvider.getApplicationContext<Context>()
                    .getString(R.string.file_detail, audioFile.name, audioFile.path, fileSize)
            )
        )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}