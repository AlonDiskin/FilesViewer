package com.alon.filesviewer.browser.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.data.BrowserUiState
import com.alon.filesviewer.browser.ui.viewmodel.BrowserRequestStack
import com.alon.filesviewer.browser.ui.viewmodel.BrowserUiPathBuilder
import com.alon.filesviewer.browser.ui.viewmodel.BrowserViewModel
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

class BrowserViewModelTest {

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
    private lateinit var viewModel: BrowserViewModel

    // Collaborators
    private val browseUseCase: BrowseDeviceFilesUseCase = mockk()
    private val pathBuilder: BrowserUiPathBuilder = mockk()
    private val requestStack: BrowserRequestStack = mockk()

    // Stub data
    private val browseUseCaseSubject = BehaviorSubject.create<Result<List<DeviceFile>>>()
    private val uiPath = "/home_path"

    @Before
    fun setUp() {
        every { browseUseCase.execute(any()) } returns browseUseCaseSubject
        every { requestStack.setRoot(any()) } returns Unit
        every { requestStack.setCurrent(any()) } returns Unit
        every { pathBuilder.buildPath(any<BrowsedCategory>()) } returns uiPath
        every { pathBuilder.buildPath(any<String>()) } returns uiPath

        viewModel = BrowserViewModel(browseUseCase,pathBuilder,requestStack)
    }

    @Test
    fun loadDeviceRootFolder_WhenCreated() {
        // Given

        // Then
        verify { browseUseCase.execute(BrowseRequest.Category(BrowsedCategory.ALL)) }
        verify { requestStack.setRoot(BrowseRequest.Category(BrowsedCategory.ALL)) }
    }

    @Test
    fun setUiBrowsePathAsDeviceHome_WhenCreated() {
        // Given

        // Then
        assertThat(viewModel.uiState.value!!.currentPath).isEqualTo(uiPath)
    }

    @Test
    fun updateUiFiles_WhenBrowsedFilesUpdated() {
        // Given
        val files = mockk<List<DeviceFile>>()

        // When
        browseUseCaseSubject.onNext(Result.success(files))

        // Then
        assertThat(viewModel.uiState.value!!.files).isEqualTo(files)
    }

    @Test
    fun updateUiError_WhenBrowsedFilesUpdatedFail() {
        // Given
        val error = mockk<BrowserError>()

        // When
        browseUseCaseSubject.onNext(Result.failure(error))

        // Then
        assertThat(viewModel.uiState.value!!.error).isEqualTo(error)
    }

    @Test
    fun updateUiFilesPath_WhenRequestToBrowseCategory() {
        // Given
        val category = mockk<BrowsedCategory>()
        val expectedUiPath = "root_path"

        every { pathBuilder.buildPath(category) } returns expectedUiPath

        // When
        viewModel.browseCategory(category)

        // Then
        assertThat(viewModel.uiState.value!!.currentPath).isEqualTo(expectedUiPath)
    }

    @Test
    fun updateUiFilesPath_WhenRequestToBrowseFolder() {
        // Given
        val folderPath = "folder_path"
        val expectedUiPath = "folder_ui_path"

        every { pathBuilder.buildPath(folderPath) } returns expectedUiPath

        // When
        viewModel.browseFolder(folderPath)

        // Then
        assertThat(viewModel.uiState.value!!.currentPath).isEqualTo(expectedUiPath)
    }

    @Test
    fun loadDeviceCategoryFiles_WhenViewRequestToBrowse() {
        // Given
        val category = mockk<BrowsedCategory>()

        // When
        viewModel.browseCategory(category)

        // Then
        verify { browseUseCase.execute(BrowseRequest.Category(category)) }
    }

    @Test
    fun loadDeviceFolderFiles_WhenViewRequestToBrowse() {
        // Given
        val path = "dir_path"

        // When
        viewModel.browseFolder(path)

        // Then
        verify { browseUseCase.execute(BrowseRequest.Folder(path)) }
    }

    @Test
    fun loadDeviceFolderParent_WhenViewRequestToNavUpFromFolder() {
        // Given
        val parent = BrowseRequest.Folder("parent_path")

        every { requestStack.isCurrentRoot() } returns false
        every { requestStack.popToParent() } returns parent

        // When
        viewModel.navUpFromFolder()

        // Then
        verify { browseUseCase.execute(parent) }
    }

    @Test
    fun resetUiError_WhenViewClearError() {
        // Given
        val errorState = BrowserUiState(error = mockk())
        val uiState = WhiteBox.getInternalState(viewModel,"_uiState") as MutableLiveData<BrowserUiState>

        uiState.value = errorState

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.uiState.value!!.error).isNull()
    }
}