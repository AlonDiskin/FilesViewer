package com.alon.filesviewer.browser.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.DeviceNamedFolder
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.alon.filesviewer.browser.ui.controller.BrowserFragmentsFactory
import com.alon.filesviewer.browser.ui.controller.BrowserNavigator
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.alon.filesviewer.browser.ui.util.TestFragment
import com.alon.filesviewer.browser.ui.util.WhiteBox
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class BrowserActivityTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // System under test
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    @Before
    fun setUp() {
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

        // Stub fragment factory
        mockkObject(BrowserFragmentsFactory)
        every { BrowserFragmentsFactory.createFolderBrowserFragment(any<DeviceNamedFolder>()) } returns TestFragment()

        // Launch activity under test
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
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
    fun showExitAppDialog_WhenPressBack() {
        // Given

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
    fun closeScreen_WhenUserConfirmAppExit() {
        // Given

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
    fun doNotCloseScreen_WhenUserDeclineAppExit() {
        // Given

        // When
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(R.string.button_dialog_negative))
            .inRoot(isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isFalse()
        }
    }

    @Test
    fun openSearchScreen_WhenUserSelectToSearchDeviceFile() {
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
    fun showDeviceFolder_WhenUserSelectToBrowseSearchedFolder() {
        // Given
        val dirPath = "dir_path"
        val result = ActivityResult(Activity.RESULT_OK,
            Intent().apply { putExtra(SearchActivity.RESULT_DIR_PATH,dirPath) })
        val testFragment = TestFragment()

        every { BrowserFragmentsFactory.createFolderBrowserFragment(dirPath) } returns testFragment

        // When
        scenario.onActivity { it.onSearchActivityResult(result) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isEqualTo(testFragment)
        }
    }

    @Test
    fun showDeviceRootFolderFilesAsDefault_WhenDisplayed() {
        // Given

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isInstanceOf(TestFragment::class.java)
        }
    }

    @Test
    fun showDeviceDownloadFolderFiles_WhenUserSelectToBrowseThem() {
        // Given
        val folderType = DeviceNamedFolder.DOWNLOAD
        val testFragment = TestFragment()

        every { BrowserFragmentsFactory.createFolderBrowserFragment(folderType) } returns testFragment

        // When
        onView(withId(R.id.nav_download))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isEqualTo(testFragment)
        }
    }

    @Test
    fun showDeviceRootFolderFiles_WhenUserSelectToBrowseThem() {
        // Given
        val folderType = DeviceNamedFolder.ROOT
        val testFragment = TestFragment()

        every { BrowserFragmentsFactory.createFolderBrowserFragment(folderType) } returns testFragment
        scenario.onActivity { activity ->
            val bottomNavMenu = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)

            bottomNavMenu.selectedItemId = R.id.nav_download
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.nav_all))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isEqualTo(testFragment)
        }
    }

    @Test
    fun showDeviceAudioFiles_WhenUserSelectToBrowseThem() {
        // Given
        val collectionType = DeviceFilesCollection.AUDIO
        val testFragment = TestFragment()

        every { BrowserFragmentsFactory.createCollectionBrowserFragment(collectionType) } returns testFragment

        // When
        onView(withId(R.id.nav_audio))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isEqualTo(testFragment)
        }
    }

    @Test
    fun showDeviceImageFiles_WhenUserSelectToBrowseThem() {
        // Given
        val collectionType = DeviceFilesCollection.IMAGE
        val testFragment = TestFragment()

        every { BrowserFragmentsFactory.createCollectionBrowserFragment(collectionType) } returns testFragment

        // When
        onView(withId(R.id.nav_image))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isEqualTo(testFragment)
        }
    }

    @Test
    fun showDeviceVideoFiles_WhenUserSelectToBrowseThem() {
        // Given
        val collectionType = DeviceFilesCollection.VIDEO
        val testFragment = TestFragment()

        every { BrowserFragmentsFactory.createCollectionBrowserFragment(collectionType) } returns testFragment

        // When
        onView(withId(R.id.nav_video))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { activity ->
            val currentFragment = activity.supportFragmentManager.fragments.first()
            assertThat(currentFragment).isEqualTo(testFragment)
        }
    }

    @Test
    fun navigateToSettingsScreen_WhenUserSelectSettingsFromMenu() {
        // Given
        val context: Context = ApplicationProvider.getApplicationContext()
        val navigator: BrowserNavigator = mockk()
        val settingsIntent = Intent("settings intent")

        Intents.init()
        every { navigator.getSettingsScreenIntent() } returns settingsIntent
        scenario.onActivity { WhiteBox.setInternalState(it,"navigator",navigator) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        openActionBarOverflowOrOptionsMenu(context)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(withText(context.getString(R.string.title_action_nav_settings)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasAction(settingsIntent.action))

        Intents.release()
    }
}