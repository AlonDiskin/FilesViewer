package com.alon.filesviewer.browser.featuretesting.di

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.alon.filesviewer.browser.ui.controller.BrowserNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideContentResolver(): ContentResolver {
        return ApplicationProvider.getApplicationContext<Context>().contentResolver
    }


    @Provides
    @Singleton
    fun provideNavigator(): BrowserNavigator {
        return mockk()
    }
}