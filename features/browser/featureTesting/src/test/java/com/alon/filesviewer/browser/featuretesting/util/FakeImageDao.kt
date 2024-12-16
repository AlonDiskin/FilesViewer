package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FakeImageDao {

    @Insert
    fun insert(file: FakeImageFile)

    @Query("SELECT * FROM fakeimagefile")
    fun getAll(): List<FakeImageFile>
}