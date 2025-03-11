package com.alon.filesviewer.journey

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.alon.filesviewer.browser.ui.controller.FilesAdapter
import com.alon.filesviewer.util.DeviceUtil
import com.google.android.material.R
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.hamcrest.CoreMatchers.allOf

class BrowseSearchedFolderJourneySteps : GreenCoffeeSteps() {

    private val searchedFolderName = "my-music"
    private val searchedFolderFiles = listOf("file_1.txt","file_2.txt")
    private lateinit var testFolder: String

    @Given("^user has a folder named 'my-music' that has files$")
    fun userHasFolder() {
        testFolder = DeviceUtil.createFolder(searchedFolderName)
        DeviceUtil.createFiles(
            testFolder,
            searchedFolderFiles
        )
    }

    @When("^he open app from device home$")
    fun userOpenApp() {
        DeviceUtil.launchApp()
    }

    @And("^locate folder via app search$")
    fun findFolder() {
        // Open search screen
        onView(withContentDescription("Search"))
            .perform(click())

        // Type query
        onView(withId(R.id.search_src_text))
            .perform(typeText(searchedFolderName))
    }

    @And("^clicks to browse it$")
    fun clickFolder() {
        Thread.sleep(3000)
        onView(withId(com.alon.filesviewer.browser.ui.R.id.searchResults))
            .perform(
                actionOnItemAtPosition<FilesAdapter.FileViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Then("^app should list folder in its files browser$")
    fun appShouldListFolder() {
        Thread.sleep(3000)
        onView(allOf(withId(com.alon.filesviewer.browser.ui.R.id.fileName),withText(
            searchedFolderFiles[0])))
            .check(matches(isDisplayed()))
        onView(allOf(withId(com.alon.filesviewer.browser.ui.R.id.fileName),withText(
            searchedFolderFiles[1]
        )))
            .check(matches(isDisplayed()))
    }

    fun getTestFolder(): String {
        return testFolder
    }
}