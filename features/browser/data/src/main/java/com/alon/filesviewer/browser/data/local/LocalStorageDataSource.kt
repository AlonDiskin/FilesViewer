package com.alon.filesviewer.browser.data.local

import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageDataSource @Inject constructor(private val filesProvider: FilesProvider,
                                                 private val mediaProvider: MediaProvider,
                                                 private val pathProvider: PathProvider) {
    fun search(query: String, filter: SearchFilter): Observable<Result<List<DeviceFile>>> {
        return when (filter) {
            SearchFilter.ALL -> filesProvider.search(query,pathProvider.getRootDirPath())
            SearchFilter.DOWNLOAD -> filesProvider.search(query,pathProvider.getDownloadsDirPath())
            SearchFilter.IMAGE -> mediaProvider.search(query, pathProvider.getImageCollectionUri())
            SearchFilter.AUDIO -> mediaProvider.search(query, pathProvider.getAudioCollectionUri())
            SearchFilter.VIDEO -> mediaProvider.search(query, pathProvider.getVideoCollectionUri())
        }
    }
}