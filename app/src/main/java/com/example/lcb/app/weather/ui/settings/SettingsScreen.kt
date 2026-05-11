package com.example.lcb.app.weather.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lcb.app.R
import com.example.lcb.app.weather.data.local.SettingsStore
import com.example.lcb.app.weather.domain.model.LanguageOption
import com.example.lcb.app.weather.domain.model.PressureUnit
import com.example.lcb.app.weather.domain.model.TemperatureUnit
import com.example.lcb.app.weather.domain.model.VisibilityUnit
import com.example.lcb.app.weather.domain.model.WeatherSettings
import com.example.lcb.app.weather.domain.model.WindSpeedUnit
import com.example.lcb.app.weather.ui.theme.GlassIconButton
import com.example.lcb.app.weather.ui.theme.GlassOnSurface
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceFaint
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceMuted
import com.example.lcb.app.weather.ui.theme.SkyPalette
import com.example.lcb.app.weather.ui.theme.StaticSkyBackground
import kotlinx.coroutines.launch

internal val SettingsSky = SkyPalette(
    gradient = listOf(Color(0xFF0F1A2C), Color(0xFF1A2D49), Color(0xFF274B73)),
    glow = Color(0x33A2C7FF),
    glowX = 220f,
    glowY = 180f,
    glowRadius = 700f
)

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
        onLanguageOption = viewModel::setLanguageOption
    )
}

private sealed interface ActiveSheet {
    data object Temperature : ActiveSheet
    data object Wind : ActiveSheet
    data object Pressure : ActiveSheet
    data object Visibility : ActiveSheet
    data object Language : ActiveSheet
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
    onLanguageOption: (LanguageOption) -> Unit
) {
    var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }

    StaticSkyBackground(palette = SettingsSky) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 18.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GlassOnSurface
                )
            }

            SectionHeader(title = stringResource(R.string.units))
            SettingRow(
                icon = Icons.Outlined.Thermostat,
                title = stringResource(R.string.temperature),
                value = settings.temperatureUnit.labelText(),
                onClick = { activeSheet = ActiveSheet.Temperature }
            )
            SettingRow(
                icon = Icons.Outlined.Air,
                title = stringResource(R.string.wind),
                value = settings.windSpeedUnit.labelText(),
                onClick = { activeSheet = ActiveSheet.Wind }
            )
            SettingRow(
                icon = Icons.Outlined.Speed,
                title = stringResource(R.string.pressure),
                value = settings.pressureUnit.labelText(),
                onClick = { activeSheet = ActiveSheet.Pressure }
            )
            SettingRow(
                icon = Icons.Outlined.Visibility,
                title = stringResource(R.string.visibility),
                value = settings.visibilityUnit.labelText(),
                onClick = { activeSheet = ActiveSheet.Visibility }
            )
            SettingRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.language),
                value = settings.languageOption.labelText(),
                onClick = { activeSheet = ActiveSheet.Language }
            )

            SectionHeader(title = stringResource(R.string.other))
            SettingRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about),
                value = null,
                onClick = onAbout
            )
            SettingRow(
                icon = Icons.Outlined.PrivacyTip,
                title = stringResource(R.string.privacy_policy),
                value = null,
                onClick = onPrivacy
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    when (activeSheet) {
        ActiveSheet.Temperature -> ChoiceBottomSheet(
            title = stringResource(R.string.temperature),
            options = TemperatureUnit.entries,
            selected = settings.temperatureUnit,
            label = { it.labelText() },
            onSelected = onTemperatureUnit,
            onDismiss = { activeSheet = null }
        )
        ActiveSheet.Wind -> ChoiceBottomSheet(
            title = stringResource(R.string.wind),
            options = WindSpeedUnit.entries,
            selected = settings.windSpeedUnit,
            label = { it.labelText() },
            onSelected = onWindSpeedUnit,
            onDismiss = { activeSheet = null }
        )
        ActiveSheet.Pressure -> ChoiceBottomSheet(
            title = stringResource(R.string.pressure),
            options = PressureUnit.entries,
            selected = settings.pressureUnit,
            label = { it.labelText() },
            onSelected = onPressureUnit,
            onDismiss = { activeSheet = null }
        )
        ActiveSheet.Visibility -> ChoiceBottomSheet(
            title = stringResource(R.string.visibility),
            options = VisibilityUnit.entries,
            selected = settings.visibilityUnit,
            label = { it.labelText() },
            onSelected = onVisibilityUnit,
            onDismiss = { activeSheet = null }
        )
        ActiveSheet.Language -> ChoiceBottomSheet(
            title = stringResource(R.string.language),
            options = LanguageOption.entries,
            selected = settings.languageOption,
            label = { it.labelText() },
            onSelected = onLanguageOption,
            onDismiss = { activeSheet = null }
        )
        null -> Unit
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 8.dp),
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = GlassOnSurfaceMuted,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.10f))
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlassOnSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GlassOnSurface
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassOnSurfaceMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = GlassOnSurfaceFaint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceBottomSheet(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1B2E48),
        contentColor = GlassOnSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.30f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = GlassOnSurface,
                fontWeight = FontWeight.SemiBold
            )
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                onSelected(option)
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) onDismiss()
                                    }
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = GlassOnSurface,
                            unselectedColor = GlassOnSurfaceFaint
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.bodyLarge,
                        color = GlassOnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TemperatureUnit.labelText(): String = when (this) {
    TemperatureUnit.Celsius -> stringResource(R.string.unit_celsius)
    TemperatureUnit.Fahrenheit -> stringResource(R.string.unit_fahrenheit)
}

@Composable
private fun WindSpeedUnit.labelText(): String = when (this) {
    WindSpeedUnit.KilometersPerHour -> stringResource(R.string.unit_kmh)
    WindSpeedUnit.MetersPerSecond -> stringResource(R.string.unit_ms)
    WindSpeedUnit.MilesPerHour -> stringResource(R.string.unit_mph)
    WindSpeedUnit.Knots -> stringResource(R.string.unit_knots)
}

@Composable
private fun PressureUnit.labelText(): String = when (this) {
    PressureUnit.Hectopascal -> stringResource(R.string.unit_hpa)
    PressureUnit.MillimeterMercury -> stringResource(R.string.unit_mmhg)
    PressureUnit.InchMercury -> stringResource(R.string.unit_inhg)
}

@Composable
private fun VisibilityUnit.labelText(): String = when (this) {
    VisibilityUnit.Kilometer -> stringResource(R.string.unit_km)
    VisibilityUnit.Mile -> stringResource(R.string.unit_mile)
}

@Composable
private fun LanguageOption.labelText(): String = when (this) {
    LanguageOption.System -> stringResource(R.string.language_system)
    LanguageOption.ChineseSimplified -> stringResource(R.string.language_zh_cn)
    LanguageOption.ChineseTraditional -> stringResource(R.string.language_zh_tw)
    LanguageOption.English -> stringResource(R.string.language_en)
    LanguageOption.Japanese -> stringResource(R.string.language_ja)
    LanguageOption.Korean -> stringResource(R.string.language_ko)
    LanguageOption.French -> stringResource(R.string.language_fr)
    LanguageOption.German -> stringResource(R.string.language_de)
    LanguageOption.Spanish -> stringResource(R.string.language_es)
    LanguageOption.Portuguese -> stringResource(R.string.language_pt)
    LanguageOption.Italian -> stringResource(R.string.language_it)
    LanguageOption.Russian -> stringResource(R.string.language_ru)
}
