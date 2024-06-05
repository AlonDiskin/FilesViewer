package com.alon.filesviewer.browser.data.local

import android.net.Uri
import android.webkit.MimeTypeMap
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceFileMapper @Inject constructor(){
    private val videoFormats = listOf("avi","webm","mkv","flv",
        "vob","ogg","ogv","gifv","mng","flv","mp4",
        "TS","mov","qt","wmv","viv","amv","mpeg","mpv","svi","nsv")
    private val textFormats = listOf("asc","doc","docx","rtf","msg"
        ,"pdf","txt","wpd","html","json","xml","odt")
    private val imageFormats = listOf("tif","gif","bmp","jpg","jpeg",
        "png","eps","svg","tiff","webp")
    private val audioFormats = listOf("mp3","wav","aac","wma","flac","alac","aiff","m4p","vox")

    fun map(file: File): DeviceFile {
        return DeviceFile(
            file.path,
            file.name,
            getFileType(file),
            file.length(),
            getFileFormat(file),
            file.lastModified()
        )
    }

    private fun getFileFormat(file: File): String {
        val uri = Uri.fromFile(file)
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    }

    private fun getFileType(file: File): DeviceFileType {
        return if (file.isDirectory) {
            DeviceFileType.DIR
        } else {
            val uri = Uri.fromFile(file)
            val format = MimeTypeMap.getFileExtensionFromUrl(uri.toString())

            when {
                videoFormats.contains(format) -> DeviceFileType.VIDEO
                imageFormats.contains(format) -> DeviceFileType.IMAGE
                audioFormats.contains(format) -> DeviceFileType.AUDIO
                textFormats.contains(format) -> DeviceFileType.TEXT
                else -> DeviceFileType.OTHER
            }
        }
    }
}