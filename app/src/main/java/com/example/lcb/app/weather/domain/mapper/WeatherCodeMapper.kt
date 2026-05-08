package com.example.lcb.app.weather.domain.mapper

import com.example.lcb.app.weather.domain.model.WeatherIcon

data class WeatherCondition(
    val code: Int,
    val text: String,
    val icon: WeatherIcon
)

object WeatherCodeMapper {
    fun map(code: Int): WeatherCondition {
        val (text, icon) = when (code) {
            0 -> "晴" to WeatherIcon.Clear
            1 -> "大部晴朗" to WeatherIcon.PartlyCloudy
            2 -> "局部多云" to WeatherIcon.PartlyCloudy
            3 -> "阴" to WeatherIcon.Cloudy
            45, 48 -> "雾" to WeatherIcon.Fog
            51 -> "小毛毛雨" to WeatherIcon.Drizzle
            53 -> "毛毛雨" to WeatherIcon.Drizzle
            55 -> "强毛毛雨" to WeatherIcon.Drizzle
            56, 57 -> "冻毛毛雨" to WeatherIcon.FreezingRain
            61 -> "小雨" to WeatherIcon.Rain
            63 -> "中雨" to WeatherIcon.Rain
            65 -> "大雨" to WeatherIcon.Rain
            66, 67 -> "冻雨" to WeatherIcon.FreezingRain
            71 -> "小雪" to WeatherIcon.Snow
            73 -> "中雪" to WeatherIcon.Snow
            75 -> "大雪" to WeatherIcon.Snow
            77 -> "雪粒" to WeatherIcon.Snow
            80 -> "小阵雨" to WeatherIcon.Shower
            81 -> "阵雨" to WeatherIcon.Shower
            82 -> "强阵雨" to WeatherIcon.Shower
            85 -> "小阵雪" to WeatherIcon.Snow
            86 -> "强阵雪" to WeatherIcon.Snow
            95 -> "雷暴" to WeatherIcon.Thunderstorm
            96, 99 -> "雷暴伴冰雹" to WeatherIcon.Thunderstorm
            else -> "未知天气" to WeatherIcon.Unknown
        }
        return WeatherCondition(code = code, text = text, icon = icon)
    }
}
