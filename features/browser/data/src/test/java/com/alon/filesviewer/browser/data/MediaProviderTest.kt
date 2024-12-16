package com.alon.filesviewer.browser.data

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.data.local.FileTypeMapper
import com.alon.filesviewer.browser.data.local.MediaProvider
import com.alon.filesviewer.browser.data.local.RxLocalStorage
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MediaProviderTest {

    // Test subject
    private lateinit var mediaProvider: MediaProvider

    // Collaborators
    private val typeMapper: FileTypeMapper = mockk()
    private val contentResolver: ContentResolver = mockk()

    @Before
    fun setUp() {
        mediaProvider = MediaProvider(typeMapper, contentResolver)
    }

    @Test
    fun searchDeviceMediaStore_WhenSearched() {
        // Given
        val fetchSlot = slot<() -> (Result<List<DeviceFile>>)>()
        val query = "query"
        val mediaUri = mockk<Uri>()
        val expectedRes = mockk<Observable<Result<List<DeviceFile>>>>()
        val expectedProjection = arrayOf(MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val expectedSelection = "${MediaStore.MediaColumns.TITLE} LIKE ?"
        val expectedSelectionArgs = arrayOf("%$query%")
        val expectedSortOrder = "${MediaStore.MediaColumns.TITLE} ASC"
        val expectedFiles = listOf(
            DeviceFile("path_1","file_1",DeviceFileType.IMAGE,10L,"path_1",20L)
        )
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
        )

        cursor.addRow(arrayOf("path_1","file_1",10L,20L))
        mockkObject(RxLocalStorage)
        every { RxLocalStorage.mediaObservable(capture(fetchSlot),contentResolver,mediaUri) } returns expectedRes
        every { contentResolver.query(mediaUri, expectedProjection,expectedSelection,
            expectedSelectionArgs,expectedSortOrder) } returns cursor
        every { typeMapper.map(any()) } returns DeviceFileType.IMAGE

        // When
        val actualRes = mediaProvider.search(query,mediaUri)

        // Then
        assertThat(actualRes).isEqualTo(expectedRes)
        assertThat(fetchSlot.invoke().getOrNull()!!).isEqualTo(expectedFiles)
    }

    @Test
    fun loadAllMediaStoreFiles_WhenSearched() {
        // Given
        val fetchSlot = slot<() -> (Result<List<DeviceFile>>)>()
        val mediaUri = mockk<Uri>()
        val expectedRes = mockk<Observable<Result<List<DeviceFile>>>>()
        val expectedProjection = arrayOf(MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val expectedSelection = null
        val expectedSelectionArgs = null
        val expectedSortOrder = null
        val expectedFiles = listOf(
            DeviceFile("path_1","file_1",DeviceFileType.IMAGE,10L,"path_1",20L)
        )
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
        )

        cursor.addRow(arrayOf("path_1","file_1",10L,20L))
        mockkObject(RxLocalStorage)
        every { RxLocalStorage.mediaObservable(capture(fetchSlot),contentResolver,mediaUri) } returns expectedRes
        every { contentResolver.query(mediaUri, expectedProjection,expectedSelection,
            expectedSelectionArgs,expectedSortOrder) } returns cursor
        every { typeMapper.map(any()) } returns DeviceFileType.IMAGE

        // When
        val actualRes = mediaProvider.getAll(mediaUri)

        // Then
        assertThat(actualRes).isEqualTo(expectedRes)
        assertThat(fetchSlot.invoke().getOrNull()!!).isEqualTo(expectedFiles)
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