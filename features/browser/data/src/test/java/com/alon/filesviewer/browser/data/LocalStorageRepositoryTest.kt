package com.alon.filesviewer.browser.data

import android.net.Uri
import com.alon.filesviewer.browser.data.local.LocalFilesRepository
import com.alon.filesviewer.browser.data.local.LocalStorageRepository
import com.alon.filesviewer.browser.data.local.LocalMediaRepository
import com.alon.filesviewer.browser.data.local.PathProvider
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class LocalStorageRepositoryTest {

    // Test subject
    private lateinit var localStorageRepo: LocalStorageRepository

    // Collaborators
    private val filesProvider: LocalFilesRepository = mockk()
    private val mediaProvider: LocalMediaRepository = mockk()
    private val pathProvider: PathProvider = mockk()

    @Before
    fun setUp() {
        localStorageRepo = LocalStorageRepository(filesProvider, mediaProvider, pathProvider)
    }

    @Test
    fun searchAllLocalStorageFiles_WhenAllFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.FILES
        val rootPath = "root_path"
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getRootDirPath() } returns rootPath
        every { filesProvider.search(query,rootPath) } returns res

        // When
        val actualRes = localStorageRepo.search(query, filter)

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
        val actualRes = localStorageRepo.search(query, filter)

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
        val actualRes = localStorageRepo.search(query, filter)

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
        val actualRes = localStorageRepo.search(query, filter)

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
        val actualRes = localStorageRepo.search(query, filter)

        // Then
        verify(exactly = 1) { pathProvider.getDownloadsDirPath() }
        verify(exactly = 1) { filesProvider.search(query,dirPath) }
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadRootFolder_WhenQueriedForAllFilesCategory() {
        // Given
        val category = BrowsedCategory.ALL
        val rootPath = "path"
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getRootDirPath() } returns rootPath
        every { filesProvider.getFolderFiles(rootPath) } returns res

        // When
        val actualRes = localStorageRepo.getCategoryFiles(category)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadDownloadFolder_WhenQueriedForDownloadCategory() {
        // Given
        val category = BrowsedCategory.DOWNLOADS
        val downloadPath = "path"
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getDownloadsDirPath() } returns downloadPath
        every { filesProvider.getFolderFiles(downloadPath) } returns res

        // When
        val actualRes = localStorageRepo.getCategoryFiles(category)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadAudioFiles_WhenQueriedForAudioCategory() {
        // Given
        val category = BrowsedCategory.AUDIO
        val uri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getAudioCollectionUri() } returns uri
        every { mediaProvider.getAll(uri) } returns res

        // When
        val actualRes = localStorageRepo.getCategoryFiles(category)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadImageFiles_WhenQueriedForImageCategory() {
        // Given
        val category = BrowsedCategory.IMAGE
        val uri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getImageCollectionUri() } returns uri
        every { mediaProvider.getAll(uri) } returns res

        // When
        val actualRes = localStorageRepo.getCategoryFiles(category)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadVideoFiles_WhenQueriedForVideoCategory() {
        // Given
        val category = BrowsedCategory.VIDEO
        val uri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getVideoCollectionUri() } returns uri
        every { mediaProvider.getAll(uri) } returns res

        // When
        val actualRes = localStorageRepo.getCategoryFiles(category)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadFolderFiles_WhenQueriedForFolder() {
        // Given
        val path = "path"
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { filesProvider.getFolderFiles(path) } returns res

        // When
        val actualRes = localStorageRepo.getFolderFiles(path)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }
}