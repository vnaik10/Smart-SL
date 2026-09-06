package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkEmergencyColorScheme = darkColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedContainer,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = TechCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = TechCyan,
    tertiary = CorridorActiveGreen,
    onTertiary = Color(0xFF00382E),
    tertiaryContainer = CorridorGreenDark,
    onTertiaryContainer = CorridorActiveGreen,
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    error = EmergencyRed,
    onError = Color.White
)

private val LightEmergencyColorScheme = lightColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = EmergencyRedDark,
    secondary = Color(0xFF006876),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9EEFFF),
    onSecondaryContainer = Color(0xFF001F24),
    tertiary = Color(0xFF006B5B),
    onTertiary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF191C1E),
    surface = Color.White,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFE7EDF0),
    onSurfaceVariant = Color(0xFF40484C),
    outline = Color(0xFF70787D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to tactical high-contrast dark theme for emergency services
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkEmergencyColorScheme else LightEmergencyColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

