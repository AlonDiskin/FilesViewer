package com.alon.filesviewer.browser.ui.viewmodel

import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.ui.data.FileUiState
import javax.inject.Inject

class FileUiStateMapper @Inject constructor() {

    fun map(file: DeviceFile): FileUiState {
        return FileUiState(file.path,file.name,file.type)
    }
}