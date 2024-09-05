package com.alon.filesviewer.browser.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.data.BrowserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val browseUseCase: BrowseDeviceFilesUseCase,
    private val uiPathBuilder: BrowserUiPathBuilder,
    private val requestStack: BrowserRequestStack

) : RxViewModel() {

    private val _uiState = MutableLiveData(BrowserUiState())
    val uiState: LiveData<BrowserUiState> = _uiState
    private val browseSubject = BehaviorSubject.create<BrowseRequest>()

    init {
        createBrowsingSubscription()
        browseCategory(BrowsedCategory.ALL)
    }

    fun browseCategory(category: BrowsedCategory) {
        val request = BrowseRequest.Category(category)
        val uiPath = uiPathBuilder.buildPath(category)
        _uiState.value = BrowserUiState(currentPath = uiPath)

        requestStack.setRoot(request)
        browseSubject.onNext(request)
    }

    fun isRootFolder(): Boolean {
        return requestStack.isCurrentRoot()
    }

    fun navUpFromFolder(): Boolean {
        return if (!requestStack.isCurrentRoot()) {
            when(val parent = requestStack.popToParent()) {
                is BrowseRequest.Category -> {
                    browseCategory(parent.category)
                }

                is BrowseRequest.Folder -> {
                    browseFolder(parent.path)
                }
            }
            true
        } else {
            false
        }
    }

    fun browseFolder(path: String) {
        val uiPath = uiPathBuilder.buildPath(path)
        _uiState.value = _uiState.value!!.copy(error = null, currentPath = uiPath)
        val request = BrowseRequest.Folder(path)

        requestStack.setCurrent(request)
        browseSubject.onNext(request)
    }

    fun clearError() {
        _uiState.value = _uiState.value!!.copy(error = null)
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