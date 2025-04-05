package com.alon.filesviewer.browser.featuretesting.browser

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.data.local.LocalFilesRepository
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.subjects.BehaviorSubject
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog

class BrowserErrorHandledSteps(filesRepo: LocalFilesRepository) : GreenCoffeeSteps() {

    // Browser feature ui
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    // Test data
    private val errorSubject = BehaviorSubject.create<Result<List<DeviceFile>>>()

    init {
        // Stub mocked data source
        //every { localDataSource.getCollectionFiles(any()) } returns errorSubject
        every { filesRepo.getFolderFiles(any(),any()) } returns errorSubject

        // Stub storage permission as granted
        mockkStatic(Environment::class)
        mockkStatic(FileProvider::class)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

        // Create test device root folder
        val testRootFolder = TemporaryFolder()
        testRootFolder.create()

        every { Environment.getExternalStorageDirectory() } returns testRootFolder.root
    }

    @Given("^user has opened browser screen$")
    fun userOpenedBrowserScreen() {
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^feature fail due to \"([^\"]*)\"$")
    fun browserFeatureFail(error: String) {
        when(error) {
            "path not recognized" -> errorSubject.onNext(
                Result.failure(BrowserError.NonExistingDir(""))
            )
            "internal feature fail" -> errorSubject.onNext(
                Result.failure(BrowserError.Internal(""))
            )
            "access restricted" -> errorSubject.onNext(
                Result.failure(BrowserError.AccessDenied(""))
            )
            else -> throw IllegalArgumentException("Unknown test arg:$error")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^browser should \"([^\"]*)\"$")
    fun browserErrorHandle(expectedHandle: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val errorMessage = when(expectedHandle) {
            "show dir not exist message" -> context.getString(R.string.error_message_dir_non_exist)
            "show feature fail  message" -> context.getString(R.string.error_message_browser_feature)
            "show show access restricted message" -> context.getString(R.string.error_message_access_denied)
            else -> throw IllegalArgumentException("Unknown test arg:$expectedHandle")
        }

        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_error))
            .inRoot(RootMatchers.isDialog())
            .check(matches(ViewMatchers.isDisplayed()))
        onView(withText(errorMessage))
            .inRoot(RootMatchers.isDialog())
            .check(matches(ViewMatchers.isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(RootMatchers.isDialog())
            .check(matches(ViewMatchers.isDisplayed()))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}