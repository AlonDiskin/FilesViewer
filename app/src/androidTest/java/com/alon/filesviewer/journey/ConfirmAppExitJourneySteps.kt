package com.alon.filesviewer.journey

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.util.DeviceUtil
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class ConfirmAppExitJourneySteps : GreenCoffeeSteps() {

    @Given("^user open app from device home$")
    fun userOpenApp() {
        DeviceUtil.launchApp()
    }

    @When("^he choose to leave app$")
    fun userLeaveApp() {
        pressBack()
    }

    @Then("^app should show exit dialog$")
    fun appShowExitDialog() {
        onView(withText("Exit app"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @When("^user confirm exit$")
    fun userConfirmExit() {
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(ViewActions.click())
    }

    @Then("^app should close$")
    fun appClose() {
        assertThat(DeviceUtil.isAppForeground()).isFalse()
    }
}