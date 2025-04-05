package com.alon.filesviewer.browser.domain.interfaces

import io.reactivex.Observable

interface AppPreferenceManager {

    fun isHiddenFilesShowingEnabled(): Observable<Boolean>
}