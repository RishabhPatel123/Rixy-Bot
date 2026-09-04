package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ImmersivePrimary,
    onPrimary = ImmersiveOnPrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersivePrimary,
    secondary = ImmersiveContainer,
    onSecondary = ImmersiveTextPrimary,
    secondaryContainer = ImmersiveContainer,
    onSecondaryContainer = ImmersiveTextPrimary,
    tertiary = ImmersiveActiveMint,
    onTertiary = Color(0xFF13381B),
    background = ImmersiveBg,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveTextMuted,
    outline = ImmersiveBorder,
    error = ImmersiveAlertCoral,
    onError = ImmersiveAlertOnCoral,
    errorContainer = ImmersiveAlertBg,
    onErrorContainer = ImmersiveAlertCoral
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = IndigoSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = EmeraldAccent,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek Command Center dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

