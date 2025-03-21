package com.alon.filesviewer.journey

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class ChangeDefaultSettingsJourneySteps : GreenCoffeeSteps() {

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
        TODO()
    }

    @And("^change default files sorting pref$")
    fun changeSortingPref() {
        TODO()
    }

    @When("^he navigates to browser screen$")
    fun openBrowserScreen() {
        TODO()
    }

    @Then("^app should change theme to selected one$")
    fun appChangeTheme() {
        TODO()
    }

    @And("^list app files according to selected sorting$")
    fun changeSorting() {
        TODO()
    }
}