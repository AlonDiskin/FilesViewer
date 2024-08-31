package com.alon.filesviewer.browser.data

import com.alon.filesviewer.browser.data.local.DeviceFileMapper
import com.alon.filesviewer.browser.data.local.FilesProvider
import com.alon.filesviewer.browser.data.local.RxLocalStorage
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

@RunWith(JUnitParamsRunner::class)
class FilesProviderTest {

    // Test subject
    private lateinit var filesProvider: FilesProvider

    // Collaborators
    private val mapper: DeviceFileMapper = mockk()

    @Before
    fun setUp() {
        filesProvider = FilesProvider(mapper)
    }

    @Test
    @Parameters(method = "searchAllParams")
    fun findAllMatchingFiles_WhenSearched(rootDir: File, query: String, expectedNames: List<String>) {
        // Given
        val fetchSlot = slot<() -> (Result<List<DeviceFile>>)>()

        mockkObject(RxLocalStorage)
        every { RxLocalStorage.filesObservable (capture(fetchSlot)) } returns Observable.empty()

        every { mapper.map(any()) } answers {
            val file: File = args[0] as File
            val mappedFile = mockk<DeviceFile>()
            every { mappedFile.name } returns file.name
            mappedFile
        }

        // When
        filesProvider.search(query,rootDir.path)

        // Then
        assertThat(fetchSlot.invoke().getOrNull()!!.map { it.name }).isEqualTo(expectedNames)
    }

    private fun searchAllParams() = arrayOf(
        createSearchAllFirstTestParams(),
        createSearchAllSecondTestParams()
    )

    private fun createSearchAllFirstTestParams(): Array<Any> {
        val query = "meta"
        val expectedSearchResNames = mutableListOf<String>()
        val rootDir = TemporaryFolder()
        rootDir.create()
        val musicDir = rootDir.newFolder("Music")
        val imageDir = rootDir.newFolder("Image")

        expectedSearchResNames.addAll(
            listOf(
                Files.createTempFile(Path(musicDir.path),"metallica - what if",".mp3").toFile().name,
                Files.createTempFile(Path(imageDir.path),"black_album_metallica",".jpeg").toFile().name
            )
        )
        Files.createTempFile(Path(musicDir.path),"megadeath",".mp3")
        Files.createTempFile(Path(imageDir.path),"nirvana",".mpeg")

        return arrayOf(
            rootDir.root,query,expectedSearchResNames
        )
    }

    private fun createSearchAllSecondTestParams(): Array<Any> {
        val query = "elvis"
        val expectedSearchResFileNames = mutableListOf<String>()
        val rootDir = TemporaryFolder()
        rootDir.create()
        val musicDir = rootDir.newFolder("Music")
        val imageDir = rootDir.newFolder("Image")
        val downloadsDir = rootDir.newFolder("Downloads")
        val favoriteMusicDir = Files.createTempDirectory(Path(musicDir.path),"Favorite").toFile()

        expectedSearchResFileNames.addAll(
            listOf(
                Files.createTempFile(Path(favoriteMusicDir.path),"elvis-if_i_can_dream",".mp3").toFile().name
            )
        )

        Files.createTempFile(Path(musicDir.path),"metallica - what if",".mp3")
        Files.createTempFile(Path(imageDir.path),"black_album_metallica",".jpeg")
        Files.createTempFile(Path(musicDir.path),"megadeath",".mp3")
        Files.createTempFile(Path(imageDir.path),"nirvana",".mpeg")
        Files.createTempFile(Path(downloadsDir.path),"salary",".pdf")

        return arrayOf(
            rootDir.root,query,expectedSearchResFileNames
        )
    }
}