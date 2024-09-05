package com.alon.filesviewer.browser.data.local

import com.alon.filesviewer.browser.domain.model.DeviceFileType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeMapper @Inject constructor() {

    private val videoFormats = listOf("avi","webm","mkv","flv",
        "vob","ogg","ogv","gifv","mng","flv","mp4",
        "TS","mov","qt","wmv","viv","amv","mpeg","mpv","svi","nsv")
    private val textFormats = listOf("asc","doc","docx","rtf","msg"
        ,"pdf","txt","wpd","html","json","xml","odt")
    private val imageFormats = listOf("tif","gif","bmp","jpg","jpeg",
        "png","eps","svg","tiff","webp")
    private val audioFormats = listOf("mp3","wav","aac","wma","flac","alac","aiff","m4p","vox")

    fun map(name: String): DeviceFileType {
        val format = name.split(".").last()

        return when {
            videoFormats.contains(format) -> DeviceFileType.VIDEO
            imageFormats.contains(format) -> DeviceFileType.IMAGE
            audioFormats.contains(format) -> DeviceFileType.AUDIO
            textFormats.contains(format) -> DeviceFileType.TEXT
            else -> DeviceFileType.OTHER
        }
    }
}