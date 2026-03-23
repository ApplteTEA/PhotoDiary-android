package com.example.photodiary

fun DiaryEntity.toUiModel(): DiaryEntry = DiaryEntry(
    id = id,
    diaryDate = diaryDate,
    title = title,
    content = content,
    imagePath = imagePath,
    mood = mood,
    weather = weather,
    tag = tag,
    sticker = sticker,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DiaryEntry.toEntity(): DiaryEntity = DiaryEntity(
    id = id,
    diaryDate = diaryDate,
    title = title,
    content = content,
    imagePath = imagePath,
    mood = mood,
    weather = weather,
    tag = tag,
    sticker = sticker,
    createdAt = createdAt,
    updatedAt = updatedAt
)
