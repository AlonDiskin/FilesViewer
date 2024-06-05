package com.alon.filesviewer.browser.ui.controller

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.alon.filesviewer.browser.domain.model.BrowserError
import com.alon.filesviewer.browser.domain.model.SearchFilter
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.databinding.ActivitySearchBinding
import com.alon.filesviewer.browser.ui.viewmodel.SearchViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private lateinit var layout: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init layout
        enableEdgeToEdge()
        layout = DataBindingUtil.setContentView(this,R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup toolbar
        setSupportActionBar(layout.toolbar)

        // Set search results recycler view
        val adapter = SearchResultsAdapter()
        layout.searchResults.adapter = adapter

        // Set search filters chip group listener
        viewModel.searchUiState.value!!.filter.let { filter ->
            val checkId = when(filter) {
                SearchFilter.ALL -> R.id.chipFilterAll
                SearchFilter.IMAGE -> R.id.chipFilterImages
                SearchFilter.VIDEO -> R.id.chipFilterVideos
                SearchFilter.AUDIO -> R.id.chipFilterAudio
                SearchFilter.DOWNLOAD -> R.id.chipFilterDownloads
            }
            layout.chipGroup.check(checkId)
        }
        layout.chipGroup.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            when(chipGroup.checkedChipId) {
                R.id.chipFilterAll -> viewModel.setFilter(SearchFilter.ALL)
                R.id.chipFilterImages -> viewModel.setFilter(SearchFilter.IMAGE)
                R.id.chipFilterVideos -> viewModel.setFilter(SearchFilter.VIDEO)
                R.id.chipFilterAudio -> viewModel.setFilter(SearchFilter.AUDIO)
                R.id.chipFilterDownloads -> viewModel.setFilter(SearchFilter.DOWNLOAD)
            }
        }

        // Observe ui state changes
        viewModel.searchUiState.observe(this) { state ->
            // Update search results
            adapter.submitList(state.results)

            // Handle error
            handleError(state.error)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate toolbar menu
        menuInflater.inflate(R.menu.menu_search,menu)

        // Set searchView
        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView

        searchItem.expandActionView()
        searchView.setIconifiedByDefault(true)
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setQuery(viewModel.searchUiState.value!!.query,false)
        searchView.setOnQueryTextListener(this)
        searchItem.setOnActionExpandListener(this)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { query -> viewModel.setQuery(query) }
        return true
    }

    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        layout.chipGroup.visibility = View.INVISIBLE
        onBackPressed()
        return true
    }

    private fun handleError(error: BrowserError?) {
        error?.let { browserError ->
               when(browserError) {
                   is BrowserError.Internal -> {
                       val snackbar = Snackbar.make(layout.main,
                           R.string.error_feature,
                           Snackbar.LENGTH_LONG
                       )
                       snackbar.setAction(R.string.action_done) {
                           snackbar.dismiss()
                           viewModel.clearError()
                       }
                       snackbar.show()
                   }
               }
        }
    }
}