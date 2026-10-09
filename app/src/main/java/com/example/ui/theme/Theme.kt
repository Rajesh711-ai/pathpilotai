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
    primary = IndigoLight,
    onPrimary = Slate950,
    primaryContainer = IndigoDark,
    onPrimaryContainer = Color.White,
    secondary = CyanLight,
    onSecondary = Slate950,
    secondaryContainer = CyanDark,
    onSecondaryContainer = Color.White,
    tertiary = VioletLight,
    background = Slate950,
    surface = Slate900,
    surfaceVariant = Slate850,
    onBackground = Slate100,
    onSurface = Slate100,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = RoseDanger
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoLight.copy(alpha = 0.2f),
    onPrimaryContainer = IndigoDark,
    secondary = CyanDark,
    onSecondary = Color.White,
    secondaryContainer = CyanLight.copy(alpha = 0.2f),
    onSecondaryContainer = CyanDark,
    tertiary = VioletTertiary,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = RoseDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tailored theme for consistent branding
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

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

