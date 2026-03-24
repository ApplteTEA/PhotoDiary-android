package com.appletea.photodiary

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries ORDER BY diaryDate DESC, createdAt DESC")
    suspend fun getAllOrdered(): List<DiaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiaryEntity): Long

    @Update
    suspend fun update(entity: DiaryEntity)

    @Delete
    suspend fun delete(entity: DiaryEntity)
}
