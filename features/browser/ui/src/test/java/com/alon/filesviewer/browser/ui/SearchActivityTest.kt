package com.alon.filesviewer.browser.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.ui.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.alon.filesviewer.browser.ui.controller.SearchResultsAdapter.FileViewHolder
import com.alon.filesviewer.browser.ui.data.FileUiState
import com.alon.filesviewer.browser.ui.data.SearchUiState
import com.alon.filesviewer.browser.ui.viewmodel.SearchViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
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

        // Launch activity under test
        scenario = ActivityScenario.launch(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showLatestSearchFilterUiState_WhenCreated() {
        // Given
        val expectedFilterId = when(latestUiState.filter) {
            SearchFilter.ALL -> R.id.chipFilterAll
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
            SearchFilter.ALL)

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
            FileUiState(
                "path_1",
                "name_1",
                DeviceFileType.DIR,
                Uri.EMPTY,
                "mime_1"
            ),
            FileUiState(
                "path_2",
                "name_2",
                DeviceFileType.VIDEO,
                Uri.EMPTY,
                "mime_2"
            ),
            FileUiState(
                "path_3",
                "name_3",
                DeviceFileType.AUDIO,
                Uri.EMPTY,
                "mime_3"
            )
        )

        // When
        uiState.value = SearchUiState(results = expectedResults)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

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
            FileUiState(
                "path",
                "name",
                DeviceFileType.TEXT,
                Uri.EMPTY,
                "mime"
            )
        )
        uiState.value = SearchUiState(results = results)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Intents.init()

        // When
        onView(withText(results.first().name))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertThat(intent.data).isEqualTo(results.first().uri)
        assertThat(intent.type).isEqualTo(results.first().mime)

        Intents.release()
    }

    @Test
    fun openSearchResultDirInBrowserScreen_WhenSelectedByUser() {
        // Given
        val results = listOf(
            FileUiState(
                "path",
                "name",
                DeviceFileType.DIR,
                Uri.EMPTY,
                "mime"
            )
        )
        scenario = ActivityScenario.launchActivityForResult(SearchActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        uiState.value = SearchUiState(results = results)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.searchResults))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FileViewHolder>(
                    0,
                    click()
                )
            )
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(scenario.result.resultData.getStringExtra(SearchActivity.RESULT_DIR_PATH))
            .isEqualTo(results.first().path)
        scenario.onActivity { assertThat(it.isFinishing).isTrue() }
    }
}