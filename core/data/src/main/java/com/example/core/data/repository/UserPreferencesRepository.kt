package com.example.core.data.repository

import com.example.core.data.datastore.UserPreferencesDataStore
import com.example.core.data.manager.dispatcher.DispatcherManager
import com.example.core.data.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Data-layer entry point for reading and updating persisted UI preferences.
 */
interface UserPreferencesRepository {
    /** Hot stream of the persisted preferences, started eagerly at injection time. */
    val preferencesStateFlow: StateFlow<UserPreferences>
    suspend fun updateTheme(themeId: String)
    suspend fun updateTypography(typographyChoice: String)
    suspend fun updateColorMode(colorMode: String)
    suspend fun updateFontScale(fontScale: Float)
    suspend fun updateActiveCustomFont(fontId: String)
}

/**
 * Preferences DataStore-backed implementation. Reads expose a [StateFlow]; each
 * update atomically edits the stored preferences.
 */
class UserPreferencesRepositoryImpl(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    dispatcherManager: DispatcherManager,
) : UserPreferencesRepository {

    private val unconfinedScope = CoroutineScope(dispatcherManager.unconfined)

    override val preferencesStateFlow: StateFlow<UserPreferences> =
        userPreferencesDataStore
            .preferences
            .stateIn(
                scope = unconfinedScope,
                started = SharingStarted.Eagerly,
                initialValue = UserPreferences.DEFAULT,
            )

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
