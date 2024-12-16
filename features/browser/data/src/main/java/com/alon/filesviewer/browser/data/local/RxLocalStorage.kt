package com.alon.filesviewer.browser.data.local

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

object RxLocalStorage {
    fun <R: Any> filesObservable(fetch: () -> (R)): Observable<R> {
        return Observable.create { emitter ->
            val storageObserver = LocalStorageObserver { emitter.onNext(fetch.invoke()) }

            storageObserver.startObserving()
            emitter.onNext(fetch.invoke())
            emitter.setCancellable { storageObserver.stopObserving() }
        }
            .subscribeOn(Schedulers.io())
    }

    fun <R: Any> folderObservable(path: String,fetch: () -> (R)): Observable<R> {
        return Observable.create { emitter ->
            val folderObserver = DeviceFolderObserver(path) { emitter.onNext(fetch.invoke()) }

            folderObserver.startObserving()
            emitter.onNext(fetch.invoke())
            emitter.setCancellable { folderObserver.stopObserving() }
        }
            .subscribeOn(Schedulers.io())
    }

    fun <R: Any> mediaObservable(fetch: () -> (R),
                        contentResolver: ContentResolver,
                        contentUri: Uri
    ): Observable<R> {
        return Observable.create { emitter ->
            // Initialize content observer
            val contentObserver = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    emitter.onNext(fetch.invoke())
                }
            }

            // Register observer to content provider
            contentResolver.registerContentObserver(
                contentUri,
                true,
                contentObserver)

            // Unregister observer from content provider upon this observable cancellation
            emitter.setCancellable { contentResolver.unregisterContentObserver(contentObserver) }

            // Initial content fetch
            emitter.onNext(fetch.invoke())
        }
            .subscribeOn(Schedulers.io())
    }
}
