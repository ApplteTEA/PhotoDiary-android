package com.example.photodiary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diaries")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diaryDate: Long,
    val title: String,
    val content: String,
    val imagePath: String?,
    val createdAt: Long,
    val updatedAt: Long
)
