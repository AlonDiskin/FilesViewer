package com.alon.filesviewer.browser.ui.controller

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import com.alon.filesviewer.browser.domain.model.DeviceFilesCollection
import com.alon.filesviewer.browser.domain.model.DeviceNamedFolder
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.databinding.ActivityBrowserBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import javax.inject.Inject

@AndroidEntryPoint
@OptionalInject
class BrowserActivity : AppCompatActivity() {

    private lateinit var layout: ActivityBrowserBinding
    private val searchActivityLauncher = registerForActivityResult(StartActivityForResult(),::onSearchActivityResult)
    @Inject lateinit var navigator: BrowserNavigator

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

        // Show folder browsing fragment for root folder
        if (savedInstanceState == null) {
            showFolder(DeviceNamedFolder.ROOT)
        }

        // Add custom handling for back pressing
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitAppDialog {
                    this.remove()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

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
            R.id.action_nav_settings -> {
                startActivity(navigator.getSettingsScreenIntent())
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

    private fun showExitAppDialog(exitAction: () -> (Unit)) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_dialog_exit_app))
            .setMessage(getString(R.string.message_dialog_exit_app))
            .setPositiveButton(getString(R.string.button_dialog_positive)) { _, _ -> exitAction.invoke() }
            .setNegativeButton(getString(R.string.button_dialog_negative)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}