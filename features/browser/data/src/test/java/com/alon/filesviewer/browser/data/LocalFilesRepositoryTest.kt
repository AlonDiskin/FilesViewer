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

    // Stub data


    @Before
    fun setUp() {
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

        filesRepo = LocalFilesRepository(mapper)
    }

    @Test
    fun loadAllMatchingFiles_WhenFolderSearchedWithHiddenIncluded() {
        // Given
        val query = "meta"
        val rootDir = TemporaryFolder()
        rootDir.create()
        val musicDir = rootDir.newFolder("Music")
        val imageDir = rootDir.newFolder("Image")
        val downloadDir = rootDir.newFolder("Download")
        val hiddenFile = Files.createTempFile(Path(downloadDir.path),".meta",".pdf").toFile()
        val matchingFiles = listOf(
            Files.createTempFile(Path(musicDir.path),"metallica - what if",".mp3").toFile(),
            Files.createTempFile(Path(imageDir.path),"black_album_metallica",".jpeg").toFile(),
            hiddenFile
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
                matchingFiles[1].lastModified()),
            DeviceFile(
                matchingFiles[2].path,
                matchingFiles[2].name,
                DeviceFileType.TEXT,
                matchingFiles[2].length(),
                "pdf",
                matchingFiles[2].lastModified())
        )
        val expectedResult = Result.success(expectedFiles)
        val hiddenIncluded = true

        Files.createTempFile(Path(downloadDir.path),"file_2",".pdf")
        Files.createTempFile(Path(rootDir.root.path),"file_1",".pdf")
        Files.createTempFile(Path(musicDir.path),"elvis",".mp3")

        if (hiddenFile.path.contains('\\')) {
            Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true)
        }

        // When
        val resultObserver = filesRepo.search(query,rootDir.root.path,hiddenIncluded).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun loadNonHiddenAllMatchingFiles_WhenFolderSearchedWithHiddenNotIncluded() {
        // Given
        val query = "meta"
        val rootDir = TemporaryFolder()
        rootDir.create()
        val musicDir = rootDir.newFolder("Music")
        val imageDir = rootDir.newFolder("Image")
        val downloadDir = rootDir.newFolder("Download")
        val hiddenFile = Files.createTempFile(Path(downloadDir.path),".meta",".pdf").toFile()
        val matchingFiles = listOf(
            Files.createTempFile(Path(musicDir.path),"metallica - what if",".mp3").toFile(),
            Files.createTempFile(Path(imageDir.path),"black_album_metallica",".jpeg").toFile(),
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
        val hiddenIncluded = false

        Files.createTempFile(Path(downloadDir.path),"file_2",".pdf")
        Files.createTempFile(Path(rootDir.root.path),"file_1",".pdf")
        Files.createTempFile(Path(musicDir.path),"elvis",".mp3")

        if (hiddenFile.path.contains('\\')) {
            Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true)
        }

        // When
        val resultObserver = filesRepo.search(query,rootDir.root.path,hiddenIncluded).test()

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
        val resultObserver = filesRepo.getFolderFiles(path,true).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(expectedResult)
    }

    @Test
    fun returnErrorResult_WhenQueryForRestrictedAccessFolder() {
        // TODO("Not yet implemented")
    }

    @Test
    fun loadFolderFilesWithHidden_WhenQueryForFolderWithHiddenIncluded() {
        // Given
        val rootDir = TemporaryFolder()
        rootDir.create()
        val folder = rootDir.newFolder("Download")
        val path = folder.path
        val hiddenFile = Files.createTempFile(Path(folder.path),".file_1",".pdf").toFile()
        val folderFiles = listOf(
            hiddenFile,
            Files.createTempFile(Path(folder.path),"file_2",".pdf").toFile(),
            Files.createTempFile(Path(folder.path),"file_3",".jpeg").toFile(),
        )
        val includeHidden = true
        val expectedFiles = listOf(
            DeviceFile(folderFiles[0].path,
                folderFiles[0].name,
                DeviceFileType.TEXT,
                folderFiles[0].length(),
                "pdf",
                folderFiles[0].lastModified()),
            DeviceFile(
                folderFiles[1].path,
                folderFiles[1].name,
                DeviceFileType.TEXT,
                folderFiles[1].length(),
                "pdf",
                folderFiles[1].lastModified()),
            DeviceFile(
                folderFiles[2].path,
                folderFiles[2].name,
                DeviceFileType.IMAGE,
                folderFiles[2].length(),
                "jpeg",
                folderFiles[2].lastModified())
        )

        if (hiddenFile.path.contains('\\')) {
            Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true)
        }

        // When
        val resultObserver = filesRepo.getFolderFiles(path,includeHidden).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(Result.success(expectedFiles))
    }

    @Test
    fun loadFolderFilesWithoutHidden_WhenQueryForFolderWithoutHiddenIncluded() {
        // Given
        val rootDir = TemporaryFolder()
        rootDir.create()
        val folder = rootDir.newFolder("Download")
        val path = folder.path
        val hiddenFile = Files.createTempFile(Path(folder.path),".file_1",".pdf").toFile()
        val regularFiles = listOf(
            Files.createTempFile(Path(folder.path),"file_2",".pdf").toFile(),
            Files.createTempFile(Path(folder.path),"file_3",".jpeg").toFile(),
        )
        val includeHidden = false
        val expectedFiles = listOf(
            DeviceFile(regularFiles[0].path,
                regularFiles[0].name,
                DeviceFileType.TEXT,
                regularFiles[0].length(),
                "pdf",
                regularFiles[0].lastModified()),
            DeviceFile(
                regularFiles[1].path,
                regularFiles[1].name,
                DeviceFileType.IMAGE,
                regularFiles[1].length(),
                "jpeg",
                regularFiles[1].lastModified())
        )

        if (hiddenFile.path.contains('\\')) {
            Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true)
        }

        // When
        val resultObserver = filesRepo.getFolderFiles(path,includeHidden).test()

        // Then
        assertThat(resultObserver.values().first()).isEqualTo(Result.success(expectedFiles))
    }

    @Test
    fun returnEmptyFilesList_WhenQueryWithEmptyFolderPath() {
        // Given
        val path = ""

        // When
        val actual = filesRepo.getFolderFiles(path,true).test()

        // Then
        actual.assertResult(Result.success(emptyList()))
    }
}