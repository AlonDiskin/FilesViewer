package com.alon.filesviewer.browser.featuretesting.search

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.browser.featuretesting.util.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.featuretesting.util.withRecyclerViewSize
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.google.android.material.R
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

class BrowseSearchResultSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<SearchActivity>
    private lateinit var testFolder: File

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

    @Given("^user has a folder named \"([^\"]*)\" with \"([^\"]*)\" file in it on device$")
    fun userHasFolder(folderName: String, fileName: String) {
        // Create test file system and
        val rootDir = TemporaryFolder()
        val testFileNameParts = fileName.split(".")

        // Create test folder and file
        rootDir.create()
        testFolder = rootDir.newFolder(folderName)
        Files.createTempFile(
            Path(testFolder.path),
            testFileNameParts.first(),testFileNameParts.last())
            .toFile()

        every { Environment.getExternalStorageDirectory() } returns rootDir.root
    }

    @When("^he find folder via search$")
    fun userFindFolder() {
        // Launch search activity
        scenario = ActivityScenario.launchActivityForResult(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Perform search
        onView(withId(R.id.search_src_text))
            .perform(typeText(testFolder.name))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^select folder form results$")
    fun userSelectFolderResult() {
        Thread.sleep(3000)
        onView(withId(com.alon.filesviewer.browser.ui.R.id.searchResults))
            .check(matches(withRecyclerViewSize(1)))
        onView(withRecyclerView(com.alon.filesviewer.browser.ui.R.id.searchResults).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should list folder in browser$")
    fun appListFolderInBrowser() {
        assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(scenario.result.resultData.getStringExtra(SearchActivity.RESULT_DIR_PATH))
            .isEqualTo(testFolder.path)
    }
}