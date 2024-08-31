package com.alon.filesviewer.browser.data.local

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaProvider @Inject constructor(private val mapper: DeviceFileMapper,
                                        private val contentResolver: ContentResolver) {

    companion object {
        const val ERROR_MEDIA_QUERY = "media store query error"
    }

    fun search(query: String,collectionUri: Uri): Observable<Result<List<DeviceFile>>> {
        return RxLocalStorage.mediaObservable({ searchMediaStore(query, collectionUri) },
            contentResolver,
            collectionUri)
    }

    private fun searchMediaStore(query: String, collectionUri: Uri): Result<List<DeviceFile>> {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val selection = "${MediaStore.MediaColumns.TITLE} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${MediaStore.MediaColumns.TITLE} ASC"
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
                val filePath = it.getString(pathColumn)

                res.add(mapper.map(File(filePath)))
            }

            cursor.close()
            Result.success(res)

        } ?: run {
            Result.failure(BrowserError.Internal(ERROR_MEDIA_QUERY))
        }
    }
}