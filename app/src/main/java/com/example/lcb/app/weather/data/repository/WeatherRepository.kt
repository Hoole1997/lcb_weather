package com.example.lcb.app.weather.data.repository

import com.example.lcb.app.weather.data.remote.OpenMeteoForecastApi
import com.example.lcb.app.weather.data.remote.dto.DailyWeatherDto
import com.example.lcb.app.weather.data.remote.dto.ForecastResponseDto
import com.example.lcb.app.weather.data.remote.dto.HourlyWeatherDto
import com.example.lcb.app.weather.domain.mapper.WeatherCodeMapper
import com.example.lcb.app.weather.domain.model.CurrentWeather
import com.example.lcb.app.weather.domain.model.DailyForecast
import com.example.lcb.app.weather.domain.model.HourlyForecast
import com.example.lcb.app.weather.domain.model.SavedCity
import com.example.lcb.app.weather.domain.model.WeatherReport
import com.example.lcb.app.weather.domain.model.WeatherSettings

class WeatherRepository(
    private val api: OpenMeteoForecastApi,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    suspend fun getWeather(
        city: SavedCity,
        settings: WeatherSettings,
        forecastDays: Int = DEFAULT_FORECAST_DAYS
    ): Result<WeatherReport> {
        return runCatching {
            val response = api.getForecast(
                latitude = city.latitude,
                longitude = city.longitude,
                timezone = city.timezone.ifBlank { "auto" },
                forecastDays = forecastDays,
                current = CURRENT_FIELDS,
                hourly = HOURLY_FIELDS,
                daily = DAILY_FIELDS,
                temperatureUnit = settings.temperatureUnit.apiValue,
                windSpeedUnit = settings.windSpeedUnit.apiValue
            )
            response.toWeatherReport(city, clock())
        }
    }

    private fun ForecastResponseDto.toWeatherReport(city: SavedCity, generatedAt: Long): WeatherReport {
        val currentDto = requireNotNull(current) { "Weather response missing current weather." }
        val dailyItems = daily.toDailyForecasts()
        val hourlyItems = hourly.toHourlyForecasts()
        val currentHour = hourlyItems.firstOrNull { it.time == currentDto.time } ?: hourlyItems.firstOrNull()
        val firstDay = dailyItems.firstOrNull()
        val code = currentDto.weatherCode ?: firstDay?.weatherCode ?: currentHour?.weatherCode ?: -1
        val condition = WeatherCodeMapper.map(code)

        return WeatherReport(
            city = city,
            timezone = timezone ?: city.timezone,
            current = CurrentWeather(
                time = currentDto.time.orEmpty(),
                temperature = currentDto.temperature ?: currentHour?.temperature ?: 0.0,
                apparentTemperature = currentDto.apparentTemperature ?: currentHour?.apparentTemperature,
                weatherCode = code,
                weatherText = condition.text,
                icon = condition.icon,
                isDay = currentDto.isDay != 0,
                highTemperature = firstDay?.highTemperature,
                lowTemperature = firstDay?.lowTemperature,
                relativeHumidity = currentDto.relativeHumidity ?: currentHour?.relativeHumidity,
                precipitation = currentDto.precipitation,
                precipitationProbability = currentHour?.precipitationProbability
                    ?: firstDay?.precipitationProbabilityMax,
                cloudCover = currentDto.cloudCover,
                pressureHpa = currentDto.pressureMsl ?: currentHour?.pressureHpa,
                surfacePressureHpa = currentDto.surfacePressure,
                windSpeed = currentDto.windSpeed ?: currentHour?.windSpeed,
                windDirectionDegrees = currentDto.windDirection ?: currentHour?.windDirectionDegrees,
                windGusts = currentDto.windGusts,
                visibilityMeters = currentHour?.visibilityMeters,
                uvIndex = currentHour?.uvIndex ?: firstDay?.uvIndexMax,
                sunrise = firstDay?.sunrise,
                sunset = firstDay?.sunset
            ),
            hourly = hourlyItems,
            daily = dailyItems,
            generatedAtEpochMillis = generatedAt
        )
    }

    private fun HourlyWeatherDto?.toHourlyForecasts(): List<HourlyForecast> {
        if (this == null) return emptyList()
        return time.mapIndexedNotNull { index, value ->
            val temperatureValue = temperature.getOrNull(index) ?: return@mapIndexedNotNull null
            val code = weatherCode.getOrNull(index) ?: -1
            val condition = WeatherCodeMapper.map(code)
            HourlyForecast(
                time = value,
                temperature = temperatureValue,
                apparentTemperature = apparentTemperature.getOrNull(index),
                weatherCode = code,
                weatherText = condition.text,
                icon = condition.icon,
                isDay = isDay.getOrNull(index) != 0,
                precipitationProbability = precipitationProbability.getOrNull(index),
                relativeHumidity = relativeHumidity.getOrNull(index),
                windSpeed = windSpeed.getOrNull(index),
                windDirectionDegrees = windDirection.getOrNull(index),
                pressureHpa = pressureMsl.getOrNull(index),
                visibilityMeters = visibility.getOrNull(index),
                uvIndex = uvIndex.getOrNull(index)
            )
        }
    }

    private fun DailyWeatherDto?.toDailyForecasts(): List<DailyForecast> {
        if (this == null) return emptyList()
        return time.mapIndexed { index, value ->
            val code = weatherCode.getOrNull(index) ?: -1
            val condition = WeatherCodeMapper.map(code)
            DailyForecast(
                date = value,
                weatherCode = code,
                weatherText = condition.text,
                icon = condition.icon,
                highTemperature = temperatureMax.getOrNull(index),
                lowTemperature = temperatureMin.getOrNull(index),
                apparentHighTemperature = apparentTemperatureMax.getOrNull(index),
                apparentLowTemperature = apparentTemperatureMin.getOrNull(index),
                sunrise = sunrise.getOrNull(index),
                sunset = sunset.getOrNull(index),
                uvIndexMax = uvIndexMax.getOrNull(index),
                precipitationProbabilityMax = precipitationProbabilityMax.getOrNull(index),
                windSpeedMax = windSpeedMax.getOrNull(index),
                dominantWindDirectionDegrees = windDirectionDominant.getOrNull(index)
            )
        }
    }

    companion object {
        const val DEFAULT_FORECAST_DAYS = 10

        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation," +
                "weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m," +
                "wind_direction_10m,wind_gusts_10m"

        const val HOURLY_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation_probability," +
                "weather_code,wind_speed_10m,wind_direction_10m,pressure_msl,visibility,uv_index,is_day"

        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max," +
                "apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_probability_max," +
                "wind_speed_10m_max,wind_direction_10m_dominant"
    }
}
