package com.example.lcb.app.weather.ui.navigation

import android.net.Uri

sealed class WeatherRoute(val route: String) {
    data object Main : WeatherRoute("weather/main?cityId={cityId}") {
        fun create(cityId: String?): String {
            return if (cityId.isNullOrBlank()) {
                "weather/main"
            } else {
                "weather/main?cityId=${Uri.encode(cityId)}"
            }
        }
    }

    data object CityManager : WeatherRoute("weather/cities")
    data object AddCity : WeatherRoute("weather/add-city")
    data object Settings : WeatherRoute("weather/settings")
    data object About : WeatherRoute("weather/about")
}
