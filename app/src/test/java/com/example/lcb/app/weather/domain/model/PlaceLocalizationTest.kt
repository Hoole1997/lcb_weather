package com.example.lcb.app.weather.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class PlaceLocalizationTest {
    @Test
    fun `saved city subtitle localizes country by country code`() {
        val city = SavedCity(
            id = "berlin",
            name = "Berlin",
            country = "Deutschland",
            countryCode = "DE",
            admin1 = "Berlin",
            latitude = 52.52,
            longitude = 13.405,
            sortIndex = 0
        )

        assertEquals("Germany", city.subtitle(Locale.ENGLISH))
        assertEquals("Allemagne", city.subtitle(Locale.FRENCH))
    }
}
