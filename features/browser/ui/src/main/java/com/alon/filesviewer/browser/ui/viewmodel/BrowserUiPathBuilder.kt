package com.alon.filesviewer.browser.ui.viewmodel

import android.content.Context
import android.os.Environment
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BrowserUiPathBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun buildPath(category: BrowsedCategory): String {

        return when(category) {
            BrowsedCategory.ALL -> context.getString(R.string.path_root_device_home)
            BrowsedCategory.DOWNLOADS -> context.getString(R.string.path_root_device_downloads)
            BrowsedCategory.VIDEO -> context.getString(R.string.path_root_device_videos)
            BrowsedCategory.IMAGE -> context.getString(R.string.path_root_device_images)
            BrowsedCategory.AUDIO -> context.getString(R.string.path_root_device_audios)
        }
    }

    fun buildPath(path: String): String {
        return path.replace(Environment.getExternalStorageDirectory().path,"/Home")
    }
}