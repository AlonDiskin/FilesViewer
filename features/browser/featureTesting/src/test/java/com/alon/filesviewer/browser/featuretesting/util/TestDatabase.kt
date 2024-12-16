package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FakeAudioFile::class,FakeImageFile::class,FakeVideoFile::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase() {

    abstract fun audioDao(): FakeAudioDao

    abstract fun videoDao(): FakeVideoDao

    abstract fun imageDao(): FakeImageDao
}