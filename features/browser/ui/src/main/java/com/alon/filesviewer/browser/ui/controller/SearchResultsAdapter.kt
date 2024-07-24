package com.alon.filesviewer.browser.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alon.filesviewer.browser.ui.data.FileUiState
import com.alon.filesviewer.browser.ui.databinding.FileBinding

class SearchResultsAdapter(
    private val clickListener: (FileUiState) -> (Unit)
) : ListAdapter<FileUiState,SearchResultsAdapter.FileViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FileUiState>() {

            override fun areItemsTheSame(oldItem: FileUiState, newItem: FileUiState): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: FileUiState, newItem: FileUiState) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = FileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding,clickListener)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class FileViewHolder(
        private val binding: FileBinding,
        clickListener: (FileUiState) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clickListener = clickListener
        }

        fun bind(file: FileUiState) {
            binding.file = file
        }
    }
}