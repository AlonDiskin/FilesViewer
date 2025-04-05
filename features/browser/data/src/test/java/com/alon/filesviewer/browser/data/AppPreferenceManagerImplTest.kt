package com.alon.filesviewer.browser.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.data.implementation.AppPreferenceManagerImpl
import com.alon.messeging.R
import com.google.common.truth.Truth.assertThat
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AppPreferenceManagerImplTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var prefManager: AppPreferenceManagerImpl

    // Collaborators
    private val app: Application = ApplicationProvider.getApplicationContext<Context>() as Application
    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)

    @Before
    fun setUp() {
        prefManager = AppPreferenceManagerImpl(app,sharedPrefs)
    }

    @Test
    fun retrieveHiddenFilesPref_WhenQueried() {
        // Given
        val key = app.getString(R.string.pref_hidden_files_key)
        val value = true

        sharedPrefs.edit()
            .putBoolean(key,value)
            .commit()

        // When
        val testObserver = prefManager.isHiddenFilesShowingEnabled().test()

        // Then
        testObserver.assertValue(value)
    }

    @Test
    fun updateHiddenFilesPref_WhenPrefChanged() {
        // Given
        val key = app.getString(R.string.pref_hidden_files_key)
        val defaultValue = app.getString(R.string.pref_hidden_files_default).toBoolean()
        val updateValue = false

        // When
        val testObserver = prefManager.isHiddenFilesShowingEnabled().test()

        // And
        sharedPrefs.edit()
            .putBoolean(key,updateValue)
            .commit()

        // Then
        assertThat(testObserver.valueCount()).isEqualTo(2)
        testObserver.assertValueAt(0,defaultValue)
        testObserver.assertValueAt(1,updateValue)
    }
}