package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserPreferencesEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        const val NAME = "minimal-hello.db"
    }
}
