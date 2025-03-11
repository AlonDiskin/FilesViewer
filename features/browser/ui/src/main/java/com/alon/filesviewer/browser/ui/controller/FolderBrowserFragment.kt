package com.alon.filesviewer.browser.ui.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.viewmodel.FolderBrowserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import org.apache.commons.io.FileUtils
import java.io.File

@OptionalInject
@AndroidEntryPoint
class FolderBrowserFragment : Fragment(R.layout.fragment_browser) {

    private val viewModel: FolderBrowserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup recycler view for browsed files presentation
        val rv = view.findViewById<RecyclerView>(R.id.browsedFiles)
        val pathTv = view.findViewById<TextView>(R.id.currentPathLabel)
        val adapter = FilesAdapter(::onFileClick,::onFileMenuClick)
        rv.adapter = adapter

        viewModel.uiState.observe(viewLifecycleOwner) {
            // Set browsed files list
            adapter.submitList(it.files)

            // Show current browsed folder path
            pathTv.text = it.currentPath

            // Show error message if one exist
            if (it.error != null) { handleErrorUpdate(it.error) }
        }

        // Handle back press. if folder has parent, nav up, else pass call to activity.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isRootFolder()) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                } else{
                    viewModel.navUpFromFolder()
                }
            }
        })
    }

    private fun onFileClick(file: DeviceFile) {
        if (file.type == DeviceFileType.DIR) {
            viewModel.navToFolder(file.path)
        } else {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName.plus(".provider"),
                File(file.path)
            )
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.format)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri,mime)
                setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, getString(R.string.title_app_chooser))

            startActivity(chooser)
        }
    }

    private fun onFileMenuClick(file: DeviceFile, view: View) {
        PopupMenu(requireContext(), view).apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_detail -> {
                        showFileDetail(file)
                        true
                    }

                    else -> false
                }
            }
            inflate(R.menu.menu_file)
            show()
        }
    }

    private fun showFileDetail(file: DeviceFile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_dialog_file_detail))
            .setMessage(
                getString(
                    R.string.file_detail,
                    file.name,
                    file.path,
                    FileUtils.byteCountToDisplaySize(file.size))
            )
            .setPositiveButton(getString(R.string.button_dialog_positive)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun handleErrorUpdate(error: BrowserError) {
        when(error) {
            is BrowserError.NonExistingDir -> showErrorNonExistingDir()
            is BrowserError.AccessDenied -> showErrorDirAccessDenied()
            is BrowserError.Internal -> showErrorBrowserFeatureFail()
            else -> {}
        }
    }

    private fun showErrorNonExistingDir() {
        val rv = requireView().findViewById<RecyclerView>(R.id.browsedFiles)
        (rv.adapter as FilesAdapter).submitList(emptyList())
        showFileErrorDialog(getString(R.string.error_message_dir_non_exist))
    }

    private fun showErrorDirAccessDenied() {
        val rv = requireView().findViewById<RecyclerView>(R.id.browsedFiles)
        (rv.adapter as FilesAdapter).submitList(emptyList())
        showFileErrorDialog(getString(R.string.error_message_access_denied))
    }

    private fun showErrorBrowserFeatureFail() {
        val rv = requireView().findViewById<RecyclerView>(R.id.browsedFiles)
        (rv.adapter as FilesAdapter).submitList(emptyList())
        showFileErrorDialog(getString(R.string.error_message_browser_feature))
    }

    private fun showFileErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_dialog_error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.button_dialog_positive)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}