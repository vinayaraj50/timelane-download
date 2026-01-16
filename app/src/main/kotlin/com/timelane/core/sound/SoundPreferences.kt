package com.timelane.core.sound

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val INITIAL_SOUND_KEY = stringPreferencesKey("notification_sound")

    val selectedSound: Flow<NotificationSound> = dataStore.data.map { preferences ->
        val soundName = preferences[INITIAL_SOUND_KEY] ?: NotificationSound.BELL.name
        try {
            NotificationSound.valueOf(soundName)
        } catch (e: Exception) {
            NotificationSound.BELL
        }
    }

    suspend fun setSound(sound: NotificationSound) {
        dataStore.edit { preferences ->
            preferences[INITIAL_SOUND_KEY] = sound.name
        }
    }
}
