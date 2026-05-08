package com.example.lcb.app.weather.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun `forecast request constants include required app fields`() {
        assertEquals(10, WeatherRepository.DEFAULT_FORECAST_DAYS)
        assertTrue(WeatherRepository.CURRENT_FIELDS.contains("temperature_2m"))
        assertTrue(WeatherRepository.CURRENT_FIELDS.contains("apparent_temperature"))
        assertTrue(WeatherRepository.HOURLY_FIELDS.contains("precipitation_probability"))
        assertTrue(WeatherRepository.HOURLY_FIELDS.contains("visibility"))
        assertTrue(WeatherRepository.DAILY_FIELDS.contains("sunrise"))
        assertTrue(WeatherRepository.DAILY_FIELDS.contains("sunset"))
    }
}
