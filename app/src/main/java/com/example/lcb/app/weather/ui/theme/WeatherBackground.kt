package com.example.lcb.app.weather.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.lcb.app.weather.domain.model.CurrentWeather
import com.example.lcb.app.weather.domain.model.WeatherIcon

/**
 * Multi-layer "sky" background used by the redesigned weather screens.
 * The base is a vertical gradient inferred from the current weather, and
 * a subtle radial glow layered on top adds depth (sun / moon halo).
 */
@Composable
fun WeatherSkyBackground(
    current: CurrentWeather?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = weatherPalette(current)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(palette.gradient))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.glow, Color.Transparent),
                        center = Offset(palette.glowX, palette.glowY),
                        radius = palette.glowRadius
                    )
                )
        )
        content()
    }
}

/** Same gradient + glow used inside other screens (city manager etc.). */
@Composable
fun StaticSkyBackground(
    palette: SkyPalette = NightPalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(palette.gradient))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.glow, Color.Transparent),
                        center = Offset(palette.glowX, palette.glowY),
                        radius = palette.glowRadius
                    )
                )
        )
        content()
    }
}

data class SkyPalette(
    val gradient: List<Color>,
    val glow: Color = Color.White.copy(alpha = 0.10f),
    val glowX: Float = 220f,
    val glowY: Float = 180f,
    val glowRadius: Float = 700f
)

private val DayClearPalette = SkyPalette(
    gradient = listOf(Color(0xFF1B4D8A), Color(0xFF3776B7), Color(0xFF6FAFD8)),
    glow = Color(0x66FFE9B0),
    glowX = 260f,
    glowY = 200f,
    glowRadius = 720f
)

private val DayCloudyPalette = SkyPalette(
    gradient = listOf(Color(0xFF3F576E), Color(0xFF6B8597), Color(0xFF8DA9BA)),
    glow = Color(0x33FFFFFF),
    glowX = 240f,
    glowY = 220f,
    glowRadius = 700f
)

private val DayRainPalette = SkyPalette(
    gradient = listOf(Color(0xFF2E4654), Color(0xFF4F6E7E), Color(0xFF7E97A5)),
    glow = Color(0x33C7E1ED),
    glowX = 220f,
    glowY = 220f,
    glowRadius = 720f
)

private val DaySnowPalette = SkyPalette(
    gradient = listOf(Color(0xFF4D6D85), Color(0xFF8AA9BC), Color(0xFFC9DDE6)),
    glow = Color(0x44FFFFFF),
    glowX = 200f,
    glowY = 180f,
    glowRadius = 700f
)

private val DayThunderPalette = SkyPalette(
    gradient = listOf(Color(0xFF22202C), Color(0xFF3D3B4D), Color(0xFF6E6B7B)),
    glow = Color(0x44C9B6FF),
    glowX = 240f,
    glowY = 240f,
    glowRadius = 700f
)

private val DayFogPalette = SkyPalette(
    gradient = listOf(Color(0xFF566875), Color(0xFF8D9BA5), Color(0xFFB6C0C7)),
    glow = Color(0x33FFFFFF),
    glowX = 220f,
    glowY = 220f,
    glowRadius = 700f
)

internal val NightPalette = SkyPalette(
    gradient = listOf(Color(0xFF0E1626), Color(0xFF1A2A44), Color(0xFF2C436A)),
    glow = Color(0x55B5C7FF),
    glowX = 260f,
    glowY = 180f,
    glowRadius = 720f
)

private val NightCloudyPalette = SkyPalette(
    gradient = listOf(Color(0xFF111522), Color(0xFF1F2638), Color(0xFF323A52)),
    glow = Color(0x33B5C7FF),
    glowX = 240f,
    glowY = 200f,
    glowRadius = 700f
)

private fun weatherPalette(current: CurrentWeather?): SkyPalette {
    val icon = current?.icon ?: WeatherIcon.Clear
    val isDay = current?.isDay ?: true
    return when {
        !isDay && (icon == WeatherIcon.Cloudy || icon == WeatherIcon.PartlyCloudy ||
            icon == WeatherIcon.Fog) -> NightCloudyPalette
        !isDay -> NightPalette
        icon == WeatherIcon.Rain || icon == WeatherIcon.Shower ||
            icon == WeatherIcon.Drizzle -> DayRainPalette
        icon == WeatherIcon.Snow || icon == WeatherIcon.FreezingRain -> DaySnowPalette
        icon == WeatherIcon.Thunderstorm -> DayThunderPalette
        icon == WeatherIcon.Fog -> DayFogPalette
        icon == WeatherIcon.Cloudy || icon == WeatherIcon.PartlyCloudy -> DayCloudyPalette
        else -> DayClearPalette
    }
}
