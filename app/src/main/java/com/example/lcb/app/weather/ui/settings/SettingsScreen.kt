package com.example.lcb.app.weather.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.domain.model.PressureUnit
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.ThemeMode
import com.example.lcb.app.weather.domain.model.VisibilityUnit
import com.example.lcb.app.weather.domain.model.WeatherSettings
import com.example.lcb.app.weather.domain.model.WindSpeedUnit

@Composable
fun SettingsRoute(
    settingsStore: SettingsStore,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(settingsStore)
    )
    val settings by viewModel.settings.collectAsState()

    SettingsScreen(
        settings = settings,
        onBack = onBack,
        onAbout = onAbout,
        onPrivacy = onPrivacy,
        onTemperatureUnit = viewModel::setTemperatureUnit,
        onWindSpeedUnit = viewModel::setWindSpeedUnit,
        onPressureUnit = viewModel::setPressureUnit,
        onVisibilityUnit = viewModel::setVisibilityUnit,
        onThemeMode = viewModel::setThemeMode
    )
}

@Composable
fun SettingsScreen(
    settings: WeatherSettings,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    onTemperatureUnit: (TemperatureUnit) -> Unit,
    onWindSpeedUnit: (WindSpeedUnit) -> Unit,
    onPressureUnit: (PressureUnit) -> Unit,
    onVisibilityUnit: (VisibilityUnit) -> Unit,
    onThemeMode: (ThemeMode) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = "设置",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            SettingsGroup(title = "单位") {
                ChoiceRow(
                    title = "温度",
                    options = TemperatureUnit.entries,
                    selected = settings.temperatureUnit,
                    label = { it.label },
                    onSelected = onTemperatureUnit
                )
                ChoiceRow(
                    title = "风力",
                    options = WindSpeedUnit.entries,
                    selected = settings.windSpeedUnit,
                    label = { it.label },
                    onSelected = onWindSpeedUnit
                )
                ChoiceRow(
                    title = "气压",
                    options = PressureUnit.entries,
                    selected = settings.pressureUnit,
                    label = { it.label },
                    onSelected = onPressureUnit
                )
                ChoiceRow(
                    title = "能见度",
                    options = VisibilityUnit.entries,
                    selected = settings.visibilityUnit,
                    label = { it.label },
                    onSelected = onVisibilityUnit
                )
            }

            SettingsGroup(title = "外观") {
                ChoiceRow(
                    title = "主题",
                    options = ThemeMode.entries,
                    selected = settings.themeMode,
                    label = { it.label },
                    onSelected = onThemeMode
                )
            }

            SettingsGroup(title = "其他") {
                NavigationRow(title = "关于", onClick = onAbout)
                NavigationRow(title = "隐私协议", onClick = onPrivacy)
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content
        )
    }
}

@Composable
private fun <T> ChoiceRow(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                AssistChip(
                    onClick = { onSelected(option) },
                    label = { Text(text = label(option)) },
                    leadingIcon = if (option == selected) {
                        { Text(text = "✓", color = MaterialTheme.colorScheme.primary) }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@Composable
private fun NavigationRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = onClick) {
            Icon(Icons.Default.ChevronRight, contentDescription = title)
        }
    }
}
