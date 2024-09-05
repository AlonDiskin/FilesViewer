package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FakeVideoDao {

    @Insert
    fun insert(file: FakeVideoFile)

    @Query("SELECT * FROM fakevideofile")
    fun getAll(): List<FakeVideoFile>
}