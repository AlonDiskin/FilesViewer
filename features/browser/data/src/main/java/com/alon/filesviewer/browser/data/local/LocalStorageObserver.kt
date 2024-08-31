package com.alon.filesviewer.browser.data.local

import android.os.Environment
import android.os.FileObserver
import java.io.File

class LocalStorageObserver(action: () -> (Unit)) {

    private val observers: List<FileObserver>

    init {
        val tempObservers = mutableListOf<FileObserver>()
        subscribeObserver(
            File(Environment.getExternalStorageDirectory().path.toString() + "/"),
            action,
            tempObservers
        )
        observers = tempObservers
    }

    fun startObserving() {
        observers.forEach { observer -> observer.startWatching() }
    }

    fun stopObserving() {
        observers.forEach { observer -> observer.stopWatching() }
    }

    private fun subscribeObserver(file: File,action: () -> (Unit),observers: MutableList<FileObserver>) {
        if (!file.isDirectory) {
            return
        } else {
            val observer = object : FileObserver(file.path) {
                override fun onEvent(event: Int, path: String?) {
                    if (event == FileObserver.DELETE ||
                        event == FileObserver.CREATE ||
                        event == FileObserver.MOVE_SELF) {
                        action.invoke()
                    }
                }

            }
            observers.add(observer)
            file.listFiles()?.forEach {
                subscribeObserver(it,action, observers)
            }
        }
    }
}