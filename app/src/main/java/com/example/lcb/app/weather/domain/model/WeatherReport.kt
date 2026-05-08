package com.example.lcb.app.weather.domain.model

data class WeatherReport(
    val city: SavedCity,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val generatedAtEpochMillis: Long
)

data class CurrentWeather(
    val time: String,
    val temperature: Double,
    val apparentTemperature: Double?,
    val weatherCode: Int,
    val weatherText: String,
    val icon: WeatherIcon,
    val isDay: Boolean,
    val highTemperature: Double?,
    val lowTemperature: Double?,
    val relativeHumidity: Int?,
    val precipitation: Double?,
    val precipitationProbability: Int?,
    val cloudCover: Int?,
    val pressureHpa: Double?,
    val surfacePressureHpa: Double?,
    val windSpeed: Double?,
    val windDirectionDegrees: Int?,
    val windGusts: Double?,
    val visibilityMeters: Double?,
    val uvIndex: Double?,
    val sunrise: String?,
    val sunset: String?
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val apparentTemperature: Double?,
    val weatherCode: Int,
    val weatherText: String,
    val icon: WeatherIcon,
    val isDay: Boolean,
    val precipitationProbability: Int?,
    val relativeHumidity: Int?,
    val windSpeed: Double?,
    val windDirectionDegrees: Int?,
    val pressureHpa: Double?,
    val visibilityMeters: Double?,
    val uvIndex: Double?
)

data class DailyForecast(
    val date: String,
    val weatherCode: Int,
    val weatherText: String,
    val icon: WeatherIcon,
    val highTemperature: Double?,
    val lowTemperature: Double?,
    val apparentHighTemperature: Double?,
    val apparentLowTemperature: Double?,
    val sunrise: String?,
    val sunset: String?,
    val uvIndexMax: Double?,
    val precipitationProbabilityMax: Int?,
    val windSpeedMax: Double?,
    val dominantWindDirectionDegrees: Int?
)

enum class WeatherIcon {
    Clear,
    PartlyCloudy,
    Cloudy,
    Fog,
    Drizzle,
    Rain,
    FreezingRain,
    Snow,
    Shower,
    Thunderstorm,
    Unknown
}
