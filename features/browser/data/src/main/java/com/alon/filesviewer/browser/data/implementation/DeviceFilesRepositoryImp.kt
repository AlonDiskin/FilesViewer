package com.alon.filesviewer.browser.data.implementation

import com.alon.filesviewer.browser.data.local.LocalFilesRepository
import com.alon.filesviewer.browser.data.local.LocalMediaRepository
import com.alon.filesviewer.browser.data.local.PathProvider
import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.SearchFilter
import io.reactivex.Observable
import javax.inject.Inject

class DeviceFilesRepositoryImp @Inject constructor(
    private val filesRepo: LocalFilesRepository,
    private val mediaRepo: LocalMediaRepository,
    private val pathProvider: PathProvider
) : DeviceFilesRepository {
    override fun getCollection(collection: DeviceFilesCollection): Observable<Result<List<DeviceFile>>> {
        return when(collection) {
            DeviceFilesCollection.IMAGE -> mediaRepo.getAll(pathProvider.getImageCollectionUri())
            DeviceFilesCollection.VIDEO -> mediaRepo.getAll(pathProvider.getVideoCollectionUri())
            DeviceFilesCollection.AUDIO -> mediaRepo.getAll(pathProvider.getAudioCollectionUri())
        }
    }

    override fun getFolder(path: String,
                           showHiddenFiles: Boolean
    ): Observable<Result<List<DeviceFile>>> {
        return filesRepo.getFolderFiles(path,showHiddenFiles)
    }

    override fun search(query: String,
                        filter: SearchFilter,
                        showHiddenFiles: Boolean
    ): Observable<Result<List<DeviceFile>>> {
        return if (query.isEmpty()) {
            Observable.just(Result.success(emptyList()))
        } else {
            when (filter) {
                SearchFilter.FILES -> filesRepo.search(query,pathProvider.getRootDirPath(),showHiddenFiles)
                SearchFilter.DOWNLOAD -> filesRepo.search(query,pathProvider.getDownloadsDirPath(),showHiddenFiles)
                SearchFilter.IMAGE -> mediaRepo.search(query, pathProvider.getImageCollectionUri())
                SearchFilter.AUDIO -> mediaRepo.search(query, pathProvider.getAudioCollectionUri())
                SearchFilter.VIDEO -> mediaRepo.search(query, pathProvider.getVideoCollectionUri())
            }
        }
    }
}