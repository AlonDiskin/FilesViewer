package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [TestMediaFile::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase() {

    abstract fun mediaFileDao(): TestMediaFileDao
}