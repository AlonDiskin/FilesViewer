package com.alon.filesviewer.browser.ui.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alon.filesviewer.browser.domain.model.DeviceFile
import com.alon.filesviewer.browser.ui.databinding.FileBinding

class FilesAdapter(
    private val clickListener: (DeviceFile) -> (Unit),
    private val menuClickListener: (DeviceFile, View) -> (Unit)
) : ListAdapter<DeviceFile,FilesAdapter.FileViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeviceFile>() {

            override fun areItemsTheSame(oldItem: DeviceFile, newItem: DeviceFile): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: DeviceFile, newItem: DeviceFile) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = FileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding,clickListener,menuClickListener)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class FileViewHolder(
        private val binding: FileBinding,
        clickListener: (DeviceFile) -> (Unit),
        menuClickListener: (DeviceFile,View) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clickListener = clickListener
            binding.menuClickListener = menuClickListener
        }

        fun bind(file: DeviceFile) {
            binding.file = file
        }
    }
}