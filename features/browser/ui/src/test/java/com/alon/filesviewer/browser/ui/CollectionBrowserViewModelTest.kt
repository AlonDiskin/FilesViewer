package com.alon.filesviewer.browser.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.viewmodel.BrowserUiPathBuilder
import com.alon.filesviewer.browser.ui.viewmodel.CollectionBrowserViewModel
import com.google.common.truth.Truth.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class CollectionBrowserViewModelTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: CollectionBrowserViewModel

    // Collaborators
    private val browseUseCase: BrowseDeviceFilesUseCase = mockk()
    private val pathBuilder: BrowserUiPathBuilder = mockk()
    private val stateHandle = SavedStateHandle()

    // Stub data
    private val browseUseCaseSubject = BehaviorSubject.create<Result<List<DeviceFile>>>()
    private val uiPath = "/collection_path"
    private val collection = DeviceFilesCollection.VIDEO.name

    @Before
    fun setUp() {
        every { browseUseCase.execute(any()) } returns browseUseCaseSubject
        every { pathBuilder.buildPath(any<DeviceFilesCollection>()) } returns uiPath
        stateHandle[CollectionBrowserViewModel.KEY_COLLECTION] = collection

        viewModel = CollectionBrowserViewModel(browseUseCase, pathBuilder, stateHandle)
    }

    @Test
    fun loadCollectionFiles_WhenCreated() {
        // Given

        // Then
        verify { browseUseCase.execute(BrowseRequest.Collection(DeviceFilesCollection.valueOf(collection))) }
    }

    @Test
    fun updateUiStateWithCollectionPath_WhenCreated() {
        // Given

        // Then
        assertThat(viewModel.uiState.value?.currentPath).isEqualTo(uiPath)
    }

    @Test
    fun updateUiState_WhenCollectionFilesLoaded() {
        // Given
        val files = mockk<List<DeviceFile>>()

        // When
        browseUseCaseSubject.onNext(Result.success(files))

        // Then
        assertThat(viewModel.uiState.value!!.files).isEqualTo(files)
    }

    @Test
    fun updateUiError_WhenFilesLoadingFail() {
        // Given
        val error = mockk<BrowserError>()

        // When
        browseUseCaseSubject.onNext(Result.failure(error))

        // Then
        assertThat(viewModel.uiState.value!!.error).isEqualTo(error)
    }
}