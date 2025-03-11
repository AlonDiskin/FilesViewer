package com.alon.filesviewer.browser.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.domain.model.SearchRequest
import com.alon.filesviewer.browser.domain.usecase.SearchDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.data.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.migration.OptionalInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchDevice: SearchDeviceFilesUseCase,
    private val savedState: SavedStateHandle
) : RxViewModel() {

    companion object {
        const val SAVED_STATE = "saved_state"
    }

    private val _searchUiState = MutableLiveData(SearchUiState())
    val searchUiState: LiveData<SearchUiState> = _searchUiState
    private val searchSubject = BehaviorSubject.create<SearchRequest>()

    init {
        createSearchSubscription()
        restoreSavedState()
    }

    fun setQuery(query: String) {
        _searchUiState.value?.let { state ->
            savedState[SAVED_STATE] = SavedSearchState(query,state.filter)
            _searchUiState.value = state.copy(query = query)
            search()
        }
    }

    fun setFilter(filter: SearchFilter) {
        _searchUiState.value?.let { state ->
            savedState[SAVED_STATE] = SavedSearchState(state.query,filter)
            _searchUiState.value = state.copy(filter = filter)
            if (state.query.isNotEmpty()) {
                search()
            }
        }
    }

    private fun createSearchSubscription() {
        addSubscription(
            searchSubject.switchMap { request ->
                when(request.query.isNotEmpty()) {
                    true -> searchDevice.execute(request)
                    false -> Observable.just(Result.success(emptyList()))
                }
            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::handleSearchUpdate)
        )
    }

    private fun restoreSavedState() {
        if (savedState.contains(SAVED_STATE)) {
            savedState.get<SavedSearchState>(SAVED_STATE)?.let { state ->
                _searchUiState.value = SearchUiState(query = state.query, filter = state.filter)
                if (state.query.isNotEmpty()) {
                    search()
                }
            }
        }
    }

    private fun search() {
        _searchUiState.value?.let { state ->
            _searchUiState.value = state.copy(error = null)
            searchSubject.onNext(SearchRequest(state.query,state.filter))
        }
    }

    private fun handleSearchUpdate(update: Result<List<DeviceFile>>) {
        when {
            update.isSuccess -> {
                _searchUiState.value!!.let { state ->
                    val searchResults = update.getOrNull()!!
                    _searchUiState.value = state.copy(results = searchResults)
                }
            }

            update.isFailure -> {
                _searchUiState.value!!.let { state ->
                    val error = update.exceptionOrNull() as BrowserError?
                    _searchUiState.value = state.copy(error = error)
                }
            }
        }
    }

    fun clearError() {
        _searchUiState.value?.let { state ->
            _searchUiState.value = state.copy(error = null)
        }
    }

    data class SavedSearchState(val query: String, val filter: SearchFilter) : Serializable
}