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
import org.robolectric.shadows.ShadowContentResolver
import javax.inject.Inject
import javax.inject.Singleton

class FakeMediaStoreContentProvider @Inject constructor() : ContentProvider() {

    private lateinit var db: TestDatabase

    companion object {
        const val AUDIO_TABLE = "fakeaudiofile"
        const val VIDEO_TABLE = "fakevideofile"
        const val IMAGE_TABLE = "fakeimagefile"
    }

    init {
        initTestDb()
        registerWithRobolectricContentResolver()
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
        val tableName = when(uri) {
            getAudioContentUri() -> AUDIO_TABLE
            getImageContentUri() -> IMAGE_TABLE
            getVideoContentUri() -> VIDEO_TABLE
            else -> throw IllegalArgumentException("Unknown uri:$uri")
        }

        return try {
            val sqlQuery = SupportSQLiteQueryBuilder
                .builder(tableName)
                .columns(projection)
                .selection(selection,selectionArgs)
                .orderBy(sortOrder)
                .create()
            db.query(sqlQuery)
        } catch (error: Throwable) {
            error.printStackTrace()
            null
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

    fun addFakeAudioFiles(files: List<FakeAudioFile>) {
        files.forEach { db.audioDao().insert(it) }
    }

    fun addFakeVideoFiles(files: List<FakeVideoFile>) {
        files.forEach { db.videoDao().insert(it) }
    }

    fun addFakeImageFiles(files: List<FakeImageFile>) {
        files.forEach { db.imageDao().insert(it) }
    }

    private fun registerWithRobolectricContentResolver() {
        ShadowContentResolver.registerProviderInternal(getImageContentUri().authority,this)
        ShadowContentResolver.registerProviderInternal(getAudioContentUri().authority,this)
        ShadowContentResolver.registerProviderInternal(getVideoContentUri().authority,this)
    }

    private fun getImageContentUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun getVideoContentUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun getAudioContentUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun initTestDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}