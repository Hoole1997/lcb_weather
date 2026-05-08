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
    primary = Color(0xFF1D5F7A),
    onPrimary = Color(0xFFF7FCFF),
    primaryContainer = Color(0xFFC7E8F5),
    onPrimaryContainer = Color(0xFF103746),
    secondary = Color(0xFF6B5B2D),
    onSecondary = Color(0xFFFFFAEC),
    secondaryContainer = Color(0xFFF2E2B4),
    onSecondaryContainer = Color(0xFF3E3418),
    tertiary = Color(0xFF7A4F5F),
    onTertiary = Color(0xFFFFF8FA),
    tertiaryContainer = Color(0xFFF4D5DD),
    onTertiaryContainer = Color(0xFF4B2A35),
    background = Color(0xFFF4F7F4),
    onBackground = Color(0xFF18201D),
    surface = Color(0xFFFBFCF7),
    onSurface = Color(0xFF18201D),
    surfaceVariant = Color(0xFFE0E7E2),
    onSurfaceVariant = Color(0xFF3F4944),
    outline = Color(0xFF707A75)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF91D0E5),
    onPrimary = Color(0xFF123543),
    primaryContainer = Color(0xFF17485C),
    onPrimaryContainer = Color(0xFFD2EFF8),
    secondary = Color(0xFFDCC67E),
    onSecondary = Color(0xFF3A3012),
    secondaryContainer = Color(0xFF51451F),
    onSecondaryContainer = Color(0xFFF4E4AF),
    tertiary = Color(0xFFE3B7C4),
    onTertiary = Color(0xFF462631),
    tertiaryContainer = Color(0xFF5E3946),
    onTertiaryContainer = Color(0xFFFFD9E3),
    background = Color(0xFF101513),
    onBackground = Color(0xFFE2E8E3),
    surface = Color(0xFF171D1A),
    onSurface = Color(0xFFE2E8E3),
    surfaceVariant = Color(0xFF3F4944),
    onSurfaceVariant = Color(0xFFC0C9C4),
    outline = Color(0xFF89938D)
)

@Composable
fun WeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
