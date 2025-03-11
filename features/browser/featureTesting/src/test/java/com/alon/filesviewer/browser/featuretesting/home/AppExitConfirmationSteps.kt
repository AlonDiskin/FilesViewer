package com.alon.filesviewer.browser.featuretesting.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.controller.BrowserActivity
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.rules.TemporaryFolder
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog

class AppExitConfirmationSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<BrowserActivity>

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

        every { Environment.getExternalStorageDirectory() } returns testRootFolder.root
    }

    @Given("^user opened app home screen$")
    fun userOpenAppHome() {
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he select to exit app$")
    fun userExitApp() {
        pressBack()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should show exit confirmation dialog$")
    fun showExitDialog() {
        assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
        onView(withText(R.string.title_dialog_exit_app))
            .inRoot(isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user select to \"([^\"]*)\" from dialog options$")
    fun userSelection(selection: String) {
        when(selection) {
            "confirm" -> {
                onView(withText(R.string.button_dialog_positive))
                    .inRoot(isDialog())
                    .perform(click())
            }
            "decline" -> {
                onView(withText(R.string.button_dialog_negative))
                    .inRoot(isDialog())
                    .perform(click())

            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$selection")
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should \"([^\"]*)\"$")
    fun appAction(action: String) {
        when(action) {
            "close app" -> {
                scenario.onActivity {
                    assertThat(it.isFinishing).isTrue()
                }
            }
            "do not close app" -> {
                scenario.onActivity {
                    assertThat(it.isFinishing).isFalse()
                }
            }
            else -> throw IllegalArgumentException("Unknown scenario arg:$action")
        }
    }
}