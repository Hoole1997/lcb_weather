package com.example.lcb.app.weather.ui.theme

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

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F6E96),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD5ECF7),
    onPrimaryContainer = Color(0xFF0E2A39),
    secondary = Color(0xFF4F6477),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE6F0),
    onSecondaryContainer = Color(0xFF1B2733),
    tertiary = Color(0xFF635A89),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6E1F8),
    onTertiaryContainer = Color(0xFF1F1A36),
    background = Color(0xFFF5F8FB),
    onBackground = Color(0xFF101418),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101418),
    surfaceVariant = Color(0xFFE6ECF1),
    onSurfaceVariant = Color(0xFF44515C),
    outline = Color(0xFF7B8893)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DC8E2),
    onPrimary = Color(0xFF0E2A39),
    primaryContainer = Color(0xFF1A4761),
    onPrimaryContainer = Color(0xFFD5ECF7),
    secondary = Color(0xFFB6C7D9),
    onSecondary = Color(0xFF1B2733),
    secondaryContainer = Color(0xFF2F3D4B),
    onSecondaryContainer = Color(0xFFDCE6F0),
    tertiary = Color(0xFFC2BBE2),
    onTertiary = Color(0xFF1F1A36),
    tertiaryContainer = Color(0xFF463F66),
    onTertiaryContainer = Color(0xFFE6E1F8),
    background = Color(0xFF0C1118),
    onBackground = Color(0xFFE2E8EE),
    surface = Color(0xFF131A22),
    onSurface = Color(0xFFE2E8EE),
    surfaceVariant = Color(0xFF2A323B),
    onSurfaceVariant = Color(0xFFB6C0CB),
    outline = Color(0xFF7B8893)
)

@Composable
fun WeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(context)
        }

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WeatherTypography,
        content = content
    )
}
