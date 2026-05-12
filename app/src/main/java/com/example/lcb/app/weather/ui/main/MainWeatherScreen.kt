package com.example.lcb.app.weather.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.LcbApp
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.repository.WeatherRepository
import com.example.lcb.app.weather.domain.mapper.UnitConverter
import com.example.lcb.app.weather.domain.model.CurrentWeather
import com.example.lcb.app.weather.domain.model.DailyForecast
import com.example.lcb.app.weather.domain.model.HourlyForecast
import com.example.lcb.app.weather.domain.model.WeatherReport
import com.example.lcb.app.weather.domain.model.WeatherSettings
import com.example.lcb.app.weather.ui.ads.NativeAdSlot
import com.example.lcb.app.weather.ui.ads.rememberNativeAdSlotState
import com.example.lcb.app.weather.ui.theme.GlassCard
import com.example.lcb.app.weather.ui.theme.GlassIconButton
import com.example.lcb.app.weather.ui.theme.GlassOnSurface
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceFaint
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceMuted
import com.example.lcb.app.weather.ui.theme.MetricIcons
import com.example.lcb.app.weather.ui.theme.WeatherSkyBackground
import com.example.lcb.app.weather.ui.theme.toVector

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
        onRefresh = viewModel::refresh,
        onOpenCities = onOpenCities,
        onOpenSettings = onOpenSettings,
        onAddCity = onAddCity
    )
}

@Composable
fun MainWeatherScreen(
    state: MainWeatherUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddCity: () -> Unit
) {
    BackHandler { LcbApp.backLaunchActivity() }
    val report = state.report
    val loadingError = state.errorMessage ?: state.errorMessageRes?.let { stringResource(it) }
    WeatherSkyBackground(current = report?.current) {
        when {
            state.hasNoCity -> EmptyWeatherState(onAddCity = onAddCity)
            report != null -> WeatherContent(
                state = state,
                report = report,
                onRetry = onRetry,
                onRefresh = onRefresh,
                onOpenCities = onOpenCities,
                onOpenSettings = onOpenSettings
            )
            else -> LoadingWeatherState(message = loadingError, onRetry = onRetry)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherContent(
    state: MainWeatherUiState,
    report: WeatherReport,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val pullState = rememberPullToRefreshState()
    val contentError = state.errorMessage ?: state.errorMessageRes?.let { stringResource(it) }
    val nativeAdState = rememberNativeAdSlotState()
    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        state = pullState,
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = state.isRefreshing,
                state = pullState,
                containerColor = Color.White.copy(alpha = 0.20f),
                color = GlassOnSurface
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                bottom = 32.dp
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
                    errorMessage = contentError,
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
                nativeAdState?.let { NativeAdSlot(state = it) }
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
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            icon = Icons.Default.Menu,
            contentDescription = stringResource(R.string.city_manager),
            onClick = onOpenCities
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = GlassOnSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleLarge,
                color = GlassOnSurface
            )
        }
        GlassIconButton(
            icon = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings),
            onClick = onOpenSettings
        )
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
    val weatherText = weatherTextForCode(current.weatherCode)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = current.icon.toVector(current.isDay),
            contentDescription = weatherText,
            tint = GlassOnSurface,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = UnitConverter.formatTemperature(current.temperature, settings.temperatureUnit),
            style = MaterialTheme.typography.displayLarge,
            color = GlassOnSurface
        )
        Text(
            text = weatherText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = GlassOnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.high_low_format,
                UnitConverter.formatTemperature(current.highTemperature, settings.temperatureUnit),
                UnitConverter.formatTemperature(current.lowTemperature, settings.temperatureUnit)
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = GlassOnSurfaceMuted
        )
        Text(
            text = stringResource(
                R.string.apparent_temperature_format,
                UnitConverter.formatTemperature(current.apparentTemperature, settings.temperatureUnit)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = GlassOnSurfaceMuted
        )
        if (isLoading) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = GlassOnSurface
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.updating),
                    color = GlassOnSurfaceMuted,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.18f),
                    contentColor = GlassOnSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.update_failed_retry))
            }
        }
    }
}

@Composable
private fun GlassSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = GlassOnSurfaceMuted,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun HourlyForecastSection(
    hourly: List<HourlyForecast>,
    settings: WeatherSettings,
    currentTime: String
) {
    Column {
        GlassSectionHeader(title = stringResource(R.string.next_24_hours))
        GlassCard(contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                items(hourly) { item ->
                    HourCell(
                        item = item,
                        settings = settings,
                        isCurrent = item.time == currentTime
                    )
                }
            }
        }
    }
}

