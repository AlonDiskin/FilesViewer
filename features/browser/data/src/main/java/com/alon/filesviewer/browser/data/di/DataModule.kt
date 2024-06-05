package com.alon.filesviewer.browser.data.di

import com.alon.filesviewer.browser.data.implementation.DeviceFilesRepositoryImp
import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindFilesRepository(repo: DeviceFilesRepositoryImp): DeviceFilesRepository
}