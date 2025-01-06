package com.alon.filesviewer.browser.data.implementation

import com.alon.filesviewer.browser.data.local.LocalStorageRepository
import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import io.reactivex.Observable
import javax.inject.Inject

class DeviceFilesRepositoryImp @Inject constructor(
    private val localRepo: LocalStorageRepository
) : DeviceFilesRepository {
    override fun getByCategory(category: BrowsedCategory): Observable<Result<List<DeviceFile>>> {
        return localRepo.getCategoryFiles(category)
    }

    override fun getFolder(path: String): Observable<Result<List<DeviceFile>>> {
        return localRepo.getFolderFiles(path)
    }

    override fun search(query: String, filter: SearchFilter): Observable<Result<List<DeviceFile>>> {
        return localRepo.search(query, filter)
    }
}