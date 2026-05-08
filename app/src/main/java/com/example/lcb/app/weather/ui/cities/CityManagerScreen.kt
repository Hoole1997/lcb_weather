package com.example.lcb.app.weather.ui.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.WeatherIcon
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun CityManagerRoute(
    cityStore: CityStore,
    settingsStore: SettingsStore,
    weatherRepository: WeatherRepository,
    onBack: () -> Unit,
    onAddCity: () -> Unit,
    onOpenCity: (String) -> Unit
) {
    val viewModel: CityManagerViewModel = viewModel(
        factory = CityManagerViewModel.Factory(cityStore, settingsStore, weatherRepository)
    )
    val state by viewModel.uiState.collectAsState()

    CityManagerScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onDelete = viewModel::deleteCity,
        onMove = viewModel::moveCity,
        onBack = onBack,
        onAddCity = onAddCity,
        onOpenCity = onOpenCity
    )
}

@Composable
fun CityManagerScreen(
    state: CityManagerUiState,
    onQueryChange: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onBack: () -> Unit,
    onAddCity: () -> Unit,
    onOpenCity: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        if (state.query.isBlank()) {
            onMove(from.index, to.index)
        }
    }
    val cards = state.filteredCards

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1D5F7A), MaterialTheme.colorScheme.background)
                )
            )
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 10.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CityManagerHeader(
                    query = state.query,
                    isRefreshing = state.isRefreshing,
                    onQueryChange = onQueryChange,
                    onBack = onBack
                )
            }

            if (cards.isEmpty()) {
                item {
                    EmptyCityList(
                        hasQuery = state.query.isNotBlank(),
                        onAddCity = onAddCity
                    )
                }
            } else {
                items(cards, key = { it.city.id }) { card ->
                    ReorderableItem(reorderableState, key = card.city.id) {
                        CityWeatherCard(
                            card = card,
                            temperatureUnit = state.settings.temperatureUnit,
                            canDrag = state.query.isBlank(),
                            onOpen = { onOpenCity(card.city.id) },
                            onDelete = { onDelete(card.city.id) },
                            modifier = if (state.query.isBlank()) Modifier.draggableHandle() else Modifier
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(22.dp),
            onClick = onAddCity
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加城市")
        }
    }
}

@Composable
private fun CityManagerHeader(
    query: String,
    isRefreshing: Boolean,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFFF9FCF8))
            }
            Text(
                modifier = Modifier.weight(1f),
                text = "城市",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF9FCF8)
            )
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFF9FCF8)
                )
            }
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            value = query,
            onValueChange = onQueryChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            placeholder = { Text(text = "搜索已添加城市") },
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun CityWeatherCard(
    card: CityCardUiState,
    temperatureUnit: TemperatureUnit,
    canDrag: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.city.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (card.city.subtitle.isNotBlank()) {
                    Text(
                        text = card.city.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when {
                        card.summary != null -> "${weatherGlyph(card.summary.icon)} ${card.summary.weatherText}"
                        card.isLoading -> "正在更新"
                        else -> card.errorMessage ?: "暂无天气"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                card.summary?.let { summary ->
                    Text(
                        text = "最高 ${UnitConverter.formatTemperature(summary.highTemperature, temperatureUnit)} / " +
                            "最低 ${UnitConverter.formatTemperature(summary.lowTemperature, temperatureUnit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = card.summary?.let {
                    UnitConverter.formatTemperature(it.temperature, temperatureUnit)
                } ?: "--",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除城市")
            }
            IconButton(
                modifier = modifier,
                onClick = {}
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = if (canDrag) "拖拽排序" else "搜索时不可排序",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyCityList(
    hasQuery: Boolean,
    onAddCity: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (hasQuery) "没有匹配的城市" else "还没有城市",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color(0xFFF9FCF8)
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (hasQuery) "换个关键词试试" else "添加城市后会在这里管理顺序和删除",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xDDF9FCF8)
        )
        if (!hasQuery) {
            FloatingActionButton(
                modifier = Modifier.padding(top = 18.dp),
                onClick = onAddCity
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加城市")
            }
        }
    }
}

private fun weatherGlyph(icon: WeatherIcon): String {
    return when (icon) {
        WeatherIcon.Clear -> "晴"
        WeatherIcon.PartlyCloudy -> "云"
        WeatherIcon.Cloudy -> "阴"
        WeatherIcon.Fog -> "雾"
        WeatherIcon.Drizzle -> "毛雨"
        WeatherIcon.Rain -> "雨"
        WeatherIcon.FreezingRain -> "冻雨"
        WeatherIcon.Snow -> "雪"
        WeatherIcon.Shower -> "阵雨"
        WeatherIcon.Thunderstorm -> "雷"
        WeatherIcon.Unknown -> "--"
    }
}
