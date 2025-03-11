package com.alon.filesviewer.browser.ui.viewmodel

import android.os.Environment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceFolderPathProvider @Inject constructor() {

    fun getRootDirPath(): String {
        return Environment.getExternalStorageDirectory().path
    }

    fun getDownloadsDirPath(): String {
        return Environment.getExternalStorageDirectory().path
            .plus("/${Environment.DIRECTORY_DOWNLOADS}")
    }
}