package com.alon.filesviewer.journey

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.ui.controller.FilesAdapter
import com.alon.filesviewer.util.DeviceUtil
import com.google.android.material.R
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class SearchImageJourneySteps: GreenCoffeeSteps() {

    private lateinit var testFile: Pair<String, Uri>

    @Given("^user has a image file named 'sun' stored in device$")
    fun userHasImageFile() {
        testFile = DeviceUtil.copyImageFileToDevice("assets/image/sun.jpg")
    }

    @When("^he open app from device home$")
    fun userOpenApp() {
        // Open app
        DeviceUtil.launchApp()
    }

    @And("^locate image via app search$")
    fun searchImage() {
        // Open search screen
        onView(withContentDescription("Search"))
            .perform(click())

        // Select images search filter
        onView(withText("Images"))
            .perform(click())

        // Type query
        onView(withId(R.id.search_src_text))
            .perform(typeText("sun"))
    }

    @And("^clicks on it$")
    fun clickOnImage() {
        Intents.init()
        onView(withId(com.alon.filesviewer.browser.ui.R.id.searchResults))
            .perform(
                actionOnItemAtPosition<FilesAdapter.FileViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^app should show device apps chooser to open image file$")
    fun showDeviceApps() {
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Intents.release()
        DeviceUtil.deleteFilesFromDevice(testFile.first)
        DeviceUtil.deleteFromMediaStore(testFile.second)
        DeviceUtil.getDevice().pressBack()
    }
}