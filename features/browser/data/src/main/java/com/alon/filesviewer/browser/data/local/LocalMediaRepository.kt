package com.alon.filesviewer.browser.data.local

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMediaRepository @Inject constructor(private val mapper: FileTypeMapper,
                                               private val contentResolver: ContentResolver) {

    companion object {
        const val ERROR_MEDIA_QUERY = "media store query error"
    }

    fun search(query: String,collectionUri: Uri): Observable<Result<List<DeviceFile>>> {
        val selection = "${MediaStore.MediaColumns.TITLE} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${MediaStore.MediaColumns.TITLE} ASC"
        return queryMedia(collectionUri,selection, selectionArgs, sortOrder)
    }

    fun getAll(collectionUri: Uri): Observable<Result<List<DeviceFile>>> {
        return queryMedia(collectionUri,null,null,null)
    }

    private fun queryMedia(collectionUri: Uri,
                           selection: String?,
                           selectionArgs: Array<String>?,
                           sortOrder: String?): Observable<Result<List<DeviceFile>>> {

        return Observable.create { emitter ->
            // Initialize content observer
            val fetch = { fetchMedia(collectionUri, selection, selectionArgs, sortOrder) }
            val contentObserver = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    emitter.onNext(fetch.invoke())
                }
            }

            // Register observer to content provider
            contentResolver.registerContentObserver(
                collectionUri,
                true,
                contentObserver)

            // Unregister observer from content provider upon this observable cancellation
            emitter.setCancellable { contentResolver.unregisterContentObserver(contentObserver) }

            // Initial content fetch
            emitter.onNext(fetch.invoke())
        }
            .subscribeOn(Schedulers.io())
    }

    private fun fetchMedia(collectionUri: Uri,
                           selection: String?,
                           selectionArgs: Array<String>?,
                           sortOrder: String?): Result<List<DeviceFile>> {
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val cursor = contentResolver.query(
            collectionUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        return cursor?.let {
            val res = mutableListOf<DeviceFile>()

            while (it.moveToNext()) {
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val modifiedColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val filePath = it.getString(pathColumn)
                val fileTitle = it.getString(titleColumn)
                val fileSize = it.getLong(sizeColumn)
                val fileModified = it.getLong(modifiedColumn)

                res.add(
                    DeviceFile(
                        filePath,
                        fileTitle,
                        mapper.map(filePath),
                        fileSize,
                        filePath.split(".").last(),
                        fileModified
                    )
                )
            }

            cursor.close()
            Result.success(res)

        } ?: run {
            Result.failure(BrowserError.Internal(ERROR_MEDIA_QUERY))
        }
    }
}