package com.alon.filesviewer.browser.featuretesting.search

import android.app.Activity
import android.net.Uri
import android.os.Environment
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.alon.filesviewer.browser.featuretesting.util.FakeMediaContentProvider
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

class OpenDirSteps(private val mediaContentProvider: FakeMediaContentProvider) :GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<SearchActivity>
    private var expectedPath = ""

    init {
        mockkStatic(Environment::class)
        mockkStatic(FileProvider::class)
    }

    @Given("^user has a directory called music$")
    fun userHasDir() {
        // Create test file system and test music dir
        val rootMockFile = mockk<File>()
        val rootDir = TemporaryFolder()

        rootDir.create()
        expectedPath = rootDir.newFolder("Music").path

        every { Environment.getExternalStorageDirectory() } returns rootMockFile
        every { rootMockFile.path } returns rootDir.root.path
        every { FileProvider.getUriForFile(any(),any(),any()) } returns Uri.EMPTY
    }

    @When("^he find dir via search$")
    fun userFindDir() {
        // Launch search activity
        scenario = ActivityScenario.launchActivityForResult(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Perform search
        onView(withId(com.google.android.material.R.id.search_src_text))
            .perform(ViewActions.typeText("Mus"))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^select to open it$")
    fun userSelectDir() {
        onView(withId(R.id.searchResults))
            .perform(
                actionOnItemAtPosition<FileViewHolder>(
                    0,
                    click()
                )
            )
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should open directory files in browser screen$")
    fun appOpenDirInBrowserScreen() {
        assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(scenario.result.resultData.getStringExtra(SearchActivity.RESULT_DIR_PATH))
            .isEqualTo(expectedPath)
        scenario.onActivity { assertThat(it.isFinishing).isTrue() }
    }
}