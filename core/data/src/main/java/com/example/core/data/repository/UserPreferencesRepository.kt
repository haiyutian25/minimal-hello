package com.example.core.data.repository

import com.example.core.data.datastore.UserPreferencesDataStore
import com.example.core.data.model.UserPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Data-layer entry point for reading and updating persisted UI preferences.
 */
interface UserPreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun updateTheme(themeId: String)
    suspend fun updateTypography(typographyChoice: String)
    suspend fun updateColorMode(colorMode: String)
    suspend fun updateFontScale(fontScale: Float)
    suspend fun updateActiveCustomFont(fontId: String)
}

/**
 * Preferences DataStore-backed implementation. Reads expose a [Flow]; each
 * update atomically edits the stored preferences.
 */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : UserPreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> =
        userPreferencesDataStore.preferences

    override suspend fun updateTheme(themeId: String) =
        userPreferencesDataStore.update { it.copy(themeId = themeId) }

    override suspend fun updateTypography(typographyChoice: String) =
        userPreferencesDataStore.update { it.copy(typographyChoice = typographyChoice) }

    override suspend fun updateColorMode(colorMode: String) =
        userPreferencesDataStore.update { it.copy(colorMode = colorMode) }

    override suspend fun updateFontScale(fontScale: Float) =
        userPreferencesDataStore.update { it.copy(fontScale = fontScale) }

    override suspend fun updateActiveCustomFont(fontId: String) =
        userPreferencesDataStore.update { it.copy(activeCustomFontId = fontId) }
}
