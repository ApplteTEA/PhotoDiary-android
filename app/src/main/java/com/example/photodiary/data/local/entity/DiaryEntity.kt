package com.example.photodiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val diaryDate: Long,
    val title: String,
    val content: String,
    val imagePath: String?,
    val createdAt: Long,
    val updatedAt: Long
)
