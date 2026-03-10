package com.example.photodiary.data.repository

import com.example.photodiary.data.local.dao.DiaryDao
import com.example.photodiary.data.local.entity.DiaryEntity
import com.example.photodiary.domain.model.Diary
import com.example.photodiary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun observeDiaries(): Flow<List<Diary>> {
        return diaryDao.observeDiaries().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDiaryById(id: Long): Diary? {
        return diaryDao.getDiaryById(id)?.toDomain()
    }

    override suspend fun insertDiary(diary: Diary): Long {
        return diaryDao.insertDiary(diary.toEntity())
    }

    override suspend fun updateDiary(diary: Diary) {
        diaryDao.updateDiary(diary.toEntity())
    }

    override suspend fun deleteDiary(diary: Diary) {
        diaryDao.deleteDiary(diary.toEntity())
    }
}

private fun DiaryEntity.toDomain(): Diary {
    return Diary(
        id = id,
        diaryDate = diaryDate,
        title = title,
        content = content,
        imagePath = imagePath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun Diary.toEntity(): DiaryEntity {
    return DiaryEntity(
        id = id,
        diaryDate = diaryDate,
        title = title,
        content = content,
        imagePath = imagePath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
