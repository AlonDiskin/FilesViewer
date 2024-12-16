package com.alon.filesviewer.browser.data.local

import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilesProvider @Inject constructor(private val mapper: DeviceFileMapper) {

    fun search(query: String,path: String): Observable<Result<List<DeviceFile>>> {
        return RxLocalStorage.filesObservable { searchFiles(query, path) }
    }

    private fun searchFiles(name: String,startPath: String): Result<List<DeviceFile>> {
        val res = mutableListOf<DeviceFile>()
        val rootFile = File(startPath)

        searchRec(name,rootFile,res)
        res.sortByDescending(DeviceFile::name)
        return Result.success(res)
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

    fun getFolderFiles(path: String): Observable<Result<List<DeviceFile>>> {
        return if (path.isEmpty()) {
            Observable.just(Result.success(emptyList()))
        } else {
            RxLocalStorage.folderObservable(path) {
                if (isFileDir(path)) {
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
        }
    }

    private fun isFileDir(path: String): Boolean {
        return File(path).isDirectory
    }
}