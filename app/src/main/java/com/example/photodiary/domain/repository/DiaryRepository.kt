package com.example.photodiary.domain.repository

import com.example.photodiary.domain.model.Diary
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun observeDiaries(): Flow<List<Diary>>
    suspend fun getDiaryById(id: Long): Diary?
    suspend fun insertDiary(diary: Diary): Long
    suspend fun updateDiary(diary: Diary)
    suspend fun deleteDiary(diary: Diary)
}
