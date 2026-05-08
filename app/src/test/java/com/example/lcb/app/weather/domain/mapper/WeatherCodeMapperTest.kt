package com.example.lcb.app.weather.domain.mapper

import com.example.lcb.app.weather.domain.model.WeatherIcon
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {
    @Test
    fun `maps common clear and rain codes`() {
        assertEquals(
            WeatherCondition(code = 0, text = "晴", icon = WeatherIcon.Clear),
            WeatherCodeMapper.map(0)
        )
        assertEquals(
            WeatherCondition(code = 63, text = "中雨", icon = WeatherIcon.Rain),
            WeatherCodeMapper.map(63)
        )
    }

    @Test
    fun `maps unknown code safely`() {
        assertEquals(
            WeatherCondition(code = -1, text = "未知天气", icon = WeatherIcon.Unknown),
            WeatherCodeMapper.map(-1)
        )
    }
}
