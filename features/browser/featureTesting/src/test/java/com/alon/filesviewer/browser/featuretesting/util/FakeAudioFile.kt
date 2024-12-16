package com.alon.filesviewer.browser.featuretesting.util

import android.provider.MediaStore
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FakeAudioFile(@PrimaryKey @ColumnInfo(name = MediaStore.MediaColumns.DATA) val path: String,
                         @ColumnInfo(name = MediaStore.MediaColumns.TITLE) val title: String,
                         @ColumnInfo(name = MediaStore.MediaColumns.SIZE) val size: Long,
                         @ColumnInfo(name = MediaStore.MediaColumns.DATE_MODIFIED) val modified: Long)