package com.alon.filesviewer.browser.data

import android.net.Uri
import com.alon.filesviewer.browser.data.implementation.DeviceFilesRepositoryImp
import com.alon.filesviewer.browser.data.local.LocalFilesRepository
import com.alon.filesviewer.browser.data.local.LocalMediaRepository
import com.alon.filesviewer.browser.data.local.PathProvider
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class DeviceFilesRepositoryImpTest {

    // Test subject
    private lateinit var repository: DeviceFilesRepositoryImp

    // Collaborators
    private val filesRepo: LocalFilesRepository = mockk()
    private val mediaRepo: LocalMediaRepository = mockk()
    private val pathProvider: PathProvider = mockk()

    @Before
    fun setUp() {
        repository = DeviceFilesRepositoryImp(filesRepo, mediaRepo, pathProvider)
    }

    @Test
    fun searchAllLocalStorageFiles_WhenAllFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.FILES
        val rootPath = "root_path"
        val includeHidden = true
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getRootDirPath() } returns rootPath
        every { filesRepo.search(query,rootPath, includeHidden) } returns res

        // When
        val actualRes = repository.search(query,filter,includeHidden)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchLocalStorageDownloadedFiles_WhenDownloadedFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.DOWNLOAD
        val dirPath = "dir_path"
        val includeHidden = true
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getDownloadsDirPath() } returns dirPath
        every { filesRepo.search(query,dirPath,includeHidden) } returns res

        // When
        val actualRes = repository.search(query,filter,includeHidden)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchLocalStorageAudioFiles_WhenAudioFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.AUDIO
        val audioUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getAudioCollectionUri() } returns audioUri
        every { mediaRepo.search(query,audioUri) } returns res

        // When
        val actualRes = repository.search(query, filter,true)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchLocalStorageVideoMediaCollection_WhenVideoFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.VIDEO
        val videoUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getVideoCollectionUri() } returns videoUri
        every { mediaRepo.search(query,videoUri) } returns res

        // When
        val actualRes = repository.search(query, filter, true)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun searchLocalStorageImageMediaCollection_WhenImageFilesSearched() {
        // Given
        val query = "query"
        val filter = SearchFilter.IMAGE
        val imageUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getImageCollectionUri() } returns imageUri
        every { mediaRepo.search(query,imageUri) } returns res

        // When
        val actualRes = repository.search(query, filter, true)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadAllLocalStorageAudioFiles_WhenQueriedForAudioCollection() {
        // Given
        val collection = DeviceFilesCollection.AUDIO
        val uri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getAudioCollectionUri() } returns uri
        every { mediaRepo.getAll(uri) } returns res

        // When
        val actualRes = repository.getCollection(collection)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadAllLocalStorageImageFiles_WhenQueriedForAudioCollection() {
        // Given
        val collection = DeviceFilesCollection.IMAGE
        val uri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getImageCollectionUri() } returns uri
        every { mediaRepo.getAll(uri) } returns res

        // When
        val actualRes = repository.getCollection(collection)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadAllLocalStorageVideoFiles_WhenQueriedForAudioCollection() {
        // Given
        val collection = DeviceFilesCollection.VIDEO
        val uri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { pathProvider.getVideoCollectionUri() } returns uri
        every { mediaRepo.getAll(uri) } returns res

        // When
        val actualRes = repository.getCollection(collection)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadLocalStorageFolderFiles_WhenQueriedForFolderFiles() {
        // Given
        val path = "path"
        val includeHidden = true
        val res = mockk<Observable<Result<List<DeviceFile>>>>()

        every { filesRepo.getFolderFiles(path,includeHidden) } returns res

        // When
        val actualRes = repository.getFolder(path,includeHidden)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }
}