package com.timelane.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import com.timelane.R
import java.time.LocalTime

val LocalTimeTheme = staticCompositionLocalOf { 
    TimeTheme.getThemeForTime(LocalTime.now()) 
}



@Composable
fun TimeLaneTheme(
    forceTime: LocalTime? = null,
    content: @Composable () -> Unit
) {
    val currentTime = forceTime ?: LocalTime.now()
    val timeTheme = TimeTheme.getThemeForTime(currentTime)
    val timeOfDay = TimeTheme.getTimeOfDay(currentTime)
    
    // We use dark colors for Evening/Night/Dusk, light for others for basic Material integration
    // But our custom components will rely heavily on LocalTimeTheme
    val isDark = timeOfDay == TimeOfDay.NIGHT || timeOfDay == TimeOfDay.EVENING || timeOfDay == TimeOfDay.DUSK

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = timeTheme.accent,
            surface = timeTheme.dashboardBg,
            background = timeTheme.sky,
            onSurface = timeTheme.textPrimary
        )
    } else {
        lightColorScheme(
            primary = timeTheme.accent,
            surface = timeTheme.dashboardBg, // Even in day, dashboard is somewhat dark/dashboard-y usually? 
                                             // Actually, let's keep it consistent with the theme palette
            background = timeTheme.sky,
            onSurface = timeTheme.textPrimary
        )
    }

    val view = LocalView.current
    val context = LocalContext.current
    val googleSans = androidx.compose.runtime.remember {
        try {
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.timelane_font)
            if (typeface != null) FontFamily(typeface) else FontFamily.Default
        } catch (e: Exception) {
            FontFamily.Default
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (findActivity(context))?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.BLACK
                window.navigationBarColor = android.graphics.Color.BLACK
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalTimeTheme provides timeTheme) {
        val typography = androidx.compose.material3.Typography(
            displayLarge = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            displayMedium = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            displaySmall = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            headlineLarge = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            headlineMedium = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            headlineSmall = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            titleMedium = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            titleSmall = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            bodySmall = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            labelMedium = androidx.compose.ui.text.TextStyle(fontFamily = googleSans),
            labelSmall = androidx.compose.ui.text.TextStyle(fontFamily = googleSans)
        )
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

private fun findActivity(context: android.content.Context): Activity? {
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

