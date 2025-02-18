package com.alon.filesviewer.browser.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.ui.util.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.ui.controller.FilesAdapter.FileViewHolder
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.alon.filesviewer.browser.ui.data.SearchUiState
import com.alon.filesviewer.browser.ui.util.withCheckedChip
import com.alon.filesviewer.browser.ui.util.withRecyclerViewSize
import com.alon.filesviewer.browser.ui.viewmodel.SearchViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowDialog
import java.io.File

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SearchActivityTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // System under test
    private lateinit var scenario: ActivityScenario<SearchActivity>

    // Collaborators
    private val viewModel = mockk<SearchViewModel>()

    // Stub data
    private val latestUiState = SearchUiState()
    private val uiState = MutableLiveData(latestUiState)

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<SearchViewModel>>().value } returns viewModel

        // Stub mocked view model
        every { viewModel.searchUiState } returns uiState

        // Stub storage permission as granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mockkStatic(Environment::class)
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

        // Launch activity under test
        scenario = ActivityScenario.launch(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun closeAndOpenMainAppActivity_WhenCreatedAndStoragePermissionMissing() {
        // Given
        every { Environment.isExternalStorageManager() } returns false

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            assertThat(activity.isFinishing).isTrue()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun closeAndOpenMainAppActivity_WhenCreatedAndStoragePermissionMissingApi24() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            assertThat(activity.isFinishing).isTrue()
        }
    }

    @Test
    fun launchActivity_WhenCreatedAndStoragePermissionExist() {
        // Given

        // Then
        assertThat(scenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun launchActivity_WhenCreatedAndStoragePermissionExistApi24() {
        // Given

        // Then
        assertThat(scenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    fun showLatestSearchFilterUiState_WhenCreated() {
        // Given
        val expectedFilterId = when(latestUiState.filter) {
            SearchFilter.FILES -> R.id.chipFilterAll
            SearchFilter.IMAGE -> R.id.chipFilterImages
            SearchFilter.VIDEO -> R.id.chipFilterVideos
            SearchFilter.AUDIO -> R.id.chipFilterAudio
            SearchFilter.DOWNLOAD -> R.id.chipFilterDownloads
        }

        // Then
        onView(withId(R.id.chipGroup))
            .check(matches(withCheckedChip(expectedFilterId)))
    }

    @Test
    fun showLatestSearchQueryUiState_WhenCreated() {
        // Given
        val expectedQuery = latestUiState.query

        // Then
        onView(withId(com.google.android.material.R.id.search_src_text))
            .check(matches(withText(expectedQuery)))
    }

    @Test
    fun performSearch_WhenQueryTyped() {
        // Given
        val query = "query"

        every { viewModel.setQuery(any()) } returns Unit

        // When
        onView(withId(com.google.android.material.R.id.search_src_text))
            .perform(typeText(query))
            .perform(pressImeActionButton())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 1) { viewModel.setQuery("q") }
        verify(exactly = 1) { viewModel.setQuery("qu") }
        verify(exactly = 1) { viewModel.setQuery("que") }
        verify(exactly = 1) { viewModel.setQuery("quer") }
        verify(exactly = 1) { viewModel.setQuery("query") }
    }

    @Test
    fun performSearch_WhenFilterSet() {
        // Given
        val filterChipsIds = listOf(R.id.chipFilterImages,
            R.id.chipFilterAudio,
            R.id.chipFilterDownloads,
            R.id.chipFilterVideos,
            R.id.chipFilterAll)
        val expectedFilters = listOf(SearchFilter.IMAGE,
            SearchFilter.AUDIO,
            SearchFilter.DOWNLOAD,
            SearchFilter.VIDEO,
            SearchFilter.FILES)

        every { viewModel.setFilter(any()) } returns Unit

        // When
        filterChipsIds.forEach { id ->
            onView(withId(id))
                .perform(click())
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        expectedFilters.forEach { filter -> verify(exactly = 1) { viewModel.setFilter(filter) } }
    }

    @Test
    fun showSearchResults_WhenResultsLoaded() {
        // Given
        val expectedResults = listOf(
            DeviceFile(
                "path_1",
                "name_1",
                DeviceFileType.DIR,
                2000L,
                "size_1",
                1000L
            ),
            DeviceFile(
                "path_2",
                "name_2",
                DeviceFileType.VIDEO,
                2000L,
                "mime_2",
                2000L
            ),
            DeviceFile(
                "path_3",
                "name_3",
                DeviceFileType.AUDIO,
                2000L,
                "mime_3",
                2000L
            )
        )

        // When
        uiState.value = SearchUiState(results = expectedResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        // Then
        onView(withId(R.id.searchResults))
            .check(matches(withRecyclerViewSize(expectedResults.size)))
        expectedResults.forEachIndexed { index, fileUiState ->
            onView(withId(R.id.searchResults))
                .perform(scrollToPosition<FileViewHolder>(index))

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.searchResults).atPosition(index))
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
    fun showErrorMessage_WhenSearchFail() {
        // Given
        val error = BrowserError.Internal("message")

        // When
        uiState.value = SearchUiState(error = error)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(
                matches(
                    allOf(
                        withText(R.string.error_feature),
                        isDisplayed()
                    )
                )
            )
    }

    @Test
    fun navigateBack_WhenSearchFieldClosed() {
        // Given

        // When
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            assertThat(activity.isFinishing).isTrue()
        }
    }

    @Test
    fun openSearchResultFileViaAppChooser_WhenSelectedByUserAndFileIsNotDir() {
        // Given
        val results = listOf(
            DeviceFile(
                "path",
                "file_name",
                DeviceFileType.TEXT,
                2000L,
                "mp3",
                2000L
            )
        )
        uiState.value = SearchUiState(results = results)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)
        Intents.init()

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(),any(), File(results.first().path)) } returns Uri.EMPTY

        // When
        onView(withRecyclerView(R.id.searchResults).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()


        // Then
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Intents.release()
    }

    @Test
    fun showResultFileDetail_WhenUserSelectFromMenu() {
        // Given
        val file = DeviceFile(
            "path",
            "name",
            DeviceFileType.DIR,
            FileUtils.ONE_MB,
            "mime",
            10000L
        )
        val size = FileUtils.byteCountToDisplaySize(file.size)

        uiState.value = SearchUiState(results = listOf(file))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        // When
        onView(withId(R.id.fileDetail))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.title_action_detail))
            .inRoot(isPlatformPopup())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_file_detail))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.file_detail,file.name,file.path,size)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun openFolderFilesInBrowserAndCloseScreen_WhenSelectToOpenFolderSearchResult() {
        // Given
        scenario = ActivityScenario.launchActivityForResult(SearchActivity::class.java)
        val results = listOf(
            DeviceFile(
                "path",
                "folder_name",
                DeviceFileType.DIR,
                2000L,
                "",
                2000L
            )
        )
        uiState.value = SearchUiState(results = results)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(),any(), File(results.first().path)) } returns Uri.EMPTY

        // When
        onView(withRecyclerView(R.id.searchResults).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(scenario.result.resultData.getStringExtra(SearchActivity.RESULT_DIR_PATH))
            .isEqualTo(results.first().path)

    }
}