package com.alon.filesviewer

import android.app.Application
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FilesViewerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        resolveAppTheme()
    }

    private fun resolveAppTheme() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (preferences.getBoolean(getString(com.alon.filesviewer.settings.ui.R.string.pref_dark_theme_key)
                ,getString(com.alon.filesviewer.settings.ui.R.string.pref_dark_theme_default).toBoolean())) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}