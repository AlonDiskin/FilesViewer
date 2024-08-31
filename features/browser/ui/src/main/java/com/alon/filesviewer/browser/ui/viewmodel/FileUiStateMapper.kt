package com.alon.filesviewer.browser.ui.viewmodel

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.ui.data.FileUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import org.apache.commons.io.FileUtils
import java.io.File
import javax.inject.Inject

@ViewModelScoped
class FileUiStateMapper @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    fun map(file: DeviceFile): FileUiState {
        val uri = FileProvider.getUriForFile(
            applicationContext,
            applicationContext.packageName.plus(".provider"),
            File(file.path)
        )
        val format = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return FileUiState(
            file.path,
            file.name,
            file.type,
            uri,
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(format) ?: "",
            convertSize(file.size)
        )
    }

    private fun convertSize(size: Long): String {
        return FileUtils.byteCountToDisplaySize(size)
    }
}