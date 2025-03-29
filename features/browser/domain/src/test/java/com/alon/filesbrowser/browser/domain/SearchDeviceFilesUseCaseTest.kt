package com.alon.filesbrowser.browser.domain

import com.alon.filesviewer.browser.domain.interfaces.AppPreferenceManager
import com.alon.filesviewer.browser.domain.interfaces.DeviceFilesRepository
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.domain.model.SearchRequest
import com.alon.filesviewer.browser.domain.usecase.SearchDeviceFilesUseCase
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
    private val prefManager: AppPreferenceManager = mockk()

    @Before
    fun setUp() {
        useCase = SearchDeviceFilesUseCase(deviceRepo,prefManager)
    }

    @Test
    fun searchDeviceFiles_WhenExecuted() {
        // Given
        val searchResults = Result.success<List<DeviceFile>>(emptyList())
        val searchRequest = SearchRequest("query",SearchFilter.FILES)
        val hiddenEnabled = true

        every { prefManager.isHiddenFilesShowingEnabled() } returns Observable.just(hiddenEnabled)
        every { deviceRepo.search(any(),any(),any()) } returns Observable.just(searchResults)

        // When
        val useCaseResult = useCase.execute(searchRequest).test()

        // Then
        verify(exactly = 1) { deviceRepo.search(searchRequest.query,searchRequest.filter,hiddenEnabled) }
        useCaseResult.assertValue(searchResults)
    }
}