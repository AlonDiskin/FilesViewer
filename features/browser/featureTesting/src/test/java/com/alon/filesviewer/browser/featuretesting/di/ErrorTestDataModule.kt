package com.alon.filesviewer.browser.featuretesting.di

import com.alon.filesviewer.browser.data.local.LocalFilesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ErrorTestDataModule {

    @Provides
    @Singleton
    fun provideFilesRepo(): LocalFilesRepository {
        return mockk()
    }
}