package com.example.lcb.app.weather.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lcb.app.weather.domain.model.WeatherIcon

/**
 * Maps a [WeatherIcon] (and optional day/night flag) to a Material vector
 * icon used across the redesigned UI. Replaces the old Chinese-character
 * glyphs (晴/云/阴…) with proper iconography.
 */
fun WeatherIcon.toVector(isDay: Boolean = true): ImageVector = when (this) {
    WeatherIcon.Clear -> if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay
    WeatherIcon.PartlyCloudy -> if (isDay) Icons.Filled.WbCloudy else Icons.Filled.Nightlight
    WeatherIcon.Cloudy -> Icons.Filled.Cloud
    WeatherIcon.Fog -> Icons.Filled.Cloud
    WeatherIcon.Drizzle -> Icons.Filled.Grain
    WeatherIcon.Rain -> Icons.Filled.BeachAccess
    WeatherIcon.FreezingRain -> Icons.Filled.AcUnit
    WeatherIcon.Snow -> Icons.Filled.AcUnit
    WeatherIcon.Shower -> Icons.Filled.BeachAccess
    WeatherIcon.Thunderstorm -> Icons.Filled.Thunderstorm
    WeatherIcon.Unknown -> Icons.Filled.Help
}

/** Icons used in the metrics grid. */
object MetricIcons {
    val Humidity: ImageVector = Icons.Filled.WaterDrop
    val Wind: ImageVector = Icons.Filled.Air
    val Pressure: ImageVector = Icons.Filled.Compress
    val Visibility: ImageVector = Icons.Filled.Visibility
    val Uv: ImageVector = Icons.Filled.WbSunny
    val Precipitation: ImageVector = Icons.Filled.Opacity
    val SunCycle: ImageVector = Icons.Filled.WbTwilight
    val CloudGeneric: ImageVector = Icons.Filled.FilterDrama
}
