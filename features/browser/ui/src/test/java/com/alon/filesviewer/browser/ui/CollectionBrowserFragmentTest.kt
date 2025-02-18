package com.alon.filesviewer.browser.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.ui.controller.CollectionBrowserFragment
import com.alon.filesviewer.browser.ui.controller.FilesAdapter
import com.alon.filesviewer.browser.ui.controller.HiltTestActivity
import com.alon.filesviewer.browser.ui.data.BrowserUiState
import com.alon.filesviewer.browser.ui.util.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.ui.util.launchFragmentInHiltContainer
import com.alon.filesviewer.browser.ui.util.withRecyclerViewSize
import com.alon.filesviewer.browser.ui.viewmodel.CollectionBrowserViewModel
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowDialog
import java.io.File

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CollectionBrowserFragmentTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject container
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: CollectionBrowserViewModel = mockk()

    // Stub data
    private val uiState = MutableLiveData<BrowserUiState>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked view model
        every { viewModel.uiState } returns uiState

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<CollectionBrowserFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showCollectionFiles_WhenDisplayed() {
        // Given
        val files = listOf(
            DeviceFile("path_1","name_1", DeviceFileType.IMAGE, 20L,"mime_1",20L),
            DeviceFile("path_2","name_2", DeviceFileType.IMAGE, 20L,"mime_2",20L),
            DeviceFile("path_3","name_3", DeviceFileType.IMAGE, 20L,"mime_3",20L)
        )

        // When
        uiState.value = BrowserUiState(files = files)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        // Then
        onView(withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(files.size)))
        files.forEachIndexed { index, fileUiState ->
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
                                withText(fileUiState.name)
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun showCurrentCollectionPath_WhenFolderBrowsed() {
        // Given
        val stateUpdate = BrowserUiState(currentPath = "Home/Images")

        // When
        uiState.value = stateUpdate
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.currentPathLabel))
            .check(matches(withText(stateUpdate.currentPath)))
    }

    @Test
    fun showFileDetail_WhenUserSelectToViewDetail() {
        // Given
        val file = DeviceFile(
            "path",
            "name",
            DeviceFileType.AUDIO,
            FileUtils.ONE_MB,
            "mime",
            10000L
        )
        val size = FileUtils.byteCountToDisplaySize(file.size)
        uiState.value = BrowserUiState(files = listOf(file))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        // When
        onView(withId(R.id.fileDetail))
            .perform(ViewActions.click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.title_action_detail))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(ViewActions.click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Truth.assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_file_detail))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
        onView(withText(
            ApplicationProvider.getApplicationContext<Context>()
                .getString(
                    R.string.file_detail,
                    file.name,
                    file.path,
                    size))
        )
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun openFileViaDeviceApp_WhenFileSelectedByUser() {
        // Given
        val files = listOf(
            DeviceFile("path_1","name_1",DeviceFileType.AUDIO, 20L,"mp3",20L)
        )
        uiState.value = BrowserUiState(files = files)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)
        Intents.init()

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(),any(), File(files.first().path)) } returns Uri.EMPTY

        // When
        onView(withText(files.first().name))
            .perform(ViewActions.click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        Truth.assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        Truth.assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Intents.release()
    }
}