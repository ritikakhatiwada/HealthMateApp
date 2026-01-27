package com.example.healthmate.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeManager(private val context: Context) {

    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    // Get theme preference (Default to False/Light if not set)
    val isDarkMode: Flow<Boolean> =
            context.dataStore.data.map { preferences -> preferences[IS_DARK_MODE] ?: false }

    // Toggle theme
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_DARK_MODE] ?: false
            preferences[IS_DARK_MODE] = !current
        }
    }

    // Set theme explicitly
    suspend fun setDarkMode(enable: Boolean) {
        context.dataStore.edit { preferences -> preferences[IS_DARK_MODE] = enable }
    }
}
