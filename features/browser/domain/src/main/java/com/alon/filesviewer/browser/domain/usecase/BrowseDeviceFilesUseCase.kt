package com.alon.filesviewer.browser.domain.usecase

import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.DeviceFile
import io.reactivex.Observable
import javax.inject.Inject

class BrowseDeviceFilesUseCase @Inject constructor(private val repository: DeviceFilesRepository) {

    fun execute(request: BrowseRequest): Observable<Result<List<DeviceFile>>> {
        return when(request) {
            is BrowseRequest.Category -> repository.getByCategory(request.category)
            is BrowseRequest.Folder -> repository.getFolder(request.path)
        }
    }
}