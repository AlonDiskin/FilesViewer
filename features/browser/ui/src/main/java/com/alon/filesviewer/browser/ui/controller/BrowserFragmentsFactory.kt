package com.alon.filesviewer.browser.ui.controller

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.DeviceNamedFolder
import com.alon.filesviewer.browser.ui.viewmodel.CollectionBrowserViewModel
import com.alon.filesviewer.browser.ui.viewmodel.FolderBrowserViewModel

object BrowserFragmentsFactory {

    fun createFolderBrowserFragment(folder: DeviceNamedFolder): Fragment {
        val bundle = bundleOf(FolderBrowserViewModel.KEY_BASE_FOLDER to folder.name)
        val fragment = FolderBrowserFragment()
        fragment.arguments = bundle

        return fragment
    }

    fun createFolderBrowserFragment(path: String): Fragment {
        val bundle = bundleOf(FolderBrowserViewModel.KEY_DEST_FOLDER to path)
        val fragment = FolderBrowserFragment()
        fragment.arguments = bundle

        return fragment
    }

    fun createCollectionBrowserFragment(collection: DeviceFilesCollection): Fragment {
        val bundle = bundleOf(CollectionBrowserViewModel.KEY_COLLECTION to collection.name)
        val fragment = CollectionBrowserFragment()
        fragment.arguments = bundle

        return fragment
    }
}