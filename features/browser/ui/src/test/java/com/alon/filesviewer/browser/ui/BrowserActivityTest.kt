package com.alon.filesviewer.browser.ui

import android.Manifest
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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.alon.filesviewer.browser.ui.controller.SearchActivity
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.fakes.RoboMenuItem

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
}