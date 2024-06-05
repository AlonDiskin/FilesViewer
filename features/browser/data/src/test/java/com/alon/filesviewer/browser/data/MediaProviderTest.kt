package com.alon.filesviewer.browser.data

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.data.local.DeviceFileMapper
import com.alon.filesviewer.browser.data.local.MediaProvider
import com.alon.filesviewer.browser.data.local.RxLocalStorage
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import java.io.File

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MediaProviderTest {

    // Test subject
    private lateinit var mediaProvider: MediaProvider

    // Collaborators
    private val contentResolver: ContentResolver = mockk()
    private val filesMapper: DeviceFileMapper = mockk()

    @Before
    fun setUp() {
        mediaProvider = MediaProvider(filesMapper, contentResolver)
    }

    @Test
    fun searchDeviceMediaStore_WhenSearched() {
        // Given
        val fetchSlot = slot<() -> (Result<List<DeviceFile>>)>()
        val query = "query"
        val mediaUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()
        val expectedProjection = arrayOf(MediaStore.MediaColumns.DATA)
        val expectedSelection = "${MediaStore.MediaColumns.TITLE} LIKE ?"
        val expectedSelectionArgs = arrayOf("%$query%")
        val expectedSortOrder = "${MediaStore.MediaColumns.TITLE} ASC"
        val expectedFiles = listOf("path_1","path_2","path_3")
        val cursor = MatrixCursor(arrayOf(MediaStore.MediaColumns.DATA,MediaStore.MediaColumns.TITLE))

        cursor.addRow(arrayOf("path_1","file_1"))
        cursor.addRow(arrayOf("path_2","file_2"))
        cursor.addRow(arrayOf("path_3","file_3"))
        mockkObject(RxLocalStorage)
        every { RxLocalStorage.mediaObservable(capture(fetchSlot),contentResolver,mediaUri) } returns res
        every { contentResolver.query(mediaUri, expectedProjection,expectedSelection,
            expectedSelectionArgs,expectedSortOrder) } returns cursor
        every { filesMapper.map(any()) } answers {
            val file: File = args[0] as File
            val mappedFile = mockk<DeviceFile>()
            every { mappedFile.path } returns file.path
            mappedFile
        }

        // When
        val actualRes = mediaProvider.search(query,mediaUri)

        // Then
        assertThat(actualRes).isEqualTo(res)
        assertThat(fetchSlot.invoke().getOrNull()!!.map { it.path }).isEqualTo(expectedFiles)
    }

    @Test
    fun returnErrorResult_WhenSearchQueryFail() {
        // Given
        val fetchSlot = slot<() -> (Result<List<DeviceFile>>)>()
        val query = "query"
        val mediaUri = mockk<Uri>()
        val res = mockk<Observable<Result<List<DeviceFile>>>>()
        val expectedError = BrowserError.Internal(MediaProvider.ERROR_MEDIA_QUERY)

        mockkObject(RxLocalStorage)
        every { RxLocalStorage.mediaObservable(capture(fetchSlot),contentResolver,mediaUri) } returns res
        every { contentResolver.query(mediaUri,any(),any(), any(),any()) } returns null

        // When
        val actualRes = mediaProvider.search(query,mediaUri)

        // Then
        assertThat(actualRes).isEqualTo(res)
        assertThat(fetchSlot.invoke().exceptionOrNull()).isEqualTo(expectedError)
    }
}