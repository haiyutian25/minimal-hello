package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table persisting the user's UI preferences across restarts.
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val themeId: String,
    val typographyChoice: String,
    val colorMode: String,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
