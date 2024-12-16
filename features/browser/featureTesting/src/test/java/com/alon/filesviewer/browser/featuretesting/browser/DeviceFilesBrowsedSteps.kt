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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.featuretesting.util.FakeAudioFile
import com.alon.filesviewer.browser.featuretesting.util.FakeImageFile
import com.alon.filesviewer.browser.featuretesting.util.FakeMediaStoreContentProvider
import com.alon.filesviewer.browser.featuretesting.util.FakeVideoFile
import com.alon.filesviewer.browser.featuretesting.util.RecyclerViewMatcher.withRecyclerView
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
import org.hamcrest.CoreMatchers.allOf
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows

class DeviceFilesBrowsedSteps(fakeMediaStoreContentProvider: FakeMediaStoreContentProvider) : GreenCoffeeSteps() {

    // Browser feature ui
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    // Test data
    private val deviceRootFolderNames = listOf("Download","Music","Shared")
    private val expectedRootFolderNames = mutableListOf<String>()
    private val deviceDownloadFilesNames = emptyList<String>()
    private val deviceAudioFiles = listOf(
        FakeAudioFile("/home/music/metallica.mp3","metalica",30000L,30000L),
        FakeAudioFile("/home/music/beatles.mp3","beatles",30000L,30000L),
        FakeAudioFile("/home/music/elvis.mp3","elvis",30000L,30000L)
    )
    private val deviceVideoFiles = listOf(
        FakeVideoFile("/home/videos/home.mp4","home",30000L,30000L),
        FakeVideoFile("/home/videos/forest.mp4","forest",30000L,30000L)
    )
    private val deviceImageFiles = listOf(
        FakeImageFile("/home/pictures/bird.jpeg","bird",30000L,30000L),
        FakeImageFile("/home/pictures/cat.jpeg","cat",30000L,30000L)
    )

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

        // Create test device root and downloads folders
        val testRootFolder = TemporaryFolder()
        testRootFolder.create()

        deviceRootFolderNames.forEach {
            testRootFolder.newFolder(it)
        }

        testRootFolder.root.listFiles().forEach {
            if (it.isDirectory) {
                expectedRootFolderNames.add(it.name)
            }
        }

        every { Environment.getExternalStorageDirectory() } returns testRootFolder.root

        // Create test media files
        fakeMediaStoreContentProvider.addFakeAudioFiles(deviceAudioFiles)
        fakeMediaStoreContentProvider.addFakeImageFiles(deviceImageFiles)
        fakeMediaStoreContentProvider.addFakeVideoFiles(deviceVideoFiles)
    }

    @Given("^user opened browser screen$")
    fun userOpenBrowser() {
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should list device root folder files$")
    fun deviceRootFolderShouldBeListed() {
        Thread.sleep(3000)
        onView(withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(deviceRootFolderNames.size)))
        expectedRootFolderNames.forEachIndexed { index, rootFolderName ->
            onView(withId(R.id.browsedFiles))
                .perform(
                    scrollToPosition<FilesAdapter.FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            onView(withRecyclerView(R.id.browsedFiles).atPosition(index))
                .check(
                    matches(
                        hasDescendant(
                            allOf(
                                withId(R.id.fileName),
                                withText(rootFolderName)
                            )
                        )
                    )
                )

        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user select to browse device \"([^\"]*)\" files$")
    fun userSelectBrowseCategory(category: String) {
        val categorySelectionView = when(category) {
            "Audio" -> onView(withId(R.id.nav_audio))
            "Video" -> onView(withId(R.id.nav_video))
            "Download" -> onView(withId(R.id.nav_download))
            else -> throw IllegalArgumentException("Unknown arg:${category}")
        }

        categorySelectionView.perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should list \"([^\"]*)\" files$")
    fun appListCategoryFiles(category: String) {
        val expectedFileNames = when(category) {
            "Audio" -> deviceAudioFiles.map { it.title }
            "Video" -> deviceVideoFiles.map { it.title }
            "Download" -> deviceDownloadFilesNames
            else -> throw IllegalArgumentException("Unknown arg:${category}")
        }

        Thread.sleep(5000)
        onView(withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(expectedFileNames.size)))
        expectedFileNames.forEachIndexed { index, fileName ->
            onView(withId(R.id.browsedFiles))
                .perform(
                    scrollToPosition<FilesAdapter.FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.browsedFiles).atPosition(index))
                .check(
                    matches(
                        hasDescendant(
                            allOf(
                                withId(R.id.fileName),
                                withText(fileName)
                            )
                        )
                    )
                )
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}