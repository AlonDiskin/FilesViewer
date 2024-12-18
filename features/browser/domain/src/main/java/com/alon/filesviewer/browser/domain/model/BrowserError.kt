package com.alon.filesviewer.browser.domain.model

sealed class BrowserError(errorMessage: String) : Throwable(errorMessage) {

    data class Internal(val errorMessage: String) : BrowserError(errorMessage)

    data class AccessDenied(val errorMessage: String) : BrowserError(errorMessage)

    data class NonExistingDir(val errorMessage: String) : BrowserError(errorMessage)
}