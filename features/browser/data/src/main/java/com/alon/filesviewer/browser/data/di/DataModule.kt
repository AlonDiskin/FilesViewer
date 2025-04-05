package com.alon.filesviewer.browser.data.di

import com.alon.filesviewer.browser.data.implementation.AppPreferenceManagerImpl
import com.alon.filesviewer.browser.data.implementation.DeviceFilesRepositoryImp
import com.alon.filesviewer.browser.domain.interfaces.AppPreferenceManager
import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun bindFilesRepository(repo: DeviceFilesRepositoryImp): DeviceFilesRepository

    @Singleton
    @Binds
    abstract fun bindAppPreferenceManager(manager: AppPreferenceManagerImpl): AppPreferenceManager
}