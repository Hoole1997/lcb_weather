package com.example.lcb.app.weather.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.repository.WeatherRepository
import com.example.lcb.app.weather.domain.mapper.UnitConverter
import com.example.lcb.app.weather.domain.mapper.WindDirectionFormatter
import com.example.lcb.app.weather.domain.model.CurrentWeather
import com.example.lcb.app.weather.domain.model.DailyForecast
import com.example.lcb.app.weather.domain.model.HourlyForecast
import com.example.lcb.app.weather.domain.model.WeatherIcon
import com.example.lcb.app.weather.domain.model.WeatherReport
import com.example.lcb.app.weather.domain.model.WeatherSettings

@Composable
fun MainWeatherRoute(
    cityId: String?,
    cityStore: CityStore,
    settingsStore: SettingsStore,
    weatherRepository: WeatherRepository,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddCity: () -> Unit
) {
    val viewModel: MainWeatherViewModel = viewModel(
        key = "main-weather-${cityId ?: "selected"}",
        factory = MainWeatherViewModel.Factory(cityStore, settingsStore, weatherRepository)
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(cityId) {
        viewModel.start(cityId)
    }

    MainWeatherScreen(
        state = state,
        onRetry = viewModel::retry,
        onOpenCities = onOpenCities,
        onOpenSettings = onOpenSettings,
        onAddCity = onAddCity
    )
}

@Composable
fun MainWeatherScreen(
    state: MainWeatherUiState,
    onRetry: () -> Unit,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddCity: () -> Unit
) {
    val report = state.report
    val colors = weatherColors(report?.current)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        when {
            state.hasNoCity -> EmptyWeatherState(onAddCity = onAddCity)
            report != null -> WeatherContent(
                state = state,
                report = report,
                onRetry = onRetry,
                onOpenCities = onOpenCities,
                onOpenSettings = onOpenSettings
            )
            else -> LoadingWeatherState(message = state.errorMessage, onRetry = onRetry)
        }
    }
}

@Composable
private fun WeatherContent(
    state: MainWeatherUiState,
    report: WeatherReport,
    onRetry: () -> Unit,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WeatherTopBar(
                cityName = state.city?.name ?: report.city.name,
                onOpenCities = onOpenCities,
                onOpenSettings = onOpenSettings
            )
        }
        item {
            CurrentWeatherHero(
                current = report.current,
                settings = state.settings,
                onRetry = onRetry,
                errorMessage = state.errorMessage,
                isLoading = state.isLoading
            )
        }
        item {
            HourlyForecastSection(
                hourly = report.hourly.take(24),
                settings = state.settings,
                currentTime = report.current.time
            )
        }
        item {
            DailyForecastSection(
                daily = report.daily.take(10),
                settings = state.settings
            )
        }
        item {
            MetricsSection(
                current = report.current,
                settings = state.settings
            )
        }
    }
}

@Composable
private fun WeatherTopBar(
    cityName: String,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF9FCF8)
            )
            Text(
                text = "Open-Meteo 实时天气",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xDDEBF7EF)
            )
        }
        IconButton(onClick = onOpenCities) {
            Icon(Icons.Default.Menu, contentDescription = "城市管理", tint = Color(0xFFF9FCF8))
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color(0xFFF9FCF8))
        }
    }
}

@Composable
private fun CurrentWeatherHero(
    current: CurrentWeather,
    settings: WeatherSettings,
    onRetry: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = UnitConverter.formatTemperature(current.temperature, settings.temperatureUnit),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = Color(0xFFF9FCF8)
        )
        Text(
            text = "${weatherGlyph(current.icon)} ${current.weatherText}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF9FCF8)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "最高 ${UnitConverter.formatTemperature(current.highTemperature, settings.temperatureUnit)} / " +
                "最低 ${UnitConverter.formatTemperature(current.lowTemperature, settings.temperatureUnit)} · " +
                "体感 ${UnitConverter.formatTemperature(current.apparentTemperature, settings.temperatureUnit)}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xEAF9FCF8)
        )
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFF9FCF8)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "正在更新", color = Color(0xEAF9FCF8))
            }
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "更新失败，重试")
            }
        }
    }
}

@Composable
private fun HourlyForecastSection(
    hourly: List<HourlyForecast>,
    settings: WeatherSettings,
    currentTime: String
) {
    WeatherSectionCard(title = "小时天气") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(hourly) { item ->
                val isCurrent = item.time == currentTime
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                        )
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isCurrent) "现在" else formatHour(item.time),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = weatherGlyph(item.icon),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = UnitConverter.formatTemperature(item.temperature, settings.temperatureUnit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.precipitationProbability ?: 0}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastSection(
    daily: List<DailyForecast>,
    settings: WeatherSettings
) {
    WeatherSectionCard(title = "未来 10 天") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            daily.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1.1f),
                        text = formatDay(item.date),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        modifier = Modifier.weight(0.7f),
                        text = weatherGlyph(item.icon),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        modifier = Modifier.weight(1.2f),
                        text = "${item.precipitationProbabilityMax ?: 0}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        modifier = Modifier.weight(1.7f),
                        text = "${UnitConverter.formatTemperature(item.highTemperature, settings.temperatureUnit)} / " +
                            UnitConverter.formatTemperature(item.lowTemperature, settings.temperatureUnit),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(
    current: CurrentWeather,
    settings: WeatherSettings
) {
    val metrics = listOf(
        "湿度" to "${current.relativeHumidity ?: 0}%",
        "风速 / 风向" to "${UnitConverter.formatWindSpeed(current.windSpeed, settings.windSpeedUnit)}\n${WindDirectionFormatter.format(current.windDirectionDegrees)}",
        "气压" to UnitConverter.formatPressure(current.pressureHpa, settings.pressureUnit),
        "能见度" to UnitConverter.formatVisibility(current.visibilityMeters, settings.visibilityUnit),
        "紫外线指数" to (current.uvIndex?.let { "%.1f".format(it) } ?: "--"),
        "降水概率" to "${current.precipitationProbability ?: 0}%",
        "日出 / 日落" to "${formatClock(current.sunrise)}\n${formatClock(current.sunset)}"
    )
    WeatherSectionCard(title = "天气指标") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            metrics.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (label, value) ->
                        MetricTile(
                            modifier = Modifier.weight(1f),
                            label = label,
                            value = value
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WeatherSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LoadingWeatherState(message: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFF9FCF8))
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = message ?: "正在加载天气",
            color = Color(0xFFF9FCF8),
            style = MaterialTheme.typography.bodyLarge
        )
        if (message != null) {
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onRetry
            ) {
                Text(text = "重试")
            }
        }
    }
}

@Composable
private fun EmptyWeatherState(onAddCity: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0x33F9FCF8))
        )
        Text(
            modifier = Modifier.padding(top = 18.dp),
            text = "还没有城市",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF9FCF8)
        )
        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            text = "添加城市后即可查看实时天气、小时预报和未来 10 天天气。",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xEAF9FCF8)
        )
        Button(onClick = onAddCity) {
            Text(text = "搜索城市")
        }
    }
}

private fun weatherColors(current: CurrentWeather?): List<Color> {
    val icon = current?.icon
    val isDay = current?.isDay ?: true
    return when {
        !isDay -> listOf(Color(0xFF172032), Color(0xFF263241), Color(0xFF101513))
        icon == WeatherIcon.Rain || icon == WeatherIcon.Shower -> {
            listOf(Color(0xFF526879), Color(0xFF73828B), Color(0xFFF0F4F1))
        }
        icon == WeatherIcon.Snow -> listOf(Color(0xFF8097A3), Color(0xFFD8E4E7), Color(0xFFF5F7F4))
        icon == WeatherIcon.Thunderstorm -> listOf(Color(0xFF363645), Color(0xFF62606D), Color(0xFFECEAE7))
        icon == WeatherIcon.Cloudy || icon == WeatherIcon.Fog -> {
            listOf(Color(0xFF64737A), Color(0xFF9AA5A6), Color(0xFFF3F5F1))
        }
        else -> listOf(Color(0xFF1D6F8F), Color(0xFF77A8B4), Color(0xFFF4F7F4))
    }
}

private fun weatherGlyph(icon: WeatherIcon): String {
    return when (icon) {
        WeatherIcon.Clear -> "晴"
        WeatherIcon.PartlyCloudy -> "云"
        WeatherIcon.Cloudy -> "阴"
        WeatherIcon.Fog -> "雾"
        WeatherIcon.Drizzle -> "霧雨"
        WeatherIcon.Rain -> "雨"
        WeatherIcon.FreezingRain -> "冻雨"
        WeatherIcon.Snow -> "雪"
        WeatherIcon.Shower -> "阵雨"
        WeatherIcon.Thunderstorm -> "雷"
        WeatherIcon.Unknown -> "--"
    }
}

private fun formatHour(value: String): String {
    return value.substringAfter('T', value).take(5).ifBlank { value }
}

private fun formatClock(value: String?): String {
    return value?.substringAfter('T', value)?.take(5)?.ifBlank { "--" } ?: "--"
}

private fun formatDay(value: String): String {
    return value.substringAfterLast('-').let { day ->
        if (day.length == 2) "${day}日" else value
    }
}
