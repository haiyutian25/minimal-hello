package com.example.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = ${UserPreferencesEntity.SINGLE_ROW_ID}")
    fun observe(): Flow<UserPreferencesEntity?>

    @Upsert
    suspend fun upsert(entity: UserPreferencesEntity)
}
