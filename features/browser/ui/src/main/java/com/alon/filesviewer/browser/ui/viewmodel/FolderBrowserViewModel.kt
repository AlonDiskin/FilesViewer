package com.alon.filesviewer.browser.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceNamedFolder
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.data.BrowserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class FolderBrowserViewModel @Inject constructor(
    private val browseUseCase: BrowseDeviceFilesUseCase,
    private val pathBuilder: BrowserUiPathBuilder,
    private val folderPathProvider: DeviceFolderPathProvider,
    private val stateHandle: SavedStateHandle
) : RxViewModel()  {

    companion object {
        const val KEY_BASE_FOLDER = "base folder"
        const val KEY_DEST_FOLDER = "destination"
    }

    private val _uiState = MutableLiveData(createInitUiState())
    val uiState: LiveData<BrowserUiState> = _uiState
    private val browseSubject = BehaviorSubject.createDefault(createInitBrowseRequest())

    init {
        createBrowsingSubscription()
    }

    fun navToFolder(path: String) {
        val uiPath = pathBuilder.buildPath(path)
        _uiState.value = _uiState.value!!.copy(error = null, currentPath = uiPath)
        val request = BrowseRequest.Folder(path)

        browseSubject.onNext(request)
    }

    fun navUpFromFolder() {
        val currentFolder = (browseSubject.value!! as BrowseRequest.Folder).path
        val baseFolder = getBaseFolder()

        if (currentFolder != baseFolder) {
            navToFolder(FilenameUtils.getFullPathNoEndSeparator(currentFolder))
        }
    }

    fun isRootFolder(): Boolean {
        val currentFolder = (browseSubject.value!! as BrowseRequest.Folder).path
        val baseFolder = getBaseFolder()

        return currentFolder == baseFolder
    }

    private fun createInitUiState(): BrowserUiState {
        val folder = if (hasDestFolder()) {
            getDestFolder()
        } else {
            getBaseFolder()
        }
        val uiPath = pathBuilder.buildPath(folder)

        return BrowserUiState(currentPath = uiPath)
    }

    private fun createInitBrowseRequest(): BrowseRequest {
        return if (hasDestFolder()) {
            BrowseRequest.Folder(getDestFolder())
        } else {
            BrowseRequest.Folder(getBaseFolder())
        }
    }

    private fun getBaseFolder(): String {
        val folder = stateHandle.get<String>(KEY_BASE_FOLDER) ?: DeviceNamedFolder.ROOT.name
        return when(DeviceNamedFolder.valueOf(folder)) {
            DeviceNamedFolder.ROOT -> folderPathProvider.getRootDirPath()
            DeviceNamedFolder.DOWNLOAD -> folderPathProvider.getDownloadsDirPath()
        }
    }

    private fun getDestFolder(): String {
        return stateHandle.get<String>(KEY_DEST_FOLDER)!!
    }

    private fun hasDestFolder(): Boolean {
        return stateHandle.contains(KEY_DEST_FOLDER)
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