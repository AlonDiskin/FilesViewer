package com.alon.filesviewer.browser.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.data.BrowserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class CollectionBrowserViewModel @Inject constructor(
    private val browseUseCase: BrowseDeviceFilesUseCase,
    private val pathBuilder: BrowserUiPathBuilder,
    private val stateHandle: SavedStateHandle
) : RxViewModel() {

    companion object {
        const val KEY_COLLECTION = "collection"
    }

    private val _uiState = MutableLiveData(createInitUiState())
    val uiState: LiveData<BrowserUiState> = _uiState
    private val browseSubject = BehaviorSubject.createDefault(createInitBrowseRequest())

    init {
        createBrowsingSubscription()
    }

    private fun createInitUiState(): BrowserUiState {
        val collection = stateHandle.get<String>(KEY_COLLECTION)!!
        val uiPath = pathBuilder.buildPath(DeviceFilesCollection.valueOf(collection))

        return BrowserUiState(currentPath = uiPath)
    }

    private fun createInitBrowseRequest(): BrowseRequest {
        val collection = stateHandle.get<String>(KEY_COLLECTION)!!

        return BrowseRequest.Collection(DeviceFilesCollection.valueOf(collection))
    }

    private fun createBrowsingSubscription() {
        addSubscription(
            browseSubject.switchMap { request -> browseUseCase.execute(request) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::handleBrowseUpdate)
        )
    }

    private fun handleBrowseUpdate(update: Result<List<DeviceFile>>) {
        when {
            update.isSuccess -> {
                val files = update.getOrNull()!!
                _uiState.value = _uiState.value!!.copy(
                    files = files,
                    error = null
                )
            }

            update.isFailure -> {
                _uiState.value!!.let { state ->
                    val error = update.exceptionOrNull() as BrowserError?
                    _uiState.value = state.copy(error = error)
                }
            }
        }
    }
}