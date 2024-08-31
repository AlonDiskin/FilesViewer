package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestMediaFile(@PrimaryKey val _data: String, val title: String, val type: Int)