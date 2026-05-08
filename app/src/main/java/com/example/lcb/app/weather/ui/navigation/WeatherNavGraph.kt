package com.example.lcb.app.weather.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun WeatherNavGraph(
    startCityId: String?,
    openAddCityFirst: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (openAddCityFirst) {
        WeatherRoute.AddCity.route
    } else {
        WeatherRoute.Main.create(startCityId)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = WeatherRoute.Main.route,
            arguments = listOf(
                navArgument("cityId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")
            MainWeatherPlaceholder(
                cityId = cityId,
                onOpenCities = { navController.navigate(WeatherRoute.CityManager.route) },
                onOpenSettings = { navController.navigate(WeatherRoute.Settings.route) }
            )
        }

        composable(WeatherRoute.CityManager.route) {
            CityManagerPlaceholder(
                onBack = navController::popBackStack,
                onAddCity = { navController.navigate(WeatherRoute.AddCity.route) },
                onOpenCity = { cityId ->
                    navController.navigate(WeatherRoute.Main.create(cityId)) {
                        popUpTo(WeatherRoute.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(WeatherRoute.AddCity.route) {
            AddCityPlaceholder(
                onBack = navController::popBackStack,
                onAdded = { cityId ->
                    navController.navigate(WeatherRoute.Main.create(cityId)) {
                        popUpTo(WeatherRoute.AddCity.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(WeatherRoute.Settings.route) {
            SettingsPlaceholder(
                onBack = navController::popBackStack,
                onAbout = { navController.navigate(WeatherRoute.About.route) },
                onPrivacy = { navController.navigate(WeatherRoute.Privacy.route) }
            )
        }

        composable(WeatherRoute.About.route) {
            SimplePlaceholderScreen(
                title = "关于",
                body = "关于页将在后续阶段展示 App 名称、版本号、数据来源和联系方式。",
                primaryAction = "返回",
                onPrimaryAction = navController::popBackStack
            )
        }

        composable(WeatherRoute.Privacy.route) {
            SimplePlaceholderScreen(
                title = "隐私协议",
                body = "隐私协议将在后续阶段说明定位、城市本地保存和 Open-Meteo 数据来源。",
                primaryAction = "返回",
                onPrimaryAction = navController::popBackStack
            )
        }
    }
}

@Composable
private fun MainWeatherPlaceholder(
    cityId: String?,
    onOpenCities: () -> Unit,
    onOpenSettings: () -> Unit
) {
    SimplePlaceholderScreen(
        title = "主天气页",
        body = "当前城市：${cityId ?: "默认城市"}。下一阶段会替换为真实天气总览。",
        primaryAction = "城市管理",
        onPrimaryAction = onOpenCities,
        secondaryAction = "设置",
        onSecondaryAction = onOpenSettings
    )
}

@Composable
private fun CityManagerPlaceholder(
    onBack: () -> Unit,
    onAddCity: () -> Unit,
    onOpenCity: (String) -> Unit
) {
    SimplePlaceholderScreen(
        title = "城市管理",
        body = "这里会展示类似 iOS 天气的城市卡片列表，支持搜索、删除和拖拽排序。",
        primaryAction = "添加城市",
        onPrimaryAction = onAddCity,
        secondaryAction = "进入当前位置",
        onSecondaryAction = { onOpenCity("current_location") },
        tertiaryAction = "返回",
        onTertiaryAction = onBack
    )
}

@Composable
private fun AddCityPlaceholder(
    onBack: () -> Unit,
    onAdded: (String) -> Unit
) {
    SimplePlaceholderScreen(
        title = "添加城市",
        body = "这里会通过 Open-Meteo Geocoding API 搜索城市并保存到本地。",
        primaryAction = "模拟添加",
        onPrimaryAction = { onAdded("manual_city") },
        secondaryAction = "返回",
        onSecondaryAction = onBack
    )
}

@Composable
private fun SettingsPlaceholder(
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit
) {
    SimplePlaceholderScreen(
        title = "设置",
        body = "这里会管理温度、风力、气压、能见度单位和主题。",
        primaryAction = "关于",
        onPrimaryAction = onAbout,
        secondaryAction = "隐私协议",
        onSecondaryAction = onPrivacy,
        tertiaryAction = "返回",
        onTertiaryAction = onBack
    )
}

@Composable
private fun SimplePlaceholderScreen(
    title: String,
    body: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    secondaryAction: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    tertiaryAction: String? = null,
    onTertiaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onPrimaryAction) {
            Text(text = primaryAction)
        }
        if (secondaryAction != null && onSecondaryAction != null) {
            OutlinedButton(
                modifier = Modifier.padding(top = 10.dp),
                onClick = onSecondaryAction
            ) {
                Text(text = secondaryAction)
            }
        }
        if (tertiaryAction != null && onTertiaryAction != null) {
            OutlinedButton(
                modifier = Modifier.padding(top = 10.dp),
                onClick = onTertiaryAction
            ) {
                Text(text = tertiaryAction)
            }
        }
    }
}
