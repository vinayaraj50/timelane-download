package com.timelane.core.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalTime

enum class TimeOfDay {
    DAWN,     // 05:00 - 07:00
    MORNING,  // 07:00 - 12:00
    AFTERNOON,// 12:00 - 17:00
    DUSK,     // 17:00 - 19:00
    EVENING,  // 19:00 - 21:00
    NIGHT     // 21:00 - 05:00
}

data class TimeThemeColors(
    val sky: Color,
    val road: Color,
    val roadMarking: Color,
    val ambientLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val dashboardBg: Color,
    val dashboardAccent: Color,
    val neoShadowLight: Color,
    val neoShadowDark: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val surfaceHigh: Color,
    val eventPalette: List<Color>
)

object TimeTheme {
    private val PALETTE_GLASS_DAY = TimeThemeColors(
        sky = Color(0xFFE8F0F8),
        road = Color(0xFFF0F4F8),
        roadMarking = Color(0xFFB0C4DE),
        ambientLight = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFF8FAFC), // Light text for dark glass
        textSecondary = Color(0xFF94A3B8),
        accent = Color(0xFF00D1FF), // Studio Cyan
        dashboardBg = Color(0xE60A0F14), // Premium Dark Glass (90% alpha, deep black-blue)
        dashboardAccent = Color(0xFF1E293B),
        neoShadowLight = Color(0x80FFFFFF),
        neoShadowDark = Color(0x20000000),
        glowPrimary = Color(0xFF00D1FF),
        glowSecondary = Color(0xFFFF00C7),
        surfaceHigh = Color(0xFFF9FAFB),
        eventPalette = listOf(
            Color(0xFFFF00C7), // Magenta
            Color(0xFF00D1FF), // Cyan
            Color(0xFF007AFF), // Azure
            Color(0xFF34C759), // Green
            Color(0xFFFF9500), // Orange
            Color(0xFFFF2D55), // Red
            Color(0xFF5856D6), // Indigo
            Color(0xFFAF52DE)  // Purple
        )
    )

    private val PALETTE_GLASS_NIGHT = TimeThemeColors(
        sky = Color(0xFF020617), 
        road = Color(0xFF020617),
        roadMarking = Color(0xFF1E293B),
        ambientLight = Color(0xFF0F172A),
        textPrimary = Color(0xFFF8FAFC), 
        textSecondary = Color(0xFF94A3B8),
        accent = Color(0xFFFF4081),
        dashboardBg = Color(0xCC020617), // Deep Studio Dark
        dashboardAccent = Color(0xFF1E293B),
        neoShadowLight = Color(0x10FFFFFF),
        neoShadowDark = Color(0x80000000),
        glowPrimary = Color(0xFF00E5FF),
        glowSecondary = Color(0xFFFF007F),
        surfaceHigh = Color(0xFF0F172A),
        eventPalette = listOf(
            Color(0xFFFF007F), // Neon Pink
            Color(0xFF00E5FF), // Neon Cyan
            Color(0xFFFFD600), // Neon Yellow
            Color(0xFF7C4DFF), // Neon Deep Purple
            Color(0xFF64FFDA), // Neon Teal
            Color(0xFFFFAB40), // Neon Orange
            Color(0xFFE040FB), // Neon Purple
            Color(0xFFEEFF41)  // Neon Lime
        )
    )

    fun getThemeForTime(time: LocalTime): TimeThemeColors {
        val hour = time.hour
        return when (hour) {
             in 6..17 -> PALETTE_GLASS_DAY // 6 AM to 5 PM
             else -> PALETTE_GLASS_NIGHT
        }
    }
    
    fun getTimeOfDay(time: LocalTime): TimeOfDay {
        val hour = time.hour
        return when (hour) {
             in 5..6 -> TimeOfDay.DAWN
             in 7..11 -> TimeOfDay.MORNING
             in 12..16 -> TimeOfDay.AFTERNOON
             in 17..18 -> TimeOfDay.DUSK
             in 19..20 -> TimeOfDay.EVENING
             else -> TimeOfDay.NIGHT
        }
    }
}
