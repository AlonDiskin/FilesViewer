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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.databinding.ActivityBrowserBinding

class BrowserActivity : AppCompatActivity() {

    private lateinit var layout: ActivityBrowserBinding
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
}