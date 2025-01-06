package com.alon.filesviewer.browser.data

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alon.filesviewer.browser.data.local.FileTypeMapper
import com.alon.filesviewer.browser.data.local.LocalMediaRepository
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class LocalMediaRepositoryTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var mediaRepo: LocalMediaRepository

    // Collaborators
    private val typeMapper: FileTypeMapper = mockk()
    private val contentResolver: ContentResolver = mockk()

    @Before
    fun setUp() {
        mediaRepo = LocalMediaRepository(typeMapper, contentResolver)
    }

    @Test
    fun loadAllMediaFiles_WhenQueriedForMediaCollection() {
        // Given
        val mediaUri = mockk<Uri>()
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
        val expectedResult = Result.success(expectedFiles)
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
        )

        cursor.addRow(arrayOf("path_1","file_1",10L,20L))
        every { contentResolver.registerContentObserver(mediaUri,true,any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { contentResolver.query(mediaUri,expectedProjection,expectedSelection, expectedSelectionArgs,expectedSortOrder) } returns cursor
        every { typeMapper.map(any()) } returns DeviceFileType.IMAGE

        // When
        val resultObserver = mediaRepo.getAll(mediaUri).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun loadMatchingMediaFiles_WhenMediaCollectionSearched() {
        // Given
        val query = "query"
        val mediaUri = mockk<Uri>()
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
        val expectedResult = Result.success(expectedFiles)
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
        )

        cursor.addRow(arrayOf("path_1","file_1",10L,20L))
        every { contentResolver.registerContentObserver(mediaUri,true,any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { contentResolver.query(mediaUri,expectedProjection,expectedSelection, expectedSelectionArgs,expectedSortOrder) } returns cursor
        every { typeMapper.map(any()) } returns DeviceFileType.IMAGE

        // When
        val resultObserver = mediaRepo.search(query,mediaUri).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun returnInternalError_WhenSearchFail() {
        // Given
        val query = "query"
        val mediaUri = mockk<Uri>()
        val expectedResult = Result.failure<BrowserError>(BrowserError.Internal(LocalMediaRepository.ERROR_MEDIA_QUERY))

        every { contentResolver.registerContentObserver(mediaUri,true,any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { contentResolver.query(mediaUri,any(),any(), any(),any()) } returns null

        // When
        val resultObserver = mediaRepo.search(query,mediaUri).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun returnInternalError_WhenLoadingAllMediaFail() {
        // Given
        val mediaUri = mockk<Uri>()
        val expectedResult = Result.failure<BrowserError>(BrowserError.Internal(LocalMediaRepository.ERROR_MEDIA_QUERY))

        every { contentResolver.registerContentObserver(mediaUri,true,any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { contentResolver.query(mediaUri,any(),any(), any(),any()) } returns null

        // When
        val resultObserver = mediaRepo.getAll(mediaUri).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }
}