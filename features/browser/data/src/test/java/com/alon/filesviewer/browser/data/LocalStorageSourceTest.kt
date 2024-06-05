package com.alon.filesviewer.browser.data

import android.net.Uri
import com.alon.filesviewer.browser.data.local.FilesProvider
import com.alon.filesviewer.browser.data.local.LocalStorageDataSource
import com.alon.filesviewer.browser.data.local.MediaProvider
import com.alon.filesviewer.browser.data.local.PathProvider
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class LocalStorageSourceTest {

    // Test subject
    private lateinit var localStorageSource: LocalStorageDataSource

    // Collaborators
    private val filesProvider: FilesProvider = mockk()
    private val mediaProvider: MediaProvider = mockk()
    private val pathProvider: PathProvider = mockk()

    @Before
    fun setUp() {
        localStorageSource = LocalStorageDataSource(filesProvider, mediaProvider, pathProvider)
    }

    @Test
    fun searchAllLocalStorageFiles_WhenAllFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.ALL
        val rootPath = "root_path"
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getRootDirPath() } returns rootPath
        every { filesProvider.search(query,rootPath) } returns res

        // When
        val actualRes = localStorageSource.search(query, filter)

        // Then
        verify(exactly = 1) { pathProvider.getRootDirPath() }
        verify(exactly = 1) { filesProvider.search(query,rootPath) }
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchAudioMediaCollection_WhenAudioFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.AUDIO
        val audioUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getAudioCollectionUri() } returns audioUri
        every { mediaProvider.search(query,audioUri) } returns res

        // When
        val actualRes = localStorageSource.search(query, filter)

        // Then
        verify(exactly = 1) { pathProvider.getAudioCollectionUri() }
        verify(exactly = 1) { mediaProvider.search(query,audioUri) }
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchVideoMediaCollection_WhenVideoFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.VIDEO
        val videoUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getVideoCollectionUri() } returns videoUri
        every { mediaProvider.search(query,videoUri) } returns res

        // When
        val actualRes = localStorageSource.search(query, filter)

        // Then
        verify(exactly = 1) { pathProvider.getVideoCollectionUri() }
        verify(exactly = 1) { mediaProvider.search(query,videoUri) }
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchImageMediaCollection_WhenImageFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.IMAGE
        val imageUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getImageCollectionUri() } returns imageUri
        every { mediaProvider.search(query,imageUri) } returns res

        // When
        val actualRes = localStorageSource.search(query, filter)

        // Then
        verify(exactly = 1) { pathProvider.getImageCollectionUri() }
        verify(exactly = 1) { mediaProvider.search(query,imageUri) }
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchLocalStorageDownloadedFiles_WhenDownloadedFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.DOWNLOAD
        val dirPath = "dir_path"
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getDownloadsDirPath() } returns dirPath
        every { filesProvider.search(query,dirPath) } returns res

        // When
        val actualRes = localStorageSource.search(query, filter)

        // Then
        verify(exactly = 1) { pathProvider.getDownloadsDirPath() }
        verify(exactly = 1) { filesProvider.search(query,dirPath) }
        assertThat(actualRes).isEqualTo(res)
    }
}