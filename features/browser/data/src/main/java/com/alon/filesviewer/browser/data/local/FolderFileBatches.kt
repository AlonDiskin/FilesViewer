package com.alon.filesviewer.browser.data.local

import java.io.File

class FolderFileBatches(file: File) {

    private val batches: List<List<File>> = if (!file.isDirectory) {
        emptyList()
    } else {
        val folderFiles = file.listFiles()!!
        val filesCount = folderFiles.size
        listOf(
            folderFiles.copyOfRange(0,filesCount / 3).asList(),
            folderFiles.copyOfRange(filesCount / 3, (filesCount * 2) / 3).asList(),
            folderFiles.copyOfRange((filesCount * 2) / 3, filesCount).asList()
        )
    }
    val firstBatch get() = batches[0]
    val secondBatch get() = batches[1]
    val thirdBatch get() = batches[2]


}