package com.alon.filesviewer.journey

import androidx.test.uiautomator.By
import com.alon.filesviewer.util.DeviceUtil
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class ApproveAppPermissionsJourneySteps : GreenCoffeeSteps() {

    @Given("^user has not yet approved storage permissions$")
    fun permissionsNotApproved() {
    }

    @When("^user open app from device home$")
    fun userOpenApp() {
        DeviceUtil.launchApp()
    }

    @Then("^app should open file access permission settings screen$")
    fun launchLoginScreen() {
        val label = DeviceUtil.getDevice()
            .findObject(By.text("All files access"))
        assertThat(label).isNotNull()
    }

    @When("^user approve permission$")
    fun approvePermission() {
        DeviceUtil.getDevice()
            .findObject(By.clazz("android.widget.Switch"))
            .click()
    }

    @And("^go back to app$")
    fun backToApp() {
        DeviceUtil.getDevice().pressBack()
    }

    @Then("^app should launch home screen$")
    fun launchHomeScreen() {
        assertThat(DeviceUtil.isAppForeground()).isTrue()
    }
}