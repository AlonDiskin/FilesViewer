package com.alon.filesviewer.browser.featuretesting.browser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.featuretesting.util.RecyclerViewMatcher
import com.alon.filesviewer.browser.featuretesting.util.withRecyclerViewSize
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.alon.filesviewer.browser.ui.controller.FilesAdapter
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import org.hamcrest.CoreMatchers
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import java.nio.file.Files
import kotlin.io.path.Path

class BrowserNavigationSteps : GreenCoffeeSteps() {

    // Browser feature ui
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    // Test data
    private val deviceRootFolderNames = listOf("Android")
    private val selectedFolderFilesName = listOf("file_1")
    private val expectedAndroidFolderFilesNames = mutableListOf<String>()
    private val selectedFolder = deviceRootFolderNames.first()

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

        deviceRootFolderNames.forEach { folderName ->
            val folder = testRootFolder.newFolder(folderName)

            if (folder.name == selectedFolder) {
                selectedFolderFilesName.forEach { fileName ->
                    expectedAndroidFolderFilesNames.add(
                        Files.createTempFile(Path(folder.path),fileName,".pdf").toFile().name
                    )
                }
            }
        }

        every { Environment.getExternalStorageDirectory() } returns testRootFolder.root
    }

    @Given("^user has opened browser screen$")
    fun userOpenBrowser() {
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he open existing folder in root folder$")
    fun userOpenFolderInRoot() {
        Thread.sleep(3000)
        onView(RecyclerViewMatcher.withRecyclerView(R.id.browsedFiles).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^browser should list folder files$")
    fun browserListFolderFiles() {
        Thread.sleep(5000)
        onView(ViewMatchers.withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(selectedFolderFilesName.size)))
        expectedAndroidFolderFilesNames.forEachIndexed { index, fileName ->
            onView(ViewMatchers.withId(R.id.browsedFiles))
                .perform(
                    RecyclerViewActions.scrollToPosition<FilesAdapter.FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(RecyclerViewMatcher.withRecyclerView(R.id.browsedFiles).atPosition(index))
                .check(
                    matches(
                        ViewMatchers.hasDescendant(
                            CoreMatchers.allOf(
                                ViewMatchers.withId(R.id.fileName),
                                withText(fileName)
                            )
                        )
                    )
                )

        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user returns to root folder$")
    fun userReturnToRoot() {
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(5000)
    }

    @Then("^browser should list root folder content$")
    fun browserShouldListRootFolder() {
        onView(ViewMatchers.withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(deviceRootFolderNames.size)))
        deviceRootFolderNames.forEachIndexed { index, rootFolderName ->
            onView(ViewMatchers.withId(R.id.browsedFiles))
                .perform(
                    RecyclerViewActions.scrollToPosition<FilesAdapter.FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(RecyclerViewMatcher.withRecyclerView(R.id.browsedFiles).atPosition(index))
                .check(
                    matches(
                        ViewMatchers.hasDescendant(
                            CoreMatchers.allOf(
                                ViewMatchers.withId(R.id.fileName),
                                withText(rootFolderName)
                            )
                        )
                    )
                )

        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}