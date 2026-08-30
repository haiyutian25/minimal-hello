package com.example.core.data.repository

import com.example.core.data.model.UserPreferences
import com.example.core.database.UserPreferencesDao
import com.example.core.database.UserPreferencesEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
) : UserPreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> =
        userPreferencesDao.observe().map { entity ->
            entity?.let {
                UserPreferences(themeId = it.themeId, typographyChoice = it.typographyChoice, colorMode = it.colorMode, fontScale = it.fontScale, activeCustomFontId = it.activeCustomFontId)
            } ?: UserPreferences.DEFAULT
        }

    override suspend fun updateTheme(themeId: String) {
        val current = userPreferencesDao.observe().first()
        userPreferencesDao.upsert(
            current?.copy(themeId = themeId)
                ?: UserPreferencesEntity(
                    themeId = themeId,
                    typographyChoice = UserPreferences.DEFAULT.typographyChoice,
                    colorMode = UserPreferences.DEFAULT.colorMode,
                    fontScale = UserPreferences.DEFAULT.fontScale,
                    activeCustomFontId = UserPreferences.DEFAULT.activeCustomFontId,
                )
        )
    }

    override suspend fun updateTypography(typographyChoice: String) {
        val current = userPreferencesDao.observe().first()
        userPreferencesDao.upsert(
            current?.copy(typographyChoice = typographyChoice)
                ?: UserPreferencesEntity(
                    themeId = UserPreferences.DEFAULT.themeId,
                    typographyChoice = typographyChoice,
                    colorMode = UserPreferences.DEFAULT.colorMode,
                    fontScale = UserPreferences.DEFAULT.fontScale,
                    activeCustomFontId = UserPreferences.DEFAULT.activeCustomFontId,
                )
        )
    }

    override suspend fun updateColorMode(colorMode: String) {
        val current = userPreferencesDao.observe().first()
        userPreferencesDao.upsert(
            current?.copy(colorMode = colorMode)
                ?: UserPreferencesEntity(
                    themeId = UserPreferences.DEFAULT.themeId,
                    typographyChoice = UserPreferences.DEFAULT.typographyChoice,
                    colorMode = colorMode,
                    fontScale = UserPreferences.DEFAULT.fontScale,
                    activeCustomFontId = UserPreferences.DEFAULT.activeCustomFontId,
                )
        )
    }

    override suspend fun updateFontScale(fontScale: Float) {
        val current = userPreferencesDao.observe().first()
        userPreferencesDao.upsert(
            current?.copy(fontScale = fontScale)
                ?: UserPreferencesEntity(
                    themeId = UserPreferences.DEFAULT.themeId,
                    typographyChoice = UserPreferences.DEFAULT.typographyChoice,
                    colorMode = UserPreferences.DEFAULT.colorMode,
                    fontScale = fontScale,
                    activeCustomFontId = UserPreferences.DEFAULT.activeCustomFontId,
                )
        )
    }

    override suspend fun updateActiveCustomFont(fontId: String) {
        val current = userPreferencesDao.observe().first()
        userPreferencesDao.upsert(
            current?.copy(activeCustomFontId = fontId)
                ?: UserPreferencesEntity(
                    themeId = UserPreferences.DEFAULT.themeId,
                    typographyChoice = UserPreferences.DEFAULT.typographyChoice,
                    colorMode = UserPreferences.DEFAULT.colorMode,
                    fontScale = UserPreferences.DEFAULT.fontScale,
                    activeCustomFontId = fontId,
                )
        )
    }
}
