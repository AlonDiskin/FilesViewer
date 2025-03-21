package com.alon.filesviewer.browser.data

import com.alon.filesviewer.browser.data.implementation.DeviceFilesRepositoryImp
import com.alon.filesviewer.browser.data.local.LocalStorageRepository
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class DeviceFilesRepositoryImpTest {

    // Test subject
    private lateinit var repository: DeviceFilesRepositoryImp

    // Collaborators
    private val localStorageDataSource: LocalStorageRepository = mockk()

    @Before
    fun setUp() {
        repository = DeviceFilesRepositoryImp(localStorageDataSource)
    }

    @Test
    fun searchLocalStorage_WhenFilesSearched() {
        // Given
        val query = "query"
        val filter = mockk<SearchFilter>()
        val res= mockk<Observable<Result<List<DeviceFile>>>>()

        every { localStorageDataSource.search(query, filter) } returns res

        // When
        val actualRes = repository.search(query, filter)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadFolderFromLocalStorage_WhenQueried() {
        // Given
        val path = "path"
        val res= mockk<Observable<Result<List<DeviceFile>>>>()

        every { localStorageDataSource.getFolderFiles(path) } returns res

        // When
        val actualRes = repository.getFolder(path)

        // Then
        assertThat(actualRes).isEqualTo(res)
    }

    @Test
    fun loadCollectionFilesFromLocalStorage_WhenQueried() {
        // Given
        val collection = mockk<DeviceFilesCollection>()
        val res= mockk<Observable<Result<List<DeviceFile>>>>()

        every { localStorageDataSource.getCollectionFiles(collection) } returns res

        // When
        val actualRes = repository.getCollection(collection)

        // Then
        verify(exactly = 1) { repository.getCollection(collection) }
        assertThat(actualRes).isEqualTo(res)
    }
}