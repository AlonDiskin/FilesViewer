package com.alon.filesviewer.di

import com.alon.filesviewer.browser.ui.controller.BrowserNavigator
import com.alon.filesviewer.util.AppNavigatorImpl
import com.alon.filesviewer.login.ui.LoginNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class AppNavigationModule {
    @Binds
    abstract fun bindLoginNavigator(navigator: AppNavigatorImpl): LoginNavigator

    @Binds
    abstract fun bindBrowserNavigator(navigator: AppNavigatorImpl): BrowserNavigator

}