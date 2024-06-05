package com.alon.filesviewer.browser.featuretesting.util

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import androidx.test.core.app.ApplicationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeMediaContentProvider @Inject constructor() : ContentProvider() {

    private val db: TestDatabase

    companion object {
        const val IMAGE_FILE = 1
        const val AUDIO_FILE = 2
        const val VIDEO_FILE = 3
        const val MEDIA_TABLE = "testmediafile"
    }

    init {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when(uri) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) ->
                    queryForMediaFiles(projection, selection, selectionArgs, sortOrder, AUDIO_FILE)
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) ->
                    queryForMediaFiles(projection, selection, selectionArgs, sortOrder, VIDEO_FILE)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) ->
                    queryForMediaFiles(projection, selection, selectionArgs, sortOrder, IMAGE_FILE)
                else -> throw IllegalArgumentException("Unknown uri arg:${uri}")
            }
        } else {
            when(uri) {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI ->
                    queryForMediaFiles(projection, selection, selectionArgs, sortOrder, AUDIO_FILE)
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI ->
                    queryForMediaFiles(projection, selection, selectionArgs, sortOrder, VIDEO_FILE)
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI ->
                    queryForMediaFiles(projection, selection, selectionArgs, sortOrder, IMAGE_FILE)
                else -> throw IllegalArgumentException("Unknown uri arg:${uri}")
            }
        }
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }

    fun addTestMediaFiles(file: TestMediaFile) {
        db.mediaFileDao().insert(file)
    }

    private fun queryForMediaFiles(projection: Array<out String>?,
                                   selection: String?,
                                   selectionArgs: Array<out String>?,
                                   sortOrder: String?,
                                   type: Int): Cursor? {
        val querySelection = selection?.let {
            it.plus("AND type = '${type}'")
        } ?: run {
            "type = '${type}'"
        }

        return try {
            val sqlQuery = SupportSQLiteQueryBuilder
                .builder(MEDIA_TABLE)
                .columns(projection)
                .selection(querySelection,selectionArgs)
                .orderBy(sortOrder)
                .create()
            db.query(sqlQuery)
        } catch (error: Throwable) {
            null
        }
    }
}