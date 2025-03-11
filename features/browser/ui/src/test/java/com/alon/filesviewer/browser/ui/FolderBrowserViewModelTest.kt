package com.alon.filesviewer.browser.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.alon.filesviewer.browser.domain.model.BrowseRequest
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceNamedFolder
import com.alon.filesviewer.browser.domain.usecase.BrowseDeviceFilesUseCase
import com.alon.filesviewer.browser.ui.util.WhiteBox
import com.alon.filesviewer.browser.ui.viewmodel.BrowserUiPathBuilder
import com.alon.filesviewer.browser.ui.viewmodel.DeviceFolderPathProvider
import com.alon.filesviewer.browser.ui.viewmodel.FolderBrowserViewModel
import com.google.common.truth.Truth.assertThat
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

class FolderBrowserViewModelTest {

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
    private lateinit var viewModel: FolderBrowserViewModel

    // Collaborators
    private val browseUseCase: BrowseDeviceFilesUseCase = mockk()
    private val pathBuilder: BrowserUiPathBuilder = mockk()
    private val stateHandle = SavedStateHandle()
    private val folderPathProvider: DeviceFolderPathProvider = mockk()

    // Stub data
    private val browseUseCaseSubject = BehaviorSubject.create<Result<List<DeviceFile>>>()
    private val uiPath = "/home_path"
    private val initialBaseFolder = DeviceNamedFolder.DOWNLOAD.name
    private val rootPath = "root"
    private val downloadPath = "download"
    private val initialFolderPath = downloadPath

    @Before
    fun setUp() {
        every { browseUseCase.execute(any()) } returns browseUseCaseSubject
        every { pathBuilder.buildPath(any<String>()) } returns uiPath
        every { folderPathProvider.getRootDirPath() } returns rootPath
        every { folderPathProvider.getDownloadsDirPath() } returns downloadPath
        stateHandle[FolderBrowserViewModel.KEY_BASE_FOLDER] = initialBaseFolder

        viewModel = FolderBrowserViewModel(browseUseCase, pathBuilder, folderPathProvider, stateHandle)
    }

    @Test
    fun loadDeviceRootFiles_WhenCreatedWithoutBaseFolderArg() {
        // Given
        stateHandle[FolderBrowserViewModel.KEY_BASE_FOLDER] = null
        viewModel = FolderBrowserViewModel(browseUseCase, pathBuilder, folderPathProvider, stateHandle)

        // Then
        verify { browseUseCase.execute(BrowseRequest.Folder(rootPath)) }
    }

    @Test
    fun loadBaseFolderFiles_WhenCreatedWithBaseFolderArgOnly() {
        // Given

        // Then
        verify { browseUseCase.execute(BrowseRequest.Folder(initialFolderPath)) }
    }

    @Test
    fun loadDestinationFolderFiles_WhenCreatedWithDestination() {
        // Given
        val destFolderPath = "dest_path"
        stateHandle[FolderBrowserViewModel.KEY_DEST_FOLDER] = destFolderPath

        // When
        viewModel = FolderBrowserViewModel(browseUseCase, pathBuilder, folderPathProvider, stateHandle)

        // Then
        verify { browseUseCase.execute(BrowseRequest.Folder(destFolderPath)) }
    }

    @Test
    fun loadFolderFiles_WhenNavigatingToFolder() {
        // Give
        val folderPath = "folder_path"

        // When
        viewModel.navToFolder(folderPath)

        // Then
        verify { browseUseCase.execute(BrowseRequest.Folder(folderPath)) }
    }

    @Test
    fun loadFolderParent_WhenNavigatingUpFromNonBaseFolder() {
        // Given
        val currentFolder = initialFolderPath.plus("/current")
        val subject = WhiteBox.getInternalState(viewModel,"browseSubject") as BehaviorSubject<BrowseRequest.Folder>

        subject.onNext(BrowseRequest.Folder(currentFolder))

        // When
        viewModel.navUpFromFolder()

        // Then
        verify(exactly = 2) { browseUseCase.execute(BrowseRequest.Folder(initialFolderPath)) }
    }

    @Test
    fun doNotLoadFolderParent_WhenNavigatingUpFromBaseFolder() {
        // Given

        // When
        viewModel.navUpFromFolder()

        // Then
        verify(exactly = 1) { browseUseCase.execute(BrowseRequest.Folder(downloadPath)) }
    }

    @Test
    fun updateUiStateFolderFile_WhenFolderFilesLoaded() {
        // Given
        val files = mockk<List<DeviceFile>>()

        // When
        browseUseCaseSubject.onNext(Result.success(files))

        // Then
        assertThat(viewModel.uiState.value!!.files).isEqualTo(files)
    }

    @Test
    fun updateUiStateFolderPath_WhenLoadingFolder() {
        // Given
        val folderPath = "folder_path"
        val expectedUiPath = "folder_ui_path"

        every { pathBuilder.buildPath(folderPath) } returns expectedUiPath

        // When
        viewModel.navToFolder(folderPath)

        // Then
        assertThat(viewModel.uiState.value!!.currentPath).isEqualTo(expectedUiPath)
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