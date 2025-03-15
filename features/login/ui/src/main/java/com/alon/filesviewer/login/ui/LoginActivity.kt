package com.alon.filesviewer.login.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject lateinit var navigator: LoginNavigator
    private val settingsForResultCallback = ActivityResultCallback<ActivityResult> {
        exitAppIfNoPermissionGrantedOrLaunchHome()
    }
    private val startSettingsForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),settingsForResultCallback)
    private val permissionRequestCallback = ActivityResultCallback<Boolean> { isGranted ->
        if (isGranted) {
            launchHomeScreen()
        } else {
            finish()
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission(),permissionRequestCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.topAppBar))

        // Check permission
        checkFilesAccessPermission()
    }

    private fun checkFilesAccessPermission() {
        // Check runtime permission for storage access since app feature all require this permission
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
            }
        ) {
            // Permission is already granted
            launchHomeScreen()

        } else {
            // Permission is not yet granted
            // Ask the user for the needed permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                openPermissionInDeviceSettings()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    openPermissionInAppSettings()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun launchHomeScreen() {
        startActivity(navigator.getHomeScreenIntent())
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openPermissionInDeviceSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startSettingsForResult.launch(intent)
    }

    private fun openPermissionInAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startSettingsForResult.launch(intent)
    }

    private fun exitAppIfNoPermissionGrantedOrLaunchHome() {
        // Check runtime permission for storage access since app feature all require this permission
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
            }
        ) {
            // Permission is already granted
            launchHomeScreen()

        } else {
            // Permission is not yet granted
            finish()
        }
    }

}