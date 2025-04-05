package com.alon.filesviewer.browser.domain.usecase

import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.interfaces.AppPreferenceManager
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.DeviceFile
import io.reactivex.Observable
import javax.inject.Inject

class BrowseDeviceFilesUseCase @Inject constructor(
    private val repository: DeviceFilesRepository,
    private val prefManager: AppPreferenceManager
) {

    fun execute(request: BrowseRequest): Observable<Result<List<DeviceFile>>> {
        return when(request) {
            is BrowseRequest.Collection -> repository.getCollection(request.collection)
            is BrowseRequest.Folder -> prefManager.isHiddenFilesShowingEnabled()
                .flatMap { enabled -> repository.getFolder(request.path,enabled) }
        }
    }
}