package com.alon.filesviewer.browser.featuretesting.di

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideContentResolver(): ContentResolver {
        return ApplicationProvider.getApplicationContext<Context>().contentResolver
    }
}