package com.example.lcb.app.weather.domain.mapper

import com.example.lcb.app.weather.domain.model.WeatherIcon

data class WeatherCondition(
    val code: Int,
    val text: String,
    val icon: WeatherIcon
)

object WeatherCodeMapper {
    fun map(code: Int): WeatherCondition {
        val (text, icon) = when (code) {
            0 -> "clear" to WeatherIcon.Clear
            1 -> "mainly_clear" to WeatherIcon.PartlyCloudy
            2 -> "partly_cloudy" to WeatherIcon.PartlyCloudy
            3 -> "overcast" to WeatherIcon.Cloudy
            45, 48 -> "fog" to WeatherIcon.Fog
            51 -> "light_drizzle" to WeatherIcon.Drizzle
            53 -> "drizzle" to WeatherIcon.Drizzle
            55 -> "heavy_drizzle" to WeatherIcon.Drizzle
            56, 57 -> "freezing_drizzle" to WeatherIcon.FreezingRain
            61 -> "light_rain" to WeatherIcon.Rain
            63 -> "moderate_rain" to WeatherIcon.Rain
            65 -> "heavy_rain" to WeatherIcon.Rain
            66, 67 -> "freezing_rain" to WeatherIcon.FreezingRain
            71 -> "light_snow" to WeatherIcon.Snow
            73 -> "moderate_snow" to WeatherIcon.Snow
            75 -> "heavy_snow" to WeatherIcon.Snow
            77 -> "snow_grains" to WeatherIcon.Snow
            80 -> "light_showers" to WeatherIcon.Shower
            81 -> "showers" to WeatherIcon.Shower
            82 -> "heavy_showers" to WeatherIcon.Shower
            85 -> "light_snow_showers" to WeatherIcon.Snow
            86 -> "heavy_snow_showers" to WeatherIcon.Snow
            95 -> "thunderstorm" to WeatherIcon.Thunderstorm
            96, 99 -> "thunderstorm_hail" to WeatherIcon.Thunderstorm
            else -> "unknown" to WeatherIcon.Unknown
        }
        return WeatherCondition(code = code, text = text, icon = icon)
    }
}
