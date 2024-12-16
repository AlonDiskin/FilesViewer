package com.alon.filesviewer.browser.domain.model

sealed class BrowseRequest {

    data class Folder(val path: String): BrowseRequest()

    data class Category(val category: BrowsedCategory): BrowseRequest()
}