package com.example.photodiary

fun DiaryEntity.toUiModel(): DiaryEntry = DiaryEntry(
    id = id,
    diaryDate = diaryDate,
    title = title,
    content = content,
    imagePath = imagePath,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DiaryEntry.toEntity(): DiaryEntity = DiaryEntity(
    id = id,
    diaryDate = diaryDate,
    title = title,
    content = content,
    imagePath = imagePath,
    createdAt = createdAt,
    updatedAt = updatedAt
)
