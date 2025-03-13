package com.alon.filesviewer.di

import com.alon.filesviewer.util.AppNavigatorImpl
import com.alon.filesviewer.login.ui.LoginNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class LoginNavigationModule {
    @Binds
    abstract fun bindAppNavigator(navigator: AppNavigatorImpl): LoginNavigator

}