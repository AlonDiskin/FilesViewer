package com.alon.filesviewer.browser.data.implementation

import android.app.Application
import android.content.SharedPreferences
import com.alon.filesviewer.browser.domain.interfaces.AppPreferenceManager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import com.alon.messeging.R as MessegingR

class AppPreferenceManagerImpl @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : AppPreferenceManager {

    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    override fun isHiddenFilesShowingEnabled(): Observable<Boolean> {
        return Observable.create { emitter ->
            val prefKey = application.getString(MessegingR.string.pref_hidden_files_key)
            val prefDefault = application.getString(MessegingR.string.pref_hidden_files_default).toBoolean()
            val current = sharedPreferences.getBoolean(
                prefKey,
                prefDefault
            )
            listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs , key ->
                if (key == prefKey) {
                    val update = prefs.getBoolean(
                        prefKey,
                        false
                    )
                    emitter.onNext(update)
                }
            }


            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            emitter.onNext(current)
            emitter.setCancellable { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }
            .subscribeOn(Schedulers.io())
    }
}