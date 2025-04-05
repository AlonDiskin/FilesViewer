package com.alon.filesviewer.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.alon.messeging.R as MessegingR

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_dark_theme_key))
            ?.setOnPreferenceChangeListener { _, newValue ->
                setDayNightTheme(newValue as Boolean)

                true
            }
    }

    private fun setDayNightTheme(isDarkTheme: Boolean) {
        when(isDarkTheme) {
            true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}