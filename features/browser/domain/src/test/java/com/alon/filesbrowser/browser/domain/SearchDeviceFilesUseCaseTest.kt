package com.alon.filesbrowser.browser.domain

import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.domain.model.SearchRequest
import com.alon.filesviewer.browser.domain.usecase.SearchDeviceFilesUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class SearchDeviceFilesUseCaseTest {

    // Test subject
    private lateinit var useCase: SearchDeviceFilesUseCase

    // Collaborators
    private val deviceRepo: DeviceFilesRepository = mockk()

    @Before
    fun setUp() {
        useCase = SearchDeviceFilesUseCase(deviceRepo)
    }

    @Test
    fun searchDeviceFiles_WhenExecuted() {
        // Given
        val searchResults: Observable<Result<List<DeviceFile>>> = mockk()
        val searchRequest = SearchRequest("query",SearchFilter.FILES)

        every { deviceRepo.search(any(),any()) } returns searchResults

        // When
        val useCaseResult = useCase.execute(searchRequest)

        // Then
        verify(exactly = 1) { deviceRepo.search(searchRequest.query,searchRequest.filter) }
        assertThat(useCaseResult).isEqualTo(searchResults)
    }
}