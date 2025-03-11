package com.alon.filesviewer.journey

import androidx.test.filters.LargeTest
import com.alon.filesviewer.util.DeviceUtil
import com.alon.filesviewer.util.GrantManageStoragePermissionRule
import com.mauriciotogneri.greencoffee.GreenCoffeeConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeTest
import com.mauriciotogneri.greencoffee.Scenario
import com.mauriciotogneri.greencoffee.ScenarioConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@HiltAndroidTest
@RunWith(Parameterized::class)
@LargeTest
class BrowseSearchedFolderJourneyStepsRunner(scenario: ScenarioConfig) :  GreenCoffeeTest(scenario) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Iterable<ScenarioConfig> {
            return GreenCoffeeConfig()
                .withFeatureFromAssets("assets/feature/browse_searched_folder.feature")
                .scenarios()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val permissionRule = GrantManageStoragePermissionRule()
    private lateinit var steps: BrowseSearchedFolderJourneySteps

    @Test
    fun test() {
        steps = BrowseSearchedFolderJourneySteps()
        start(steps)
    }

    override fun afterScenarioEnds(scenario: Scenario?, locale: Locale?) {
        DeviceUtil.deleteFilesFromDevice(steps.getTestFolder())
        super.afterScenarioEnds(scenario, locale)
    }
}