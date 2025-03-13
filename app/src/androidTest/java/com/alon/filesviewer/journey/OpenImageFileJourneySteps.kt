package com.alon.filesviewer.journey

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.alon.filesviewer.browser.ui.controller.FilesAdapter
import com.alon.filesviewer.util.DeviceUtil
import com.google.common.truth.Truth
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class OpenImageFileJourneySteps : GreenCoffeeSteps() {

    private lateinit var testFile: Pair<String, Uri>

    @Given("^user has a image file named 'sun' stored on device$")
    fun userHasImageOnDevice() {
        testFile = DeviceUtil.copyImageFileToDevice("assets/image/sun.jpg")
    }

    @When("^he open app from device home$")
    fun userOpenApp() {
        DeviceUtil.launchApp()
    }

    @And("^open image via app browser$")
    fun openImageViaBrowser() {
        Intents.init()
        onView(withId(com.alon.filesviewer.browser.ui.R.id.nav_image))
            .perform(click())
        Thread.sleep(2000)
        onView(withId(com.alon.filesviewer.browser.ui.R.id.browsedFiles))
            .perform(
                actionOnItemAtPosition<FilesAdapter.FileViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^app should show device apps chooser view it$")
    fun appShouldShowDeviceAppsChooser() {
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        Truth.assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        Truth.assertThat(intent.flags).isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Intents.release()
        DeviceUtil.deleteFilesFromDevice(testFile.first)
        DeviceUtil.deleteFromMediaStore(testFile.second)
        DeviceUtil.getDevice().pressBack()
    }

    fun getTestFilePath(): String {
        return testFile.first
    }
}