package com.alon.filesviewer.browser.data

import com.alon.filesviewer.browser.data.local.DeviceFileMapper
import com.alon.filesviewer.browser.data.local.LocalFilesRepository
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
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

class LocalFilesRepositoryTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var filesRepo: LocalFilesRepository

    // Collaborators
    private val mapper: DeviceFileMapper = mockk()

    @Before
    fun setUp() {
        filesRepo = LocalFilesRepository(mapper)
    }

    @Test
    fun loadAllMatchingFiles_WhenFolderSearched() {
        // Given
        val query = "meta"
        val rootDir = TemporaryFolder()
        rootDir.create()
        val musicDir = rootDir.newFolder("Music")
        val imageDir = rootDir.newFolder("Image")
        val downloadDir = rootDir.newFolder("Download")
        val matchingFiles = listOf(
            Files.createTempFile(Path(musicDir.path),"metallica - what if",".mp3").toFile(),
            Files.createTempFile(Path(imageDir.path),"black_album_metallica",".jpeg").toFile()
        )
        val expectedFiles = listOf(
            DeviceFile(matchingFiles[0].path,
                matchingFiles[0].name,
                DeviceFileType.AUDIO,
                matchingFiles[0].length(),
                "mp3",
                matchingFiles[0].lastModified()),
            DeviceFile(
                matchingFiles[1].path,
                matchingFiles[1].name,
                DeviceFileType.IMAGE,
                matchingFiles[1].length(),
                "jpeg",
                matchingFiles[1].lastModified())
        )
        val expectedResult = Result.success(expectedFiles)

        Files.createTempFile(Path(downloadDir.path),"file_2",".pdf")
        Files.createTempFile(Path(rootDir.root.path),"file_1",".pdf")
        Files.createTempFile(Path(musicDir.path),"elvis",".mp3")

        every { mapper.map(any()) } answers {
            val file: File = args[0] as File
            val suffix = file.name.split(".").last()
            val type = when(suffix){
                "mp3" -> DeviceFileType.AUDIO
                "jpeg" -> DeviceFileType.IMAGE
                "pdf" -> DeviceFileType.TEXT
                else -> throw IllegalArgumentException("Unknown test file suffix:$suffix")
            }
            val mappedFile = DeviceFile(file.path,
                file.name,
                type,
                file.length(),
                suffix,
                file.lastModified())
            mappedFile
        }

        // When
        val resultObserver = filesRepo.search(query,rootDir.root.path).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun returnErrorResult_WhenQueryForNonExistingFolder() {
        // Given
        val rootDir = TemporaryFolder()
        rootDir.create()
        val path = rootDir.root.path + "SomeFolder"
        val expectedErrorMessage = "Dir not existing: $path"
        val expectedResult = Result.failure<BrowserError>(BrowserError.NonExistingDir(expectedErrorMessage))

        // When
        val resultObserver = filesRepo.getFolderFiles(path).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun returnErrorResult_WhenQueryForRestrictedAccessFolder() {
        // TODO("Not yet implemented")
    }

    @Test
    fun loadFolderFiles_WhenQueryForFolder() {
        // Given
        val rootDir = TemporaryFolder()
        rootDir.create()
        val folder = rootDir.newFolder("Music")
        val path = folder.path
        val expectedFiles = mutableListOf<DeviceFile>()

        every { mapper.map(any()) } answers {
            val file: File = args[0] as File
            val mappedFile = DeviceFile(file.path,
                file.name,
                DeviceFileType.IMAGE,
                file.length(),
                "format",
                file.lastModified())
            expectedFiles.add(mappedFile)
            mappedFile
        }

        // When
        val resultObserver = filesRepo.getFolderFiles(path).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(Result.success(expectedFiles))
    }

    @Test
    fun returnEmptyFilesList_WhenQueryWithEmptyFolderPath() {
        // Given
        val path = ""

        // When
        val actual = filesRepo.getFolderFiles(path).test()

        // Then
        actual.assertResult(Result.success(emptyList()))
    }
}