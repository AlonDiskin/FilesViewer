package com.alon.filesbrowser.browser.domain

import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class BrowseFilesUseCaseTest {

    // Test subject
    private lateinit var useCase: BrowseDeviceFilesUseCase

    // Collaborators
    private val repo: DeviceFilesRepository = mockk()

    @Before
    fun setUp() {
        useCase = BrowseDeviceFilesUseCase(repo)
    }

    @Test
    fun fetchDeviceCollectionFiles_WhenExecutedToBrowseCollection() {
        // Given
        val filesCollection: Observable<Result<List<DeviceFile>>> = mockk()
        val requestCollection = mockk<DeviceFilesCollection>()
        val request = BrowseRequest.Collection(requestCollection)

        every { repo.getCollection(requestCollection) } returns filesCollection

        // When
        val result = useCase.execute(request)

        // Then
        verify(exactly = 1) { repo.getCollection(requestCollection) }
        assertThat(result).isEqualTo(filesCollection)
    }

    @Test
    fun fetchDeviceFolderFiles_WhenExecutedToBrowseFolder() {
        // Given
        val files: Observable<Result<List<DeviceFile>>> = mockk()
        val request = BrowseRequest.Folder("path")

        every { repo.getFolder(any()) } returns files

        // When
        val result = useCase.execute(request)

        // Then
        verify(exactly = 1) { repo.getFolder(request.path) }
        assertThat(result).isEqualTo(files)
    }
}