package com.alon.filesviewer.browser.domain.usecase

import com.alon.filesviewer.browser.domain.interfaces.AppPreferenceManager
import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchRequest
import io.reactivex.Observable
import javax.inject.Inject

class SearchDeviceFilesUseCase @Inject constructor(
    private val repository: DeviceFilesRepository,
    private val prefManager: AppPreferenceManager
) {

    fun execute(request: SearchRequest): Observable<Result<List<DeviceFile>>> {
        return prefManager.isHiddenFilesShowingEnabled()
            .flatMap { enabled -> repository.search(request.query,request.filter,enabled) }
    }
}