package com.example.lcb.app.weather.domain.mapper

import com.example.lcb.app.weather.domain.model.PressureUnit
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.VisibilityUnit
import com.example.lcb.app.weather.domain.model.WindSpeedUnit
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

object UnitConverter {
    fun formatTemperature(value: Double?, unit: TemperatureUnit): String {
        if (value == null) return "--"
        return "${value.roundToInt()}${unit.symbol}"
    }

    fun formatWindSpeed(value: Double?, unit: WindSpeedUnit): String {
        if (value == null) return "--"
        return "${oneDecimal(value)} ${unit.symbol}"
    }

    fun formatPressure(hpa: Double?, unit: PressureUnit): String {
        if (hpa == null) return "--"
        val value = when (unit) {
            PressureUnit.Hectopascal -> hpa
            PressureUnit.MillimeterMercury -> hpa * 0.750062
            PressureUnit.InchMercury -> hpa * 0.02953
        }
        val text = when (unit) {
            PressureUnit.Hectopascal -> value.roundToInt().toString()
            PressureUnit.MillimeterMercury -> value.roundToInt().toString()
            PressureUnit.InchMercury -> twoDecimals(value)
        }
        return "$text ${unit.symbol}"
    }

    fun formatVisibility(meters: Double?, unit: VisibilityUnit): String {
        if (meters == null) return "--"
        val kilometers = meters / 1000.0
        val value = when (unit) {
            VisibilityUnit.Kilometer -> kilometers
            VisibilityUnit.Mile -> kilometers * 0.621371
        }
        return "${oneDecimal(value)} ${unit.symbol}"
    }

    private fun oneDecimal(value: Double): String = decimal("0.#").format(value)

    private fun twoDecimals(value: Double): String = decimal("0.##").format(value)

    private fun decimal(pattern: String): DecimalFormat {
        return DecimalFormat(pattern).apply {
            roundingMode = RoundingMode.HALF_UP
        }
    }
}
