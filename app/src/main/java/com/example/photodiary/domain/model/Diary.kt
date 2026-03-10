package com.example.photodiary.domain.model

data class Diary(
    val id: Long,
    val diaryDate: Long,
    val title: String,
    val content: String,
    val imagePath: String?,
    val createdAt: Long,
    val updatedAt: Long
)
