package com.example.photodiary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonthlyReflectionDao {
    @Query("SELECT * FROM monthly_reflections")
    suspend fun getAll(): List<MonthlyReflectionEntity>

    @Query("SELECT * FROM monthly_reflections WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getByYearMonth(yearMonth: String): MonthlyReflectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MonthlyReflectionEntity)

    @Query("UPDATE monthly_reflections SET coverImagePath = '' WHERE coverImagePath = :path")
    suspend fun clearCoverImagePath(path: String)
}
