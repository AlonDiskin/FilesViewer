package com.alon.filesviewer.browser.ui.viewmodel

import android.content.Context
import android.os.Environment
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BrowserUiPathBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun buildPath(collection: DeviceFilesCollection): String {
        return when(collection) {
            DeviceFilesCollection.VIDEO -> "/Video"
            DeviceFilesCollection.IMAGE -> "/Image"
            DeviceFilesCollection.AUDIO -> "/Audio"
        }
    }

    fun buildPath(path: String): String {
        return path.replace(Environment.getExternalStorageDirectory().path,"/Home")
    }
}