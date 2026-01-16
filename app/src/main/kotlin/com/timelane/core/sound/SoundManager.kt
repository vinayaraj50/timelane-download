package com.timelane.core.sound

import android.content.Context
import android.media.MediaPlayer
import com.timelane.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class NotificationSound(val label: String, val resId: Int?) {
    SILENT("SILENT", null),
    BELL("BELL", R.raw.bell_notification),
    CONFIRMATION("CONFIRMATION", R.raw.confirmation_tone),
    MELODIC_FLUTE("MELODIC FLUTE", R.raw.melodical_flute_music_notification)
}

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(sound: NotificationSound) {
        playSound(sound.resId ?: return)
    }

    fun playSound(resId: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.start()
    }
}
