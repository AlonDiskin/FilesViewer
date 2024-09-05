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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import org.apache.commons.io.FileUtils
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog
import java.io.File

class BrowsedFileDetailShownSteps : GreenCoffeeSteps() {

    // Browser feature ui
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    // Test data
    private val selectedRootFile: File

    init {
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
        selectedRootFile = testRootFolder.newFile("text.pdf")

        every { Environment.getExternalStorageDirectory() } returns testRootFolder.root
    }

    @Given("^user has opened browser screen$")
    fun userOpenedBrowserScreen() {
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he select to view file detail in root folder$")
    fun userSelectFileDetail() {
        onView(withId(R.id.fileDetail))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.title_action_detail))
            .inRoot(isPlatformPopup())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^browser should show file detail$")
    fun browserShowFileDetail() {
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_file_detail))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(
            ApplicationProvider.getApplicationContext<Context>()
                .getString(
                    R.string.file_detail,
                    selectedRootFile.name,
                    selectedRootFile.path,
                    FileUtils.byteCountToDisplaySize(selectedRootFile.length())))
        )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.button_dialog_positive))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}