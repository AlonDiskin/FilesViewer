package com.alon.filesviewer.util

import android.content.Context
import android.content.Intent
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.alon.filesviewer.browser.ui.controller.BrowserNavigator
import com.alon.filesviewer.login.ui.LoginNavigator
import com.alon.filesviewer.settings.ui.SettingsActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppNavigatorImpl @Inject constructor(@ApplicationContext private val context: Context) : LoginNavigator,
    BrowserNavigator {
    override fun getHomeScreenIntent(): Intent {
        return Intent(context, BrowserActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    override fun getSettingsScreenIntent(): Intent {
        return Intent(context, SettingsActivity::class.java)
    }
}