package com.example.lcb.app.weather.domain.model

data class WeatherSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.Celsius,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KilometersPerHour,
    val pressureUnit: PressureUnit = PressureUnit.Hectopascal,
    val visibilityUnit: VisibilityUnit = VisibilityUnit.Kilometer,
    val themeMode: ThemeMode = ThemeMode.System
)

enum class TemperatureUnit(val apiValue: String, val symbol: String, val label: String) {
    Celsius("celsius", "°C", "摄氏度"),
    Fahrenheit("fahrenheit", "°F", "华氏度")
}

enum class WindSpeedUnit(val apiValue: String, val symbol: String, val label: String) {
    KilometersPerHour("kmh", "km/h", "km/h"),
    MetersPerSecond("ms", "m/s", "m/s"),
    MilesPerHour("mph", "mph", "mph"),
    Knots("kn", "节", "节")
}

enum class PressureUnit(val symbol: String, val label: String) {
    Hectopascal("hPa", "hPa"),
    MillimeterMercury("mmHg", "mmHg"),
    InchMercury("inHg", "inHg")
}

enum class VisibilityUnit(val symbol: String, val label: String) {
    Kilometer("km", "km"),
    Mile("mile", "mile")
}

enum class ThemeMode(val label: String) {
    System("跟随系统"),
    Light("浅色"),
    Dark("深色")
}
