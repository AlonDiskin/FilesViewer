package com.alon.filesviewer.browser.data

import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.Q
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.data.local.PathProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PathProviderTest {

    // Test subject
    private val pathProvider = PathProvider()

    @Test
    fun returnStorageRootDirPath() {
        // Given
        val expectedPath = Environment.getExternalStorageDirectory().path

        // When
        val actualPath = pathProvider.getRootDirPath()

        // Then
        assertThat(actualPath).isEqualTo(expectedPath)
    }

    @Test
    fun returnStorageDownloadsDirPath() {
        // Given
        val expectedPath = Environment.getExternalStorageDirectory()
            .path.plus("/${Environment.DIRECTORY_DOWNLOADS}")

        // When
        val actualPath = pathProvider.getDownloadsDirPath()

        // Then
        assertThat(actualPath).isEqualTo(expectedPath)
    }

    @Test
    @Config(sdk = [Q])
    fun returnMediaStoreVideoUriSdk29() {
        // Given
        val expectedUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        // When
        val actualUri = pathProvider.getVideoCollectionUri()

        // Then
        assertThat(actualUri).isEqualTo(expectedUri)
    }

    @Test
    @Config(sdk = [N])
    fun returnMediaStoreVideoUriSdk24() {
        // Given
        val expectedUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        // When
        val actualUri = pathProvider.getVideoCollectionUri()

        // Then
        assertThat(actualUri).isEqualTo(expectedUri)
    }
}