package com.alon.filesviewer.browser.data.local

import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFilesRepository @Inject constructor(private val mapper: DeviceFileMapper) {

    fun search(query: String,path: String): Observable<Result<List<DeviceFile>>> {
        return Single.create { it.onSuccess(FolderFileBatches(File(path))) }
            .subscribeOn(Schedulers.io())
            .flatMapObservable { batches -> searchBatches(batches,query) }
    }

    fun getFolderFiles(path: String): Observable<Result<List<DeviceFile>>> {
        return if (path.isEmpty()) {
            Observable.just(Result.success(emptyList()))
        } else {
            return Observable.create<Result<List<DeviceFile>>> { emitter ->
                val folderObserver = DeviceFolderObserver(path) { emitter.onNext(fetchFolderFiles(path)) }

                folderObserver.startObserving()
                emitter.onNext(fetchFolderFiles(path))
                emitter.setCancellable { folderObserver.stopObserving() }
            }
                .subscribeOn(Schedulers.io())
        }
    }

    private fun searchRec(name: String, file: File, res:MutableList<DeviceFile>) {
        if (file.name.contains(name,true)) {
            res.add(mapper.map(file))
        }

        if (file.isDirectory) {
            file.listFiles()?.forEach {
                searchRec(name,it,res)
            }
        }
    }

    private fun fetchFolderFiles(path: String): Result<List<DeviceFile>> {
        return if (isFileDir(path)) {
            val folder = File(path)
            try {
                folder.listFiles()?.let { files ->
                    Result.success(files.map { file -> mapper.map(file) })
                } ?: run {
                    val errorMessage = "Access Denied: $path"
                    Result.failure(BrowserError.AccessDenied(errorMessage))
                }
            } catch (error: Throwable) {
                val errorMessage = "Access Denied: $path"
                Result.failure(BrowserError.AccessDenied(errorMessage))
            }
        } else {
            val errorMessage = "Dir not existing: $path"
            Result.failure(BrowserError.NonExistingDir(errorMessage))
        }
    }

    private fun isFileDir(path: String): Boolean {
        return File(path).isDirectory
    }

    private fun searchBatches(batches: FolderFileBatches, query: String): Observable<Result<List<DeviceFile>>> {
        return Observable.combineLatest(
            searchBatch(batches.firstBatch, query),
            searchBatch(batches.secondBatch, query),
            searchBatch(batches.thirdBatch, query)
        ) { s1, s2, s3 ->
            val files = mutableListOf<DeviceFile>()

            files.addAll(s1)
            files.addAll(s2)
            files.addAll(s3)
            files.sortByDescending(DeviceFile::name)
            return@combineLatest Result.success(files)
        }
    }

    private fun searchBatch(batch: List<File>, query: String): Observable<List<DeviceFile>> {
        return Observable.create { emitter ->
            val res = mutableListOf<DeviceFile>()

            batch.forEach { file -> searchRec(query,file,res) }
            emitter.onNext(res.toList())

        }
            .subscribeOn(Schedulers.io())
    }
}