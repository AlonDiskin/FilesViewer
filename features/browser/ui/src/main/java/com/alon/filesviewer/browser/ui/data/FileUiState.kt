package com.alon.filesviewer.browser.ui.data

import android.net.Uri
import com.alon.filesviewer.browser.domain.model.DeviceFileType

data class FileUiState(val path: String,
                       val name: String,
                       val type: DeviceFileType,
                       val uri: Uri,
                       val mime: String)