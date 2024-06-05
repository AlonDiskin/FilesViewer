package com.alon.filesviewer.browser.featuretesting.di

import android.content.ContentResolver
import android.net.Uri
import com.alon.filesviewer.browser.featuretesting.util.FakeMediaContentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideContentResolver(mediaContentProvider: FakeMediaContentProvider): ContentResolver {
        val cr = mockk<ContentResolver>()

        every { cr.registerContentObserver(any(),any(),any()) } returns Unit
        every { cr.unregisterContentObserver(any()) } returns Unit
        every { cr.query(any(),any(),any(),any(),any()) } answers {
            val uri: Uri = this.args[0] as Uri
            val proj: Array<out String>? = this.args[1] as Array<out String>?
            val selection: String? = this.args[2] as String?
            val selectionArgs: Array<out String>? = this.args[3] as Array<out String>?
            val sortOrder: String? = this.args[4] as String?

            mediaContentProvider.query(uri,proj,selection, selectionArgs, sortOrder)
        }
        return cr
    }
}