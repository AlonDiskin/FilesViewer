package com.alon.filesviewer.browser.featuretesting.util

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FakeAudioDao {

    @Insert
    fun insert(file: FakeAudioFile)

    @Query("SELECT * FROM fakeaudiofile")
    fun getAll(): List<FakeAudioFile>
}