package com.alon.filesviewer.browser.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.domain.model.SearchRequest
import com.alon.filesviewer.browser.domain.usecase.SearchDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.viewmodel.SearchViewModel.*
import com.alon.filesviewer.browser.ui.data.SearchUiState
import com.alon.filesviewer.browser.ui.viewmodel.SearchViewModel
import com.google.common.truth.Truth.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: SearchViewModel

    // Collaborators
    private val searchUseCase: SearchDeviceFilesUseCase = mockk()
    private val savedState = SavedStateHandle()

    // Stub data
    private val searchSubject = BehaviorSubject.create<Result<List<DeviceFile>>>()

    @Before
    fun setUp() {
        every { searchUseCase.execute(any()) } returns searchSubject

        viewModel = SearchViewModel(searchUseCase,savedState)
    }

    @Test
    fun initDefaultUiState_WhenCreated() {
        // Given
        val expected = SearchUiState()

        // Then
        assertThat(viewModel.searchUiState.value).isEqualTo(expected)
    }

    @Test
    fun restoreSavedState_WhenCreated() {
        // Given
        val state = SavedSearchState("query",SearchFilter.IMAGE)

        savedState[SearchViewModel.SAVED_STATE] = state

        // When
        viewModel = SearchViewModel(searchUseCase,savedState)

        // Then
        assertThat(viewModel.searchUiState.value!!.query).isEqualTo(state.query)
        assertThat(viewModel.searchUiState.value!!.filter).isEqualTo(state.filter)
        verify { searchUseCase.execute(SearchRequest(state.query,state.filter)) }
    }

    @Test
    fun saveSearchData_WhenChangedByView() {
        // Given
        val state = SavedSearchState("query",SearchFilter.IMAGE)

        // When
        viewModel.setFilter(state.filter)
        viewModel.setQuery(state.query)

        // Then
        assertThat(savedState.get<SavedSearchState>(SearchViewModel.SAVED_STATE)).isEqualTo(state)
    }

    @Test
    fun performDeviceFilesSearch_WhenQuerySetByView() {
        // Given
        val query = "query"

        // When
        viewModel.setQuery(query)

        // Then
        verify { searchUseCase.execute(SearchRequest(query,viewModel.searchUiState.value!!.filter)) }
    }

    @Test
    fun performDeviceFilesSearch_WhenFilterSetByView() {
        // Given
        val query = "query"
        val filter = SearchFilter.DOWNLOAD
        val uiState = WhiteBox.getInternalState(viewModel,"_searchUiState") as MutableLiveData<SearchUiState>

        uiState.value = SearchUiState(query = query)

        // When
        viewModel.setFilter(filter)

        // Then
        verify { searchUseCase.execute(SearchRequest(query,filter))}
    }

    @Test
    fun dontPerformDeviceFilesSearch_WhenQueryEmpty() {
        // Given
        val defaultFilter = SearchUiState().filter

        // When
        viewModel.setQuery("")

        // Then
        verify(exactly = 0) { searchUseCase.execute(SearchRequest("",defaultFilter))}
    }

    @Test
    fun updateUiState_WhenModelUpdateDeviceFilesSearchResults() {
        // Given
        val query = "query"
        val files = mockk<List<DeviceFile>>()
        val searchResult = Result.success(files)
        val expectedUiState = SearchUiState(query = query, results = files)

        viewModel.setQuery(query)

        // When
        searchSubject.onNext(searchResult)

        // Then
        assertThat(viewModel.searchUiState.value).isEqualTo(expectedUiState)
    }

    @Test
    fun updateUiState_WhenModelFailToDeliverSearchResults() {
        // Given
        val error = BrowserError.Internal("")

        viewModel.setQuery("query")

        // When
        searchSubject.onNext(Result.failure(error))

        // Then
        assertThat(viewModel.searchUiState.value!!.error).isEqualTo(error)
    }

    @Test
    fun clearErrorState_WhenClearedByView() {
        // Given
        val error = BrowserError.Internal("message")
        val uiState = WhiteBox.getInternalState(viewModel,"_searchUiState") as MutableLiveData<SearchUiState>

        uiState.value = SearchUiState(error = error)

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.searchUiState.value!!.error).isNull()
    }
}