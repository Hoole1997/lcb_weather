package com.example.lcb.app.weather.domain.mapper

import com.example.lcb.app.weather.domain.model.PressureUnit
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.VisibilityUnit
import com.example.lcb.app.weather.domain.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherFormattersTest {
    @Test
    fun `formats weather units`() {
        assertEquals("23°C", UnitConverter.formatTemperature(22.6, TemperatureUnit.Celsius))
        assertEquals("9.4 km/h", UnitConverter.formatWindSpeed(9.36, WindSpeedUnit.KilometersPerHour))
        assertEquals("760 mmHg", UnitConverter.formatPressure(1013.25, PressureUnit.MillimeterMercury))
        assertEquals("29.92 inHg", UnitConverter.formatPressure(1013.25, PressureUnit.InchMercury))
        assertEquals("6.2 mile", UnitConverter.formatVisibility(10_000.0, VisibilityUnit.Mile))
    }
}
