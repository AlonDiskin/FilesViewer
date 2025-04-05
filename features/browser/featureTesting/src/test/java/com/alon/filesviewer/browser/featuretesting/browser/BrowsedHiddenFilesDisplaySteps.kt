package com.alon.filesviewer.browser.featuretesting.browser

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.featuretesting.util.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.featuretesting.util.withRecyclerViewSize
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.alon.filesviewer.browser.ui.controller.FilesAdapter
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import org.hamcrest.CoreMatchers.allOf
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

class BrowsedHiddenFilesDisplaySteps(private val sharedPreferences: SharedPreferences
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<BrowserActivity>
    private val testFiles: List<File>
    private lateinit var expectedFiles: List<File>

    init {
        // Stub storage permission as granted
        mockkStatic(Environment::class)
        mockkStatic(FileProvider::class)

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

        // Create test device root folder
        val testRootFolder = TemporaryFolder()
        testRootFolder.create()
        val hiddenFile = Files.createTempFile(
            Path(testRootFolder.root.path),
            ".file_1",".pdf"
        ).toFile()
        testFiles = listOf(
            hiddenFile,
            testRootFolder.newFile("image_1.jpeg")
        )

        if (hiddenFile.path.contains('\\')) {
            Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true)
        }

        every { Environment.getExternalStorageDirectory() } returns testRootFolder.root
    }

    @Given("^user has hidden files in device root folder$")
    fun userHasHiddenFiles() {

    }

    @And("^hidden files listing pref is \"([^\"]*)\"$")
    fun currentPrefIs(pref: String) {
        val app: Application = ApplicationProvider.getApplicationContext<Context>() as Application
        val key = app.getString(com.alon.messeging.R.string.pref_hidden_files_key)

        expectedFiles = when(pref) {
            "enabled" -> {
                sharedPreferences.edit()
                    .putBoolean(key,true)
                    .commit()
                testFiles
            }

            "disabled" -> {
                sharedPreferences.edit()
                    .putBoolean(key,false)
                    .commit()
                testFiles.filter { !it.isHidden }
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$pref")
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user open device root folder in browser screen$")
    fun userOpenRootDirBrowser() {
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^hidden files display should be according to pref$")
    fun browserShouldDisplay() {
        Thread.sleep(4000)
        onView(withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(expectedFiles.size)))
        expectedFiles.forEachIndexed { index, file ->
            onView(withId(R.id.browsedFiles))
                .perform(
                    scrollToPosition<FilesAdapter.FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(
                withRecyclerView(R.id.browsedFiles).atPosition(index)
            )
                .check(
                    matches(
                        hasDescendant(
                            allOf(
                                withId(R.id.fileName),
                                withText(file.name)
                            )
                        )
                    )
                )

        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^preference is changed to \"([^\"]*)\"$")
    fun prefChanged(pref: String) {
        val app: Application = ApplicationProvider.getApplicationContext<Context>() as Application
        val key = app.getString(com.alon.messeging.R.string.pref_hidden_files_key)

        expectedFiles = when(pref) {
            "enabled" -> {
                sharedPreferences.edit()
                    .putBoolean(key,true)
                    .commit()
                testFiles
            }

            "disabled" -> {
                sharedPreferences.edit()
                    .putBoolean(key,false)
                    .commit()
                testFiles.filter { !it.isHidden }
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:$pref")
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("browser should display hidden files accordingly$")
    fun browserShouldChangeDisplay() {
        Thread.sleep(4000)
        onView(withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(expectedFiles.size)))
        expectedFiles.forEachIndexed { index, file ->
            onView(withId(R.id.browsedFiles))
                .perform(
                    scrollToPosition<FilesAdapter.FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(
                withRecyclerView(R.id.browsedFiles).atPosition(index)
            )
                .check(
                    matches(
                        hasDescendant(
                            allOf(
                                withId(R.id.fileName),
                                withText(file.name)
                            )
                        )
                    )
                )

        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}