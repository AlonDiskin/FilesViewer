package com.alon.filesviewer.browser.data.local

import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageRepository @Inject constructor(private val filesRepo: LocalFilesRepository,
                                                 private val mediaRepo: LocalMediaRepository,
                                                 private val pathProvider: PathProvider) {
    fun search(query: String, filter: SearchFilter): Observable<Result<List<DeviceFile>>> {
        return if (query.isEmpty()) {
            Observable.just(Result.success(emptyList()))
        } else {
            when (filter) {
                SearchFilter.FILES -> filesRepo.search(query,pathProvider.getRootDirPath())
                SearchFilter.DOWNLOAD -> filesRepo.search(query,pathProvider.getDownloadsDirPath())
                SearchFilter.IMAGE -> mediaRepo.search(query, pathProvider.getImageCollectionUri())
                SearchFilter.AUDIO -> mediaRepo.search(query, pathProvider.getAudioCollectionUri())
                SearchFilter.VIDEO -> mediaRepo.search(query, pathProvider.getVideoCollectionUri())
            }
        }
    }

    fun getCategoryFiles(category: BrowsedCategory): Observable<Result<List<DeviceFile>>> {
        return when(category) {
            BrowsedCategory.ALL -> filesRepo.getFolderFiles(pathProvider.getRootDirPath())
            BrowsedCategory.DOWNLOADS -> filesRepo.getFolderFiles(pathProvider.getDownloadsDirPath())
            BrowsedCategory.IMAGE -> mediaRepo.getAll(pathProvider.getImageCollectionUri())
            BrowsedCategory.VIDEO -> mediaRepo.getAll(pathProvider.getVideoCollectionUri())
            BrowsedCategory.AUDIO -> mediaRepo.getAll(pathProvider.getAudioCollectionUri())
        }
    }

    fun getFolderFiles(path: String): Observable<Result<List<DeviceFile>>> {
        return filesRepo.getFolderFiles(path)
    }
}