package com.alon.filesviewer.browser.data.local

import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathProvider @Inject constructor() {

    fun getRootDirPath(): String {
        return Environment.getExternalStorageDirectory().path
    }

    fun getDownloadsDirPath(): String {
        return Environment.getExternalStorageDirectory().path
            .plus("/${Environment.DIRECTORY_DOWNLOADS}")
    }

    fun getAudioCollectionUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }

    fun getVideoCollectionUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
    }

    fun getImageCollectionUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }
}