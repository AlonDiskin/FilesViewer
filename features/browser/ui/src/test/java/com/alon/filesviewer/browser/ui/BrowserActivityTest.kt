package com.alon.filesviewer.browser.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.ui.RecyclerViewMatcher.withRecyclerView
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.alon.filesviewer.browser.ui.controller.FilesAdapter.FileViewHolder
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.alon.filesviewer.browser.ui.data.BrowserUiState
import com.alon.filesviewer.browser.ui.viewmodel.BrowserViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog
import java.io.File

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class BrowserActivityTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // System under test
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    // Collaborators
    private val viewModel = mockk<BrowserViewModel>()

    // Stub data
    private val latestUiState = BrowserUiState()
    private val uiState = MutableLiveData(latestUiState)

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<BrowserViewModel>>().value } returns viewModel

        // Stub mocked view model
        every { viewModel.uiState } returns uiState

        // Stub storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mockkStatic(Environment::class)
            every { Environment.isExternalStorageManager() } returns true
        } else {
            mockkStatic(ContextCompat::class)
            every {
                ContextCompat.checkSelfPermission(
                    any(), // activity context not yet available
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } returns PackageManager.PERMISSION_GRANTED
        }

        // Launch activity under test
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun openSearchScreen_WhenUserSelectSearch() {
        // Given
        Intents.init()

        // When
        scenario.onActivity {
            it.onOptionsItemSelected(RoboMenuItem(R.id.action_nav_search))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasComponent(SearchActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun openStoragePermissionInDeviceSetting_WhenNotGrantedYet() {
        // Given
        Intents.init()
        every { Environment.isExternalStorageManager() } returns false

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Intents.getIntents().size).isEqualTo(1)
        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION))

        Intents.release()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun showStoragePermissionRequestUi_WhenCreatedAndPermissionIsNotGrantedApi24() {
        // Given
        Intents.init()
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Intents.getIntents().size).isEqualTo(1)
        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))

        Intents.release()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun exitApp_WhenUserDeclineStoragePermissionApi24() {
        // Given

        // When
        scenario.onActivity {
            val permissionCallback: ActivityResultCallback<Boolean> = WhiteBox.getInternalState(it,"permissionRequestCallback") as ActivityResultCallback<Boolean>
            permissionCallback.onActivityResult(false)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun resumeUi_WhenUserAcceptStoragePermissionApi24() {
        // Given

        // When
        scenario.onActivity {
            val permissionCallback: ActivityResultCallback<Boolean> = WhiteBox.getInternalState(it,"permissionRequestCallback") as ActivityResultCallback<Boolean>
            permissionCallback.onActivityResult(true)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isFalse()
        }
        assertThat(scenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    fun exitApp_WhenUserReturnsFromSettingsAndPermissionNotGranted() {
        // Given
        every { Environment.isExternalStorageManager() } returns false

        // When
        scenario.onActivity {
            val resultCallback: ActivityResultCallback<ActivityResult> = WhiteBox.getInternalState(it,"settingsForResultCallback") as ActivityResultCallback<ActivityResult>
            resultCallback.onActivityResult(ActivityResult(0,null))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    fun resumeUi_WhenUserReturnsFromSettingsPermissionGranted() {
        // Given

        // When
        scenario.onActivity {
            val resultCallback: ActivityResultCallback<ActivityResult> = WhiteBox.getInternalState(it,"settingsForResultCallback") as ActivityResultCallback<ActivityResult>
            resultCallback.onActivityResult(ActivityResult(0,null))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isFalse()
        }
        assertThat(scenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun resumeUi_WhenUserReturnsFromSettingsPermissionGrantedApi24() {
        // Given

        // When
        scenario.onActivity {
            val resultCallback: ActivityResultCallback<ActivityResult> = WhiteBox.getInternalState(it,"settingsForResultCallback") as ActivityResultCallback<ActivityResult>
            resultCallback.onActivityResult(ActivityResult(0,null))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isFalse()
        }
        assertThat(scenario.state).isEqualTo(Lifecycle.State.RESUMED)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun exitApp_WhenUserReturnsFromSettingsAndPermissionNotGrantedApi24() {
        // Given
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED


        // When
        scenario.onActivity {
            val resultCallback: ActivityResultCallback<ActivityResult> = WhiteBox.getInternalState(it,"settingsForResultCallback") as ActivityResultCallback<ActivityResult>
            resultCallback.onActivityResult(ActivityResult(0,null))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    fun browseRootDir_WhenCreated() {
        // Given

        // Then
        onView(withId(R.id.bottom_navigation))
            .check(matches(withBottomNavSelected(R.id.nav_all)))
    }

    @Test
    fun showBrowsedFiles_WhenLoaded() {
        // Given
        val files = listOf(
            DeviceFile("path_1","name_1",DeviceFileType.IMAGE, 20L,"mime_1",20L),
            DeviceFile("path_2","name_2",DeviceFileType.VIDEO, 20L,"mime_2",20L),
            DeviceFile("path_3","name_3",DeviceFileType.AUDIO, 20L,"mime_3",20L)
        )

        // When
        uiState.value = BrowserUiState(files = files)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        // Then
        onView(withId(R.id.browsedFiles))
            .check(matches(withRecyclerViewSize(files.size)))
        files.forEachIndexed { index, fileUiState ->
            onView(withId(R.id.browsedFiles))
                .perform(
                    scrollToPosition<FileViewHolder>(
                        index
                    )
                )

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.browsedFiles).atPosition(index))
                .check(
                    matches(
                        hasDescendant(
                            allOf(
                                withId(R.id.fileName),
                                withText(fileUiState.name)
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun loadRootDir_WhenSelectedToBrowse() {
        // Given
        val category = BrowsedCategory.ALL

        every { viewModel.browseCategory(any()) } returns Unit
        onView(withId(R.id.nav_audio))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.nav_all))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.browseCategory(category) }
        onView(withId(R.id.bottom_navigation))
            .check(matches(withBottomNavSelected(R.id.nav_all)))
    }

    @Test
    fun loadAudioFiles_WhenSelectedToBrowse() {
        // Given
        val category = BrowsedCategory.AUDIO

        every { viewModel.browseCategory(any()) } returns Unit

        // When
        onView(withId(R.id.nav_audio))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.browseCategory(category) }
        onView(withId(R.id.bottom_navigation))
            .check(matches(withBottomNavSelected(R.id.nav_audio)))
    }

    @Test
    fun loadVideoFiles_WhenSelectedToBrowse() {
        // Given
        val category = BrowsedCategory.VIDEO

        every { viewModel.browseCategory(any()) } returns Unit

        // When
        onView(withId(R.id.nav_video))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.browseCategory(category) }
        onView(withId(R.id.bottom_navigation))
            .check(matches(withBottomNavSelected(R.id.nav_video)))
    }

    @Test
    fun loadImageFiles_WhenSelectedToBrowse() {
        // Given
        val category = BrowsedCategory.IMAGE

        every { viewModel.browseCategory(any()) } returns Unit

        // When
        onView(withId(R.id.nav_image))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.browseCategory(category) }
        onView(withId(R.id.bottom_navigation))
            .check(matches(withBottomNavSelected(R.id.nav_image)))
    }

    @Test
    fun loadDownloadedFiles_WhenSelectedToBrowse() {
        // Given
        val category = BrowsedCategory.DOWNLOADS

        every { viewModel.browseCategory(any()) } returns Unit

        // When
        onView(withId(R.id.nav_download))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.browseCategory(category) }
        onView(withId(R.id.bottom_navigation))
            .check(matches(withBottomNavSelected(R.id.nav_download)))
    }

    @Test
    fun showCurrentBrowsedDirPath_WhenFilesBrowsed() {
        // Given
        val stateUpdate = BrowserUiState(currentPath = "Home/Dir/InnerDir")

        // When
        uiState.value = stateUpdate
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.currentPathLabel))
            .check(matches(withText(stateUpdate.currentPath)))
    }

    @Test
    fun showParentFolderFiles_WhenPressingBackFromNonRootBrowsedFolder() {
        // Given
        every { viewModel.isRootFolder() } returns false
        every { viewModel.navUpFromFolder() } returns true

        // When
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.navUpFromFolder() }
    }

    @Test
    fun showExitAppConfirmation_WhenPressBackToExitApp() {
        // Given
        every { viewModel.isRootFolder() } returns true

        // When
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_exit_app))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun closeApp_WhenUserSelectExitInAppExitConfirmationUi() {
        // Given
        every { viewModel.isRootFolder() } returns true

        // When
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
    }

    @Test
    fun showFolderFiles_WhenFolderSelected() {
        val files = listOf(
            DeviceFile("path_1","name_1",DeviceFileType.DIR, 20L,"mime_1",20L)
        )
        uiState.value = BrowserUiState(files = files)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        every { viewModel.browseFolder(any()) } returns Unit

        // When
        onView(withText(files.first().name))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.browseFolder(files.first().path) }
    }

    @Test
    fun showFileDetail_WhenUserSelectToViewDetail() {
        // Given
        val file = DeviceFile(
            "path",
            "name",
            DeviceFileType.DIR,
            FileUtils.ONE_MB,
            "mime",
            10000L
        )
        val size = FileUtils.byteCountToDisplaySize(file.size)
        uiState.value = BrowserUiState(files = listOf(file))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)

        // When
        onView(withId(R.id.fileDetail))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.title_action_detail))
            .inRoot(isPlatformPopup())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_file_detail))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(
            ApplicationProvider.getApplicationContext<Context>()
            .getString(
                R.string.file_detail,
                file.name,
                file.path,
                size))
        )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun openFileViaAppChooser_WhenUserSelectToOpenNonFolderFile() {
        // Given
        val files = listOf(
            DeviceFile("path_1","name_1",DeviceFileType.AUDIO, 20L,"mp3",20L)
        )
        uiState.value = BrowserUiState(files = files)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)
        Intents.init()

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(),any(), File(files.first().path)) } returns Uri.EMPTY

        // When
        onView(withText(files.first().name))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Intents.release()
    }

    @Test
    fun showErrorMessage_WhenBrowserFeatureFail() {
        // Given
        val error = BrowserError.Internal("")

        // When
        uiState.value = BrowserUiState(error = error)

        // Then
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.error_message_browser_feature)
        ))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun showError_WhenBrowsedFolderAccessDenied() {
        // Given
        val error = BrowserError.AccessDenied("")

        // When
        uiState.value = BrowserUiState(error = error)

        // Then
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.error_message_access_denied)
        ))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun showErrorMessage_WhenTryToBrowseNonExistingFolder() {
        // Given
        val error = BrowserError.NonExistingDir("")

        // When
        uiState.value = BrowserUiState(error = error)

        // Then
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.error_message_dir_non_exist)
        ))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}