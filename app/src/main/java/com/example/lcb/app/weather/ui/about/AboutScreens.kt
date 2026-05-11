package com.example.lcb.app.weather.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.lcb.app.BuildConfig
import com.example.lcb.app.R
import com.example.lcb.app.weather.ui.theme.GlassCard
import com.example.lcb.app.weather.ui.theme.GlassIconButton
import com.example.lcb.app.weather.ui.theme.GlassOnSurface
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceFaint
import com.example.lcb.app.weather.ui.theme.GlassOnSurfaceMuted
import com.example.lcb.app.weather.ui.theme.StaticSkyBackground
import com.example.lcb.app.weather.ui.settings.SettingsSky

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val iconBitmap = remember {
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            ?.toBitmap(width = 216, height = 216)
            ?.asImageBitmap()
    }

    StaticSkyBackground(palette = SettingsSky) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
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
                    text = stringResource(R.string.about),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GlassOnSurface
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = null,
                            modifier = Modifier.size(108.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = GlassOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.about_version_format, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassOnSurfaceMuted
                )
            }

            Spacer(modifier = Modifier.weight(1.4f))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                text = stringResource(R.string.about_data_source),
                style = MaterialTheme.typography.bodySmall,
                color = GlassOnSurfaceFaint,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    InfoPage(title = stringResource(R.string.privacy_policy), onBack = onBack) {
        InfoBlock(
            title = stringResource(R.string.privacy_location),
            lines = listOf(
                stringResource(R.string.privacy_location_line_1),
                stringResource(R.string.privacy_location_line_2)
            )
        )
        InfoBlock(
            title = stringResource(R.string.privacy_local_data),
            lines = listOf(
                stringResource(R.string.privacy_local_data_line_1),
                stringResource(R.string.privacy_local_data_line_2)
            )
        )
        InfoBlock(
            title = stringResource(R.string.privacy_weather_data),
            lines = listOf(
                stringResource(R.string.privacy_weather_data_line_1),
                stringResource(R.string.privacy_weather_data_line_2)
            )
        )
    }
}

@Composable
private fun InfoPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    StaticSkyBackground(palette = SettingsSky) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = GlassOnSurface
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun InfoBlock(
    title: String,
    lines: List<String>
) {
    GlassCard(contentPadding = PaddingValues(18.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = GlassOnSurface
        )
        lines.forEach { line ->
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassOnSurfaceMuted
            )
        }
    }
}
