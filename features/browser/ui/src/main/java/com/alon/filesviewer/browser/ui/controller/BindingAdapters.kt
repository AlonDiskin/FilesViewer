package com.alon.filesviewer.browser.ui.controller

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.alon.filesviewer.browser.domain.model.DeviceFileType
import com.alon.filesviewer.browser.ui.R
import com.alon.filesviewer.browser.ui.data.FileUiState

@BindingAdapter("setFileIcon")
fun setFileIcon(imageView: ImageView, file: FileUiState) {
    when(file.type) {
        DeviceFileType.TEXT -> imageView.setImageResource(R.drawable.baseline_text_snippet_24)
        DeviceFileType.IMAGE -> imageView.setImageResource(R.drawable.round_photo_24)
        DeviceFileType.VIDEO -> imageView.setImageResource(R.drawable.round_play_circle_24)
        DeviceFileType.AUDIO -> imageView.setImageResource(R.drawable.round_audiotrack_48)
        DeviceFileType.DIR -> imageView.setImageResource(R.drawable.round_folder_open_24)
        DeviceFileType.OTHER -> imageView.setImageResource(R.drawable.baseline_question_mark_24)
    }
}