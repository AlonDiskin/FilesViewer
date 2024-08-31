package com.alon.filesviewer.browser.ui.data

import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.SearchFilter

data class SearchUiState(val query: String = "",
                         val filter: SearchFilter = SearchFilter.ALL,
                         val results: List<FileUiState> = emptyList(),
                         val error: BrowserError? = null)
