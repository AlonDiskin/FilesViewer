package com.alon.filesviewer.browser.domain.usecase

import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchRequest
import io.reactivex.Observable
import javax.inject.Inject

class SearchDeviceFilesUseCase @Inject constructor(
    private val deviceRepo: DeviceFilesRepository
) {

    fun execute(request: SearchRequest): Observable<Result<List<DeviceFile>>> {
        return deviceRepo.search(request.query,request.filter)
    }
}