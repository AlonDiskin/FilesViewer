package com.alon.filesviewer.browser.ui.data

import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile

data class BrowserUiState(val files: List<DeviceFile> = emptyList(),
                          val currentPath: String = "/Home",
                          val error: BrowserError? = null)