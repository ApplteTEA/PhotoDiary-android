package com.example.photodiary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DiaryEntity::class, MonthlyReflectionEntity::class], version = 4, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun monthlyReflectionDao(): MonthlyReflectionDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null


        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diaries ADD COLUMN mood TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE diaries ADD COLUMN weather TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE diaries ADD COLUMN tag TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS monthly_reflections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        yearMonth TEXT NOT NULL,
                        coverImagePath TEXT NOT NULL,
                        reflectionText TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_monthly_reflections_yearMonth ON monthly_reflections(yearMonth)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diaries ADD COLUMN sticker TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "photodiary.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