@Composable
private fun HourCell(
    item: HourlyForecast,
    settings: WeatherSettings,
    isCurrent: Boolean
) {
    val highlight = isCurrent
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (highlight) Color.White.copy(alpha = 0.22f) else Color.Transparent
            )
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (highlight) stringResource(R.string.now) else formatHour(item.time),
            style = MaterialTheme.typography.labelMedium,
            color = if (highlight) GlassOnSurface else GlassOnSurfaceMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = item.icon.toVector(item.isDay),
            contentDescription = null,
            tint = GlassOnSurface,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        val precip = item.precipitationProbability ?: 0
        Text(
            text = if (precip > 0) "$precip%" else " ",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFA5D8FF)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = UnitConverter.formatTemperature(item.temperature, settings.temperatureUnit),
            style = MaterialTheme.typography.titleMedium,
            color = GlassOnSurface
        )
    }
}

@Composable
private fun DailyForecastSection(
    daily: List<DailyForecast>,
    settings: WeatherSettings
) {
    val highs = daily.mapNotNull { it.highTemperature }
    val lows = daily.mapNotNull { it.lowTemperature }
    val rangeMax = highs.maxOrNull()
    val rangeMin = lows.minOrNull()

    Column {
        GlassSectionHeader(title = stringResource(R.string.future_days_format, daily.size))
        GlassCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
            daily.forEachIndexed { index, item ->
                DailyForecastRow(
                    item = item,
                    settings = settings,
                    rangeMin = rangeMin,
                    rangeMax = rangeMax
                )
                if (index != daily.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecast,
    settings: WeatherSettings,
    rangeMin: Double?,
    rangeMax: Double?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1.1f),
            text = formatDayText(item.date),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = GlassOnSurface
        )
        Icon(
            imageVector = item.icon.toVector(true),
            contentDescription = null,
            tint = GlassOnSurface,
            modifier = Modifier
                .weight(0.7f)
                .size(22.dp)
        )
        val precip = item.precipitationProbabilityMax ?: 0
        Text(
            modifier = Modifier.weight(0.9f),
            text = if (precip > 0) "$precip%" else "—",
            style = MaterialTheme.typography.bodyMedium,
            color = if (precip > 0) Color(0xFFA5D8FF) else GlassOnSurfaceFaint,
            textAlign = TextAlign.Center
        )
        Text(
            text = UnitConverter.formatTemperature(item.lowTemperature, settings.temperatureUnit),
            style = MaterialTheme.typography.bodyMedium,
            color = GlassOnSurfaceMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.width(46.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TemperatureRangeBar(
            modifier = Modifier
                .weight(1.4f)
                .height(6.dp),
            globalMin = rangeMin,
            globalMax = rangeMax,
            dayLow = item.lowTemperature,
            dayHigh = item.highTemperature
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = UnitConverter.formatTemperature(item.highTemperature, settings.temperatureUnit),
            style = MaterialTheme.typography.bodyMedium,
            color = GlassOnSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(46.dp)
        )
    }
}

@Composable
private fun TemperatureRangeBar(
    modifier: Modifier,
    globalMin: Double?,
    globalMax: Double?,
    dayLow: Double?,
    dayHigh: Double?
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        if (globalMin != null && globalMax != null && dayLow != null && dayHigh != null &&
            globalMax > globalMin
        ) {
            val span = (globalMax - globalMin).coerceAtLeast(0.001)
            val startFraction = ((dayLow - globalMin) / span).toFloat().coerceIn(0f, 1f)
            val endFraction = ((dayHigh - globalMin) / span).toFloat().coerceIn(0f, 1f)
            val widthFraction = (endFraction - startFraction).coerceAtLeast(0.04f)
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(startFraction.coerceAtLeast(0.0001f)))
                Box(
                    modifier = Modifier
                        .weight(widthFraction)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8FC8E8), Color(0xFFFFD479), Color(0xFFFF9D7C))
                            )
                        )
                )
                val trailing = (1f - endFraction).coerceAtLeast(0.0001f)
                Spacer(modifier = Modifier.weight(trailing))
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
        MetricItem(
            icon = MetricIcons.Humidity,
            label = stringResource(R.string.humidity),
            value = "${current.relativeHumidity ?: 0}%"
        ),
        MetricItem(
            icon = MetricIcons.Wind,
            label = stringResource(R.string.wind_speed_direction),
            value = UnitConverter.formatWindSpeed(current.windSpeed, settings.windSpeedUnit),
            secondary = windDirectionText(current.windDirectionDegrees)
        ),
        MetricItem(
            icon = MetricIcons.Pressure,
            label = stringResource(R.string.pressure),
            value = UnitConverter.formatPressure(current.pressureHpa, settings.pressureUnit)
        ),
        MetricItem(
            icon = MetricIcons.Visibility,
            label = stringResource(R.string.visibility),
            value = UnitConverter.formatVisibility(current.visibilityMeters, settings.visibilityUnit)
        ),
        MetricItem(
            icon = MetricIcons.Uv,
            label = stringResource(R.string.uv_index),
            value = current.uvIndex?.let { "%.1f".format(it) } ?: "--"
        ),
        MetricItem(
            icon = MetricIcons.Precipitation,
            label = stringResource(R.string.precipitation_probability),
            value = "${current.precipitationProbability ?: 0}%"
        ),
        MetricItem(
            icon = MetricIcons.SunCycle,
            label = stringResource(R.string.sunrise_sunset),
            value = formatClock(current.sunrise),
            secondary = formatClock(current.sunset)
        )
    )

    Column {
        GlassSectionHeader(title = stringResource(R.string.weather_metrics))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { metric ->
                        MetricTile(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            metric = metric
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

private data class MetricItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val secondary: String? = null
)

@Composable
private fun MetricTile(
    modifier: Modifier,
    metric: MetricItem
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = metric.icon,
                contentDescription = null,
                tint = GlassOnSurfaceMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelMedium,
                color = GlassOnSurfaceMuted
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = metric.value,
            style = MaterialTheme.typography.headlineSmall,
            color = GlassOnSurface
        )
        if (metric.secondary != null) {
            Text(
                text = metric.secondary,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassOnSurfaceMuted
            )
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
        if (message == null) {
            CircularProgressIndicator(color = GlassOnSurface)
        }
        Text(
            modifier = Modifier.padding(top = if (message == null) 16.dp else 0.dp),
            text = message ?: stringResource(R.string.loading_weather),
            color = GlassOnSurface,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        if (message != null) {
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.18f),
                    contentColor = GlassOnSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.retry))
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
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = GlassOnSurface,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.no_city),
            style = MaterialTheme.typography.headlineMedium,
            color = GlassOnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_weather_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = GlassOnSurfaceMuted
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddCity,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.20f),
                contentColor = GlassOnSurface
            )
        ) {
            Text(text = stringResource(R.string.search_city))
        }
    }
}

private fun formatHour(value: String): String {
    return value.substringAfter('T', value).take(5).ifBlank { value }
}

private fun formatClock(value: String?): String {
    return value?.substringAfter('T', value)?.take(5)?.ifBlank { "--" } ?: "--"
}

@Composable
private fun formatDayText(value: String): String {
    return value.substringAfterLast('-').let { day ->
        if (day.length == 2) stringResource(R.string.day_of_month_format, day) else value
    }
}

@Composable
private fun windDirectionText(degrees: Int?): String {
    if (degrees == null) return "--"
    val normalized = ((degrees % 360) + 360) % 360
    return when (((normalized + 22.5) / 45.0).toInt() % 8) {
        0 -> stringResource(R.string.wind_direction_n)
        1 -> stringResource(R.string.wind_direction_ne)
        2 -> stringResource(R.string.wind_direction_e)
        3 -> stringResource(R.string.wind_direction_se)
        4 -> stringResource(R.string.wind_direction_s)
        5 -> stringResource(R.string.wind_direction_sw)
        6 -> stringResource(R.string.wind_direction_w)
        else -> stringResource(R.string.wind_direction_nw)
    }
}

@Composable
internal fun weatherTextForCode(code: Int): String = when (code) {
    0 -> stringResource(R.string.weather_clear)
    1 -> stringResource(R.string.weather_mainly_clear)
    2 -> stringResource(R.string.weather_partly_cloudy)
    3 -> stringResource(R.string.weather_overcast)
    45, 48 -> stringResource(R.string.weather_fog)
    51 -> stringResource(R.string.weather_light_drizzle)
    53 -> stringResource(R.string.weather_drizzle)
    55 -> stringResource(R.string.weather_heavy_drizzle)
    56, 57 -> stringResource(R.string.weather_freezing_drizzle)
    61 -> stringResource(R.string.weather_light_rain)
    63 -> stringResource(R.string.weather_moderate_rain)
    65 -> stringResource(R.string.weather_heavy_rain)
    66, 67 -> stringResource(R.string.weather_freezing_rain)
    71 -> stringResource(R.string.weather_light_snow)
    73 -> stringResource(R.string.weather_moderate_snow)
    75 -> stringResource(R.string.weather_heavy_snow)
    77 -> stringResource(R.string.weather_snow_grains)
    80 -> stringResource(R.string.weather_light_showers)
    81 -> stringResource(R.string.weather_showers)
    82 -> stringResource(R.string.weather_heavy_showers)
    85 -> stringResource(R.string.weather_light_snow_showers)
    86 -> stringResource(R.string.weather_heavy_snow_showers)
    95 -> stringResource(R.string.weather_thunderstorm)
    96, 99 -> stringResource(R.string.weather_thunderstorm_hail)
    else -> stringResource(R.string.weather_unknown)
}
