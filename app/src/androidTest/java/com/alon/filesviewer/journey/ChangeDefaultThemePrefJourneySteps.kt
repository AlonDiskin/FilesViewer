package com.alon.filesviewer.journey

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.util.DeviceUtil
import com.google.common.truth.Truth
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class ChangeDefaultThemePrefJourneySteps : GreenCoffeeSteps() {

    @Given("^user open app from device home$")
    fun userLaunchApp() {
        DeviceUtil.launchApp()
    }

    @And("^navigates to app settings screen$")
    fun openSettings() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText("Settings"))
            .perform(click())
        Thread.sleep(4000)
    }

    @When("^he change default theme pref$")
    fun changeAppTheme() {
        onView(withText("Enable dark theme"))
            .perform(click())
        Thread.sleep(4000)
    }

    @Then("^app should change theme to selected one$")
    fun themeShouldChange() {
        Truth.assertThat(AppCompatDelegate.getDefaultNightMode())
            .isEqualTo(AppCompatDelegate.MODE_NIGHT_YES)
    }
}