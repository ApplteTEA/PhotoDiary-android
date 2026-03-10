package com.example.photodiary.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.photodiary.data.local.dao.DiaryDao
import com.example.photodiary.data.local.entity.DiaryEntity

@Database(
    entities = [DiaryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}
