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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.DeviceNamedFolder
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.databinding.ActivityBrowserBinding
import com.alon.filesviewer.browser.ui.viewmodel.CollectionBrowserViewModel
import com.alon.filesviewer.browser.ui.viewmodel.FolderBrowserViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class BrowserActivity : AppCompatActivity() {

    private lateinit var layout: ActivityBrowserBinding
    private val searchActivityLauncher = registerForActivityResult(StartActivityForResult(),::onSearchActivityResult)
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

        // Check for storage permission
        askFilesAccessPermissionIfNotGranted()

        // Show folder browsing fragment for root folder
        if (savedInstanceState == null) {
            showFolder(DeviceNamedFolder.ROOT)
        }

        // Setup bottom navigation
        layout.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_all -> {
                    if (!item.isChecked) {
                        showFolder(DeviceNamedFolder.ROOT)
                    }
                    true
                }
                R.id.nav_download -> {
                    if (!item.isChecked) {
                        showFolder(DeviceNamedFolder.DOWNLOAD)
                    }
                    true
                }
                R.id.nav_audio -> {
                    if (!item.isChecked) {
                        showCollection(DeviceFilesCollection.AUDIO)
                    }
                    true
                }
                R.id.nav_video -> {
                    if (!item.isChecked) {
                        showCollection(DeviceFilesCollection.VIDEO)
                    }
                    true
                }
                R.id.nav_image -> {
                    if (!item.isChecked) {
                        showCollection(DeviceFilesCollection.IMAGE)
                    }
                    true
                }
                else -> false
            }
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
                searchActivityLauncher.launch(Intent(this,SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @VisibleForTesting(VisibleForTesting.PRIVATE)
    fun onSearchActivityResult(result: ActivityResult) {
        result.data?.let { data ->
            // Extract search result folder, and check bottom nav as browsing all files
            val folder = data.getStringExtra(SearchActivity.RESULT_DIR_PATH)!!
            layout.bottomNavigation.selectedItemId = R.id.nav_all

            showSearchResultFolder(folder)
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

    private fun showFolder(folder: DeviceNamedFolder) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view,
                BrowserFragmentsFactory.createFolderBrowserFragment(folder)
            )
        }
    }

    private fun showCollection(collection: DeviceFilesCollection) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view,
                BrowserFragmentsFactory.createCollectionBrowserFragment(collection)
            )
        }
    }

    private fun showSearchResultFolder(folder: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view,
                BrowserFragmentsFactory.createFolderBrowserFragment(folder)
            )
        }
    }
}