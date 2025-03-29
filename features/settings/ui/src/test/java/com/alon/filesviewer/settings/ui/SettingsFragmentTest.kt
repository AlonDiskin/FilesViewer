package com.alon.filesviewer.settings.ui

import android.content.Context
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import com.alon.messeging.R as MessegingR

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class SettingsFragmentTest {

    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Fragment under test
    private lateinit var scenario : FragmentScenario<SettingsFragment>

    @Before
    fun setUp() {
        scenario = FragmentScenario.Companion.launchInContainer(SettingsFragment::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showDarkThemePref_WhenDisplayed() {
        // Given

        // Then
        onView(withText(R.string.pref_dark_theme_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun setDarkThemePrefDisabledByDefault() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.pref_dark_theme_key),true))
            .isFalse()
    }

    @Test
    fun setDarkThemePrefEnabled_WhenUserEnableIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } returns Unit

        // When
        onView(withText(R.string.pref_dark_theme_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.pref_dark_theme_key),false))
            .isTrue()
        verify(exactly = 1) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
    }

    @Test
    fun setDarkThemePrefDisabled_WhenUserDisableIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(context.getString(R.string.pref_dark_theme_key),true)
            .commit()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } returns Unit

        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withText(R.string.pref_dark_theme_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.pref_dark_theme_key),true))
            .isFalse()
        verify(exactly = 1) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
    }

    @Test
    fun showHiddenFilesEnabledPref_WhenDisplayed() {
        // Given

        // Then
        onView(withText(MessegingR.string.pref_hidden_files_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun setShowHiddenFilesPrefEnabledByDefault() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(MessegingR.string.pref_hidden_files_key),false))
            .isTrue()
    }

    @Test
    fun setShowHiddenFilesPrefDisabled_WhenUserDisableIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        // When
        onView(withText(MessegingR.string.pref_hidden_files_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(MessegingR.string.pref_hidden_files_key),true))
            .isFalse()
    }

    @Test
    fun setShowHiddenFilesPrefEnabled_WhenUserEnableIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(context.getString(MessegingR.string.pref_hidden_files_key),false)
            .commit()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withText(MessegingR.string.pref_hidden_files_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(MessegingR.string.pref_hidden_files_key),false))
            .isTrue()
    }
}