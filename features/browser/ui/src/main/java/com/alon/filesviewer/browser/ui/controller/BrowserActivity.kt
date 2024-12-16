package com.alon.filesviewer.browser.ui.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.alon.filesviewer.browser.domain.model.BrowsedCategory
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.databinding.ActivityBrowserBinding
import com.alon.filesviewer.browser.ui.viewmodel.BrowserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import org.apache.commons.io.FileUtils
import java.io.File

@AndroidEntryPoint
@OptionalInject
class BrowserActivity : AppCompatActivity() {

    private lateinit var layout: ActivityBrowserBinding
    private val viewModel: BrowserViewModel by viewModels()
    private val settingsForResultCallback = ActivityResultCallback<ActivityResult> {
        exitAppIfFilesAccessPermissionIfNotGranted()
    }
    private val startSettingsForResult = registerForActivityResult(StartActivityForResult(),settingsForResultCallback)
    private val permissionRequestCallback = ActivityResultCallback<Boolean> { isGranted ->
        if (!isGranted) {
            // Permission is not granted, exit app
            finish()
        }
    }
    private val requestPermission = registerForActivityResult(RequestPermission(),permissionRequestCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        layout = DataBindingUtil.setContentView(this,R.layout.activity_browser)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup toolbar
        setSupportActionBar(layout.toolbar)

        // Setup browsed files adapter
        val adapter = FilesAdapter(::onFileClick,::onFileMenuClick)
        layout.browsedFiles.adapter = adapter

        // Set bottom nav bar
        layout.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_all -> {
                    if (!item.isChecked) {
                        viewModel.browseCategory(BrowsedCategory.ALL)
                    }
                    true
                }
                R.id.nav_audio -> {
                    if (!item.isChecked) {
                        viewModel.browseCategory(BrowsedCategory.AUDIO)
                    }
                    true
                }
                R.id.nav_video -> {
                    if (!item.isChecked) {
                        viewModel.browseCategory(BrowsedCategory.VIDEO)
                    }
                    true
                }
                R.id.nav_image -> {
                    if (!item.isChecked) {
                        viewModel.browseCategory(BrowsedCategory.IMAGE)
                    }
                    true
                }
                R.id.nav_download -> {
                    if (!item.isChecked) {
                        viewModel.browseCategory(BrowsedCategory.DOWNLOADS)
                    }
                    true
                }
                else -> false
            }
        }

        // Check for storage permission
        askFilesAccessPermissionIfNotGranted()

        // Observe ui state
        viewModel.uiState.observe(this) {
            // Set browsed files list
            adapter.submitList(it.files)

            // Show current browsed folder path
            layout.currentPathLabel.text = it.currentPath

            // Show error message if one exist
            if (it.error != null) { handleErrorUpdate(it.error) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_browser, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.action_nav_search -> {
                startActivity(Intent(this,SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (viewModel.isRootFolder()) {
            // Show exit app dialog
            showExitAppDialog { super.onBackPressed() }
        } else {
            // Don't exit app, nav up from current browsed folder
            viewModel.navUpFromFolder()
        }
    }

    private fun askFilesAccessPermissionIfNotGranted() {
        // Check runtime permission for storage access since app feature all require this permission
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                !Environment.isExternalStorageManager()
            } else {
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
            }
        ) {
            // Permission is not yet granted
            // Ask the user for the needed permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                openPermissionInDeviceSettings()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    openPermissionInAppSettings()
                } else {
                    requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun exitAppIfFilesAccessPermissionIfNotGranted() {
        // Check runtime permission for storage access since app feature all require this permission
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                !Environment.isExternalStorageManager()
            } else {
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
            }
        ) {
            // Permission is not yet granted, exit app
            finish()
        }
    }

    private fun openPermissionInAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startSettingsForResult.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openPermissionInDeviceSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startSettingsForResult.launch(intent)
    }

    private fun onFileMenuClick(file: DeviceFile, view: View) {
        // Show file options menu
        PopupMenu(this, view).apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_detail -> {
                        // Show file detail ui
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

    private fun onFileClick(file: DeviceFile) {
        // If file is a dir, browse its files, else open file with device app
        if (file.type == DeviceFileType.DIR) {
            browseFolderFiles(file.path)
        } else {
            openFileWithDeviceApp(file)
        }
    }

    private fun browseFolderFiles(path: String) {
        viewModel.browseFolder(path)
    }

    private fun openFileWithDeviceApp(file: DeviceFile) {
        val uri = FileProvider.getUriForFile(
            applicationContext,
            applicationContext.packageName.plus(".provider"),
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

    private fun showFileDetail(file: DeviceFile) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_dialog_file_detail))
            .setMessage(
                getString(R.string.file_detail,
                    file.name,
                    file.path,
                    FileUtils.byteCountToDisplaySize(file.size))
            )
            .setPositiveButton(getString(R.string.button_dialog_positive)) { dialog, which -> dialog.dismiss() }
            .show()
    }

    private fun showExitAppDialog(exitAction: () -> (Unit)) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_dialog_exit_app))
            .setMessage(getString(R.string.message_dialog_exit_app))
            .setPositiveButton(getString(R.string.button_dialog_positive)) { _, _ -> exitAction.invoke() }
            .setNegativeButton(getString(R.string.button_dialog_negative)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun handleErrorUpdate(error: BrowserError) {
        when(error) {
            is BrowserError.NonExistingDir -> handleErrorNonExistingDir()
            is BrowserError.AccessDenied -> handleDirAccessDeniedError()
            is BrowserError.Internal -> handleBrowserFeatureError()
        }
    }

    private fun handleErrorNonExistingDir() {
        (layout.browsedFiles.adapter as FilesAdapter).submitList(emptyList())
        showFileErrorDialog(getString(R.string.error_message_dir_non_exist))
    }

    private fun handleDirAccessDeniedError() {
        (layout.browsedFiles.adapter as FilesAdapter).submitList(emptyList())
        showFileErrorDialog(getString(R.string.error_message_access_denied))
    }

    private fun handleBrowserFeatureError() {
        showFileErrorDialog(getString(R.string.error_message_browser_feature))
    }

    private fun showFileErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_dialog_error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.button_dialog_positive)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}