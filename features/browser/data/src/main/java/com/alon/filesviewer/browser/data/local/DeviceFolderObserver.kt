package com.alon.filesviewer.browser.data.local

import android.os.FileObserver
import java.io.File
class DeviceFolderObserver(private val path: String,
                           private val action: () -> (Unit)) {

    private val observers: List<FileObserver>

    init {
        val tempObservers = mutableListOf<FileObserver>()
        subscribeObservers(tempObservers)
        observers = tempObservers
    }

    fun startObserving() {
        observers.forEach { observer -> observer.startWatching() }
    }

    fun stopObserving() {
        observers.forEach { observer -> observer.stopWatching() }
    }

    private fun subscribeObservers(observers: MutableList<FileObserver>) {
        val folder = File(path)

        folder.listFiles()?.let { files ->
            files.map { file ->
                observers.add(
                    object : FileObserver(file.path) {
                        override fun onEvent(event: Int, path: String?) {
                            if (event == FileObserver.DELETE ||
                                event == FileObserver.CREATE ||
                                event == FileObserver.MODIFY ||
                                event == FileObserver.MOVE_SELF) {
                                action.invoke()
                            }
                        }
                    }
                )
            }
        }

        observers.add(
            object : FileObserver(folder.path) {
                override fun onEvent(event: Int, path: String?) {
                    val folderEvent = event and FileObserver.ALL_EVENTS

                    if (folderEvent == FileObserver.DELETE ||
                        folderEvent == FileObserver.CREATE ||
                        folderEvent == FileObserver.MODIFY ||
                        folderEvent == FileObserver.MOVED_FROM ||
                        folderEvent == FileObserver.MOVED_TO ||
                        folderEvent == FileObserver.DELETE_SELF) {
                        action.invoke()
                    }
                }
            }
        )
    }
}