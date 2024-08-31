package com.alon.filesviewer.browser.domain.model

data class DeviceFile(val path: String,
                      val name: String,
                      val type: DeviceFileType,
                      val size: Long,
                      val format: String,
                      val modificationDate: Long)