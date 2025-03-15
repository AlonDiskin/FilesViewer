package com.alon.filesviewer.login.ui

import android.Manifest
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
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class)
class LoginActivityTest {

    // Testing rules
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // System under test
    private lateinit var scenario: ActivityScenario<LoginActivity>

    // Collaborators
    @BindValue @JvmField
    val navigator: LoginNavigator = mockk()

    // Stub data
    private val homeIntent: Intent = Intent("launch home")

    @Before
    fun setUp() {
        // Inject test collaborator
        hiltRule.inject()
        // Start intents sending recording
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun openStoragePermissionInDeviceSetting_WhenPermissionWasNotYetGrantedApi30() {
        // Given
        mockkStatic(Environment::class)
        every { Environment.isExternalStorageManager() } returns false

        // When
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Intents.getIntents().size).isEqualTo(1)
        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun showStoragePermissionRequestUi_WhenCreatedAndPermissionIsNotGrantedApi24() {
        // Given
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        // When
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Intents.getIntents().size).isEqualTo(1)
        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun exitApp_WhenUserDeclineStoragePermissionApi24() {
        // Given
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

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
        verify(exactly = 0) { navigator.getHomeScreenIntent() }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun launchHomeScreen_WhenUserAcceptStoragePermissionApi24() {
        // Given
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        every { navigator.getHomeScreenIntent() } returns homeIntent

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            val permissionCallback: ActivityResultCallback<Boolean> = WhiteBox.getInternalState(it,"permissionRequestCallback") as ActivityResultCallback<Boolean>
            permissionCallback.onActivityResult(true)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity { assertThat(it.isFinishing).isTrue() }
        Intents.intended(IntentMatchers.hasAction(homeIntent.action))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun exitApp_WhenUserReturnsFromSettingsAndPermissionNotGrantedApi30() {
        // Given
        mockkStatic(Environment::class)
        every { Environment.isExternalStorageManager() } returns false

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

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
        verify(exactly = 0) { navigator.getHomeScreenIntent() }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun launchHomeScreen_WhenUserReturnsFromSettingsPermissionGrantedApi30() {
        // Given
        mockkStatic(Environment::class)
        every { Environment.isExternalStorageManager() } returns false
        every { navigator.getHomeScreenIntent() } returns homeIntent

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        every { Environment.isExternalStorageManager() } returns true
        scenario.onActivity {
            val resultCallback: ActivityResultCallback<ActivityResult> = WhiteBox.getInternalState(it,"settingsForResultCallback") as ActivityResultCallback<ActivityResult>
            resultCallback.onActivityResult(ActivityResult(0,null))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
        Intents.intended(IntentMatchers.hasAction(homeIntent.action))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun launchHomeScreen_WhenUserReturnsFromSettingsPermissionGrantedApi24() {
        // Given
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        every { navigator.getHomeScreenIntent() } returns homeIntent

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_GRANTED
        scenario.onActivity {
            val resultCallback: ActivityResultCallback<ActivityResult> = WhiteBox.getInternalState(it,"settingsForResultCallback") as ActivityResultCallback<ActivityResult>
            resultCallback.onActivityResult(ActivityResult(0,null))
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            assertThat(it.isFinishing).isTrue()
        }
        Intents.intended(IntentMatchers.hasAction(homeIntent.action))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun exitApp_WhenUserReturnsFromSettingsAndPermissionNotGrantedApi24() {
        // Given
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                any(), // activity context not yet available
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } returns PackageManager.PERMISSION_DENIED

        scenario = ActivityScenario.launch(LoginActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

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
        verify(exactly = 0) { navigator.getHomeScreenIntent() }
    }
}