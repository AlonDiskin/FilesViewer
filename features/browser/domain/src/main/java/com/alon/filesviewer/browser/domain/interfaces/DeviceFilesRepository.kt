package com.alon.filesviewer.browser.domain.interfaces

import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import io.reactivex.Observable

interface DeviceFilesRepository {

    fun getByCategory(category: BrowsedCategory): Observable<Result<List<DeviceFile>>>

    fun getFolder(path: String): Observable<Result<List<DeviceFile>>>

    fun search(query: String, filter: SearchFilter): Observable<Result<List<DeviceFile>>>
}