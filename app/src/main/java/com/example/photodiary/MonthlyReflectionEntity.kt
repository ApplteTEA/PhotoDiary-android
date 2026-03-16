package com.example.photodiary

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_reflections",
    indices = [Index(value = ["yearMonth"], unique = true)]
)
data class MonthlyReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: String,
    val coverImagePath: String = "",
    val reflectionText: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
