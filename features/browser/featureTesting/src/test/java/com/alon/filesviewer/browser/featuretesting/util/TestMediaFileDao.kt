package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TestMediaFileDao {

    @Insert
    fun insert(file: TestMediaFile)

    @Query("SELECT * FROM testmediafile")
    fun getAll(): List<TestMediaFile>
}