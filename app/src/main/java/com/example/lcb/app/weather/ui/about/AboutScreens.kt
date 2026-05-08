package com.example.lcb.app.weather.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lcb.app.BuildConfig

@Composable
fun AboutScreen(onBack: () -> Unit) {
    InfoPage(title = "关于", onBack = onBack) {
        InfoBlock(
            title = "天气",
            lines = listOf(
                "版本号：${BuildConfig.VERSION_NAME}",
                "数据来源：Open-Meteo 提供天气预报和地理编码数据。",
                "API 来源：Weather Forecast API、Geocoding API。",
                "联系方式：contact@example.com"
            )
        )
        InfoBlock(
            title = "数据说明",
            lines = listOf(
                "天气数据会随 Open-Meteo 的可用字段和更新频率变化。",
                "应用会根据你选择的城市经纬度请求当前天气、小时天气和未来 10 天天气。"
            )
        )
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    InfoPage(title = "隐私协议", onBack = onBack) {
        InfoBlock(
            title = "定位",
            lines = listOf(
                "定位仅用于首次进入时获取当前位置天气。",
                "拒绝定位后仍可通过搜索城市继续使用应用。"
            )
        )
        InfoBlock(
            title = "本地数据",
            lines = listOf(
                "你添加的城市列表、排序和单位设置保存在本地设备。",
                "应用不会主动将本地保存的城市列表上传到自有服务器。"
            )
        )
        InfoBlock(
            title = "天气数据",
            lines = listOf(
                "天气和城市搜索数据来自 Open-Meteo。",
                "请求天气时会向 Open-Meteo 发送城市经纬度和单位参数。"
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
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(
                modifier = Modifier.padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

@Composable
private fun InfoBlock(
    title: String,
    lines: List<String>
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            lines.forEach { line ->
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
