package com.alon.filesviewer.settings.ui

import android.content.Context
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode

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
    fun showDarkThemePrefTitle_WhenDisplayed() {
        // Given

        // Then
        onView(withText(R.string.pref_dark_theme_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun setDarkThemeDisabledByDefault() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Then
        // check pref manager also
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.pref_dark_theme_key),true))
            .isFalse()
        onView(instanceOf(SwitchCompat::class.java))
            .check(matches(isNotChecked()))
    }

    @Test
    fun setDarkThemeEnabled_WhenUserEnableIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } returns Unit

        // When
        onView(instanceOf(SwitchCompat::class.java))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.pref_dark_theme_key),false))
            .isTrue()
        verify(exactly = 1) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
    }

    @Test
    fun setDarkThemeDisable_WhenUserDisableIt() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(context.getString(R.string.pref_dark_theme_key),true)
            .commit()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } returns Unit

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(instanceOf(SwitchCompat::class.java))
            .check(matches(isChecked()))

        // When
        onView(instanceOf(SwitchCompat::class.java))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.pref_dark_theme_key),true))
            .isFalse()
        onView(instanceOf(SwitchCompat::class.java))
            .check(matches(isNotChecked()))
        verify(exactly = 1) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }

    }
}