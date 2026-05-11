package com.example.lcb.app.weather.ui.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.CityStore
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.data.repository.WeatherRepository
import com.example.lcb.app.weather.domain.mapper.UnitConverter
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.ui.main.weatherTextForCode
import com.example.lcb.app.weather.ui.theme.GlassCard
import com.example.lcb.app.weather.ui.theme.GlassIconButton
import com.example.lcb.app.weather.ui.theme.GlassOnSurface
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceFaint
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceMuted
import com.example.lcb.app.weather.ui.theme.NightPalette
import com.example.lcb.app.weather.ui.theme.SkyPalette
import com.example.lcb.app.weather.ui.theme.StaticSkyBackground
import com.example.lcb.app.weather.ui.theme.toVector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val CityManagerSky = SkyPalette(
    gradient = listOf(Color(0xFF13243B), Color(0xFF1F3F60), Color(0xFF305B83)),
    glow = Color(0x44A2C7FF),
    glowX = 240f,
    glowY = 200f,
    glowRadius = 700f
)

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
        onCommitOrder = viewModel::commitOrder,
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
    onCommitOrder: () -> Unit,
    onBack: () -> Unit,
    onAddCity: () -> Unit,
    onOpenCity: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        if (state.query.isBlank()) {
            onMove(from.index - CITY_LIST_HEADER_COUNT, to.index - CITY_LIST_HEADER_COUNT)
        }
    }
    val cards = state.filteredCards

    StaticSkyBackground(palette = CityManagerSky) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 8.dp,
                    bottom = 110.dp
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
                            val dragModifier = if (state.query.isBlank()) {
                                Modifier.longPressDraggableHandle(
                                    onDragStopped = { onCommitOrder() }
                                )
                            } else {
                                Modifier
                            }
                            CityWeatherCard(
                                card = card,
                                temperatureUnit = state.settings.temperatureUnit,
                                canDrag = state.query.isBlank(),
                                onOpen = { onOpenCity(card.city.id) },
                                onDelete = { onDelete(card.city.id) },
                                dragModifier = dragModifier
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(22.dp),
                onClick = onAddCity,
                containerColor = Color.White.copy(alpha = 0.92f),
                contentColor = Color(0xFF14304D)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_city))
            }
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
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = onBack
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.cities),
                style = MaterialTheme.typography.headlineMedium,
                color = GlassOnSurface
            )
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = GlassOnSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        GlassSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.search_added_cities)
        )
    }
}

@Composable
internal fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = GlassOnSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = GlassOnSurfaceFaint,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = GlassOnSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(GlassOnSurface)
                )
            }
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear),
                        tint = GlassOnSurfaceMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CityWeatherCard(
    card: CityCardUiState,
    temperatureUnit: TemperatureUnit,
    canDrag: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    dragModifier: Modifier
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(dragModifier)
            .clickable(onClick = onOpen),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = (card.summary?.icon
                        ?: com.example.lcb.app.weather.domain.model.WeatherIcon.Unknown).toVector(true),
                    contentDescription = null,
                    tint = GlassOnSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.city.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = GlassOnSurface
                )
                if (card.city.subtitle.isNotBlank()) {
                    Text(
                        text = card.city.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = GlassOnSurfaceFaint
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        card.summary != null -> weatherTextForCode(card.summary.weatherCode)
                        card.isLoading -> stringResource(R.string.updating)
                        else -> card.errorMessage
                            ?: card.errorMessageRes?.let { stringResource(it) }
                            ?: stringResource(R.string.no_weather)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassOnSurfaceMuted
                )
                card.summary?.let { summary ->
                    Text(
                        text = stringResource(
                            R.string.high_low_format,
                            UnitConverter.formatTemperature(summary.highTemperature, temperatureUnit),
                            UnitConverter.formatTemperature(summary.lowTemperature, temperatureUnit)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = GlassOnSurfaceMuted
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = card.summary?.let {
                    UnitConverter.formatTemperature(it.temperature, temperatureUnit)
                } ?: "--",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                color = GlassOnSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            CompactGlassButton(
                onClick = onDelete,
                contentDescription = stringResource(R.string.delete)
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    style = MaterialTheme.typography.labelLarge,
                    color = GlassOnSurfaceMuted
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = if (canDrag) {
                        stringResource(R.string.drag_to_reorder)
                    } else {
                        stringResource(R.string.cannot_reorder_while_searching)
                    },
                    tint = if (canDrag) GlassOnSurface else GlassOnSurfaceFaint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactGlassButton(
    onClick: () -> Unit,
    contentDescription: String?,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        content()
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
            text = if (hasQuery) stringResource(R.string.no_matching_city) else stringResource(R.string.no_city),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = GlassOnSurface
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (hasQuery) {
                stringResource(R.string.try_another_keyword)
            } else {
                stringResource(R.string.empty_city_manager_hint)
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = GlassOnSurfaceMuted
        )
        if (!hasQuery) {
            FloatingActionButton(
                modifier = Modifier.padding(top = 18.dp),
                onClick = onAddCity,
                containerColor = Color.White.copy(alpha = 0.92f),
                contentColor = Color(0xFF14304D)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_city))
            }
        }
    }
}

// Suppress unused variable warning - palette only used for parity in case future refactors need it
@Suppress("unused")
private val UnusedNightPaletteRef: SkyPalette = NightPalette

private const val CITY_LIST_HEADER_COUNT = 1
