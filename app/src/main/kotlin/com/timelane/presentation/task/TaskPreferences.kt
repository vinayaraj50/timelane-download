package com.timelane.presentation.task

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "task_settings")

@Singleton
class TaskPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val MOVE_COMPLETED_TO_BOTTOM = booleanPreferencesKey("move_completed_to_bottom")
    private val FONT_SIZE = floatPreferencesKey("font_size")
    private val EVENT_FONT_SIZE = floatPreferencesKey("event_font_size")

    val fontSize: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_SIZE] ?: 16f
        }

    val eventFontSize: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[EVENT_FONT_SIZE] ?: 16f
        }

    val moveCompletedToBottom: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MOVE_COMPLETED_TO_BOTTOM] ?: false
        }

    suspend fun setMoveCompletedToBottom(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOVE_COMPLETED_TO_BOTTOM] = enabled
        }
    }

    suspend fun setFontSize(fontSize: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = fontSize
        }
    }

    suspend fun setEventFontSize(fontSize: Float) {
        context.dataStore.edit { preferences ->
            preferences[EVENT_FONT_SIZE] = fontSize
        }
    }
}
