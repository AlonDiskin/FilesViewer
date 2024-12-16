package com.alon.filesbrowser.browser.domain

import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
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
    fun fetchDeviceCategoryFiles_WhenExecutedToBrowseCategory() {
        // Given
        val files: Observable<Result<List<DeviceFile>>> = mockk()
        val category = BrowsedCategory.ALL
        val request = BrowseRequest.Category(category)

        every { repo.getByCategory(category) } returns files

        // When
        val result = useCase.execute(request)

        // Then
        verify(exactly = 1) { repo.getByCategory(category) }
        assertThat(result).isEqualTo(files)
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