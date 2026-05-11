package com.example.lcb.app.weather.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.Celsius,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KilometersPerHour,
    val pressureUnit: PressureUnit = PressureUnit.Hectopascal,
    val visibilityUnit: VisibilityUnit = VisibilityUnit.Kilometer,
    val languageOption: LanguageOption = LanguageOption.System
)

@Serializable
enum class TemperatureUnit(val apiValue: String, val symbol: String) {
    Celsius("celsius", "°C"),
    Fahrenheit("fahrenheit", "°F")
}

@Serializable
enum class WindSpeedUnit(val apiValue: String, val symbol: String) {
    KilometersPerHour("kmh", "km/h"),
    MetersPerSecond("ms", "m/s"),
    MilesPerHour("mph", "mph"),
    Knots("kn", "kn")
}

@Serializable
enum class PressureUnit(val symbol: String) {
    Hectopascal("hPa"),
    MillimeterMercury("mmHg"),
    InchMercury("inHg")
}

@Serializable
enum class VisibilityUnit(val symbol: String) {
    Kilometer("km"),
    Mile("mile")
}

@Serializable
enum class LanguageOption(
    val localeTag: String?,
    private val apiLanguage: String?
) {
    System(null, null),
    ChineseSimplified("zh-CN", "zh"),
    ChineseTraditional("zh-TW", "zh"),
    English("en", "en"),
    Japanese("ja", "ja"),
    Korean("ko", "ko"),
    French("fr", "fr"),
    German("de", "de"),
    Spanish("es", "es"),
    Portuguese("pt", "pt"),
    Italian("it", "it"),
    Russian("ru", "ru");

    fun geocodingLanguage(defaultLanguage: String): String {
        return apiLanguage ?: defaultLanguage.takeIf { it in SupportedGeocodingLanguages } ?: "en"
    }

    private companion object {
        val SupportedGeocodingLanguages = setOf("zh", "en", "ja", "ko", "fr", "de", "es", "pt", "it", "ru")
    }
}
