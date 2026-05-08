# 天气 App 需求与实施进度

## 使用规则

- 每次开始一个新的实现步骤前，必须先阅读本文档，确认整体流程、当前阶段和未完成项。
- 每完成一个需求项，将对应条目从 `- [ ]` 改为 `- [x] ✅`。
- 每完成一个大阶段，必须在该阶段的“阶段备注”中补充上下文，至少包含：完成了什么、关键文件、关键决策、遗留风险或下一步注意事项。
- 不删除未完成项；如果需求变更，新增“变更说明”并保留历史上下文。
- 本文档用于防止上下文窗口压缩后丢失项目状态，后续实现必须以本文档为进度源。

## 产品目标

实现一个现代、轻量、好维护的 Android 天气 App。UI 使用 Jetpack Compose 直接在 `app` 模块内实现业务代码。天气数据使用 Open-Meteo Forecast API，城市搜索使用 Open-Meteo Geocoding API。首次进入 App 时默认通过系统定位 API 获取经纬度，请求当前位置天气；定位失败或用户拒绝后，引导用户搜索城市。

## 技术选型

- UI：Jetpack Compose + Material3
- 导航：Navigation Compose
- 状态管理：ViewModel + StateFlow
- 网络：Retrofit + kotlinx.serialization
- 本地存储：DataStore Preferences，城市列表以 JSON 字符串保存
- 定位：Android 系统定位 API，优先粗略定位
- 架构：单 Activity + Repository + 手动 AppContainer，暂不引入 Hilt
- 数据库：第一版不使用 Room，后续需要离线缓存或历史数据时再引入

## API 规划

### 天气接口

接口来源：https://open-meteo.com/en/docs

基础地址：

```text
https://api.open-meteo.com/v1/forecast
```

首版请求字段：

```text
latitude
longitude
timezone=auto 或城市保存的 timezone
forecast_days=10
current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m
hourly=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m,wind_direction_10m,pressure_msl,visibility,uv_index,is_day
daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_probability_max,wind_speed_10m_max,wind_direction_10m_dominant
temperature_unit=celsius 或 fahrenheit
wind_speed_unit=kmh/ms/mph/kn
```

### 城市搜索接口

接口来源：https://open-meteo.com/en/docs/geocoding-api

基础地址：

```text
https://geocoding-api.open-meteo.com/v1/search
```

请求参数：

```text
name={keyword}
count=10
language=zh
format=json
```

保存字段：

```kotlin
cityName
country
admin1
latitude
longitude
timezone
sortIndex
```

## 阶段 0：项目基线与实施文档

- [x] ✅ 创建本文档，明确需求拆分、完成标记规则和上下文备注规则。
- [x] ✅ 检查当前工程结构、Gradle 插件、Compose 依赖版本和入口 Activity。
- [x] ✅ 记录当前项目基线，包括 package、flavor、minSdk、targetSdk、现有广告和 metrics 代码边界。
- [x] ✅ 确认第一版所有业务代码直接放在 `app` 模块内，不拆新模块。

阶段备注：

```text
已创建根目录 WEATHER_APP_REQUIREMENTS.md，作为后续实现的进度源。当前项目位于 /Users/apple/StudioProjects/lcb_weather，已有 app 与 metrics 模块，package 为 com.example.lcb.app，compileSdk 36，targetSdk 35，minSdk 默认 26。MainActivity 当前仍是 AppCompatActivity + XML；主题资源已经使用 Material3 DayNight NoActionBar。LcbApp 中存在广告初始化 LcbAdInitializer 和 metrics/AdjustTracker 初始化，后续切 Compose 入口时必须保留 Application 初始化边界。第一版天气业务直接放在 app 模块，不拆新模块。已补充 .impeccable.md，设计方向为清晰、冷静、实用的现代天气仪表盘。
```

## 阶段 1：Compose 基础设施

- [x] ✅ 在 `app/build.gradle.kts` 中启用 Compose build feature。
- [x] ✅ 添加 Kotlin Compose 插件。
- [x] ✅ 添加 Compose、Material3、Navigation Compose、Lifecycle ViewModel Compose 依赖。
- [x] ✅ 将 `MainActivity` 从 XML `setContentView` 改为 Compose `setContent`。
- [x] ✅ 保留 `enableEdgeToEdge()`，并用 Compose 处理系统栏间距。
- [x] ✅ 创建 `WeatherApp` 作为 Compose 根入口。
- [x] ✅ 创建 Material3 主题，支持系统深浅色。
- [x] ✅ 确认原有广告、metrics 初始化不被破坏。
- [x] ✅ 编译通过。

阶段备注：

```text
已完成 Compose 基础设施。根 Gradle 增加 kotlin-compose 插件，app 模块启用 compose build feature 并接入 activity-compose、compose foundation/material3/icons/ui/tooling、navigation-compose、lifecycle-viewmodel-compose。MainActivity 已改为 ComponentActivity + setContent，保留 enableEdgeToEdge；Application 层 LcbApp 未改动，广告和 metrics 初始化仍在原边界内。新增 WeatherApp、WeatherTheme、Type，当前仅显示 Compose 占位 UI，后续阶段会替换为真实导航和天气页面。验证命令：./gradlew :app:compileLocalDebugKotlin，结果 BUILD SUCCESSFUL；仅有既有第三方 SDK 资源/Manifest 警告和 LcbApp unchecked cast 警告。
```

## 阶段 2：领域模型与单位换算

- [x] ✅ 创建城市模型 `SavedCity`。
- [x] ✅ 创建天气聚合模型 `WeatherReport`。
- [x] ✅ 创建当前天气模型 `CurrentWeather`。
- [x] ✅ 创建小时天气模型 `HourlyForecast`。
- [x] ✅ 创建每日天气模型 `DailyForecast`。
- [x] ✅ 创建设置模型 `WeatherSettings`。
- [x] ✅ 创建温度单位枚举：摄氏度、华氏度。
- [x] ✅ 创建风速单位枚举：km/h、m/s、mph、kn。
- [x] ✅ 创建气压单位枚举：hPa、mmHg、inHg。
- [x] ✅ 创建能见度单位枚举：km、mile。
- [x] ✅ 创建主题枚举：跟随系统、浅色、深色。
- [x] ✅ 创建天气代码映射器，将 Open-Meteo `weather_code` 转成中文天气描述和图标语义。
- [x] ✅ 创建风向转换器，将角度转为中文方向，例如北风、东北风。
- [x] ✅ 创建单位换算工具，覆盖气压、能见度、温度展示和风速展示。
- [x] ✅ 添加基础单元测试，覆盖单位换算和天气代码映射。

阶段备注：

```text
已完成领域模型与格式化工具。新增 SavedCity、WeatherReport、CurrentWeather、HourlyForecast、DailyForecast、WeatherSettings 及单位/主题枚举；新增 WeatherCodeMapper 将 Open-Meteo weather_code 映射为中文描述和 WeatherIcon 语义；新增 WindDirectionFormatter 和 UnitConverter，覆盖温度、风速、气压、能见度展示。测试文件位于 app/src/test/java/com/example/lcb/app/weather/domain/mapper，验证天气代码映射、风向和单位换算。验证命令：./gradlew :app:testLocalDebugUnitTest，结果 BUILD SUCCESSFUL。
```

## 阶段 3：Open-Meteo 网络层

- [x] ✅ 添加 Retrofit、OkHttp、kotlinx.serialization 依赖。
- [x] ✅ 添加 Kotlin serialization 插件。
- [x] ✅ 创建 Forecast API DTO。
- [x] ✅ 创建 Geocoding API DTO。
- [x] ✅ 创建 `OpenMeteoForecastApi`。
- [x] ✅ 创建 `OpenMeteoGeocodingApi`。
- [x] ✅ 创建 `WeatherRepository`，负责请求天气并映射为领域模型。
- [x] ✅ 创建 `GeocodingRepository`，负责搜索城市并映射为领域模型。
- [x] ✅ 支持 10 天天气预报请求。
- [x] ✅ 支持当前天气、小时天气、每日天气、指标卡片数据统一返回。
- [x] ✅ 处理网络错误、空响应、字段缺失。
- [x] ✅ 确认接口不需要 API Key。
- [x] ✅ 添加 Repository 层基础测试或至少添加可手动验证的 sample 请求。

阶段备注：

```text
已完成 Open-Meteo 网络层。Gradle 已接入 kotlin-serialization 插件、Retrofit、OkHttp、converter-kotlinx-serialization 和 kotlinx-serialization-json。新增 remote/dto 下 ForecastDtos 与 GeocodingDtos；新增 OpenMeteoForecastApi、OpenMeteoGeocodingApi 和 NetworkModule，baseUrl 分别为 https://api.open-meteo.com/ 与 https://geocoding-api.open-meteo.com/，接口无需 API Key。WeatherRepository 使用当前/小时/每日字段常量请求 10 天天气，并映射 CurrentWeather、HourlyForecast、DailyForecast；GeocodingRepository 负责城市搜索并输出 CitySearchResult。网络错误通过 Result 包装，缺失 current 会返回失败，小时/每日缺字段会跳过或用 null 容错。测试新增 WeatherRepositoryTest 校验请求字段覆盖核心需求。验证命令：./gradlew :app:testLocalDebugUnitTest，结果 BUILD SUCCESSFUL。
```

## 阶段 4：本地存储

- [x] ✅ 添加 DataStore Preferences 依赖。
- [x] ✅ 创建 `CityStore`，保存和读取城市列表。
- [x] ✅ 城市列表使用 JSON 存储，保持排序字段 `sortIndex`。
- [x] ✅ 支持添加城市。
- [x] ✅ 支持删除城市。
- [x] ✅ 支持更新城市排序。
- [x] ✅ 支持读取当前选中城市 ID。
- [x] ✅ 支持设置当前选中城市。
- [x] ✅ 创建 `SettingsStore`，保存天气单位和主题设置。
- [x] ✅ 设置项变更后通过 Flow 通知 UI。
- [x] ✅ 城市列表变更后通过 Flow 通知 UI。
- [x] ✅ 处理首次无城市的空状态。

阶段备注：

```text
已完成本地存储层。app 模块接入 DataStore Preferences；新增 WeatherPreferences 统一创建 weather_preferences DataStore。CityStore 使用 JSON 字符串保存 List<SavedCity>，并提供 cities、selectedCityId、selectedCity Flow；支持 addCity、upsertCity、deleteCity、updateSort、setSelectedCity，删除当前城市时自动切到剩余首项，城市为空时清除 selectedCityId。SettingsStore 保存温度、风速、气压、能见度和主题枚举，settings Flow 默认返回 WeatherSettings。SavedCity 和 WeatherSettings/枚举已添加 kotlinx.serialization 支持。验证命令：./gradlew :app:testLocalDebugUnitTest，结果 BUILD SUCCESSFUL。
```

## 阶段 5：首次启动与系统定位

- [x] ✅ 在 `AndroidManifest.xml` 添加定位权限。
- [x] ✅ 首选 `ACCESS_COARSE_LOCATION`，必要时兼容 `ACCESS_FINE_LOCATION`。
- [x] ✅ 创建定位数据源或定位 Repository。
- [x] ✅ App 首次启动时检查本地城市列表。
- [x] ✅ 如果已有城市，直接进入主天气页。
- [x] ✅ 如果无城市，请求系统定位权限。
- [x] ✅ 用户同意定位后，通过系统定位 API 获取经纬度。
- [x] ✅ 使用经纬度请求 Open-Meteo 天气接口。
- [x] ✅ 使用 Android `Geocoder` 尝试反查城市名、省份、国家。
- [x] ✅ 反查失败时城市名显示“当前位置”。
- [x] ✅ 将当前位置保存为默认城市，`sortIndex = 0`。
- [x] ✅ 当前位置城市的 `timezone` 默认使用 `auto`。
- [x] ✅ 用户拒绝定位后，跳转或引导到添加城市页。
- [x] ✅ 定位失败后，显示错误态并提供“搜索城市”和“重试定位”。
- [x] ✅ 定位流程不阻塞后续手动搜索城市。

阶段备注：

```text
已完成首次启动定位流程。AndroidManifest 增加 ACCESS_COARSE_LOCATION 与 ACCESS_FINE_LOCATION，Compose 根入口优先请求 ACCESS_COARSE_LOCATION。新增 AppContainer，在 LcbApp 中懒加载组装 CityStore、SettingsStore、WeatherRepository、GeocodingRepository、LocationRepository。LocationRepository 使用 Android LocationManager，优先 NETWORK_PROVIDER，兼容 GPS_PROVIDER；先取 30 分钟内 lastKnownLocation，否则单次 requestLocationUpdates，10 秒超时；Geocoder 反查城市名、省份、国家，失败时名称使用“当前位置”，timezone 保存 auto。StartupViewModel 启动时检查本地城市：有城市直接 Ready，无城市则请求定位；授权后定位并调用 WeatherRepository 请求一次 Open-Meteo 天气，成功后保存 current_location；拒绝或失败时进入手动搜索引导状态并提供重试定位。当前 UI 仍是阶段性占位，阶段 6 会接入正式导航。验证命令：./gradlew :app:compileLocalDebugKotlin，结果 BUILD SUCCESSFUL。
```

## 阶段 6：导航结构

- [ ] 创建 `WeatherNavGraph`。
- [ ] 定义主天气页路由。
- [ ] 定义城市管理页路由。
- [ ] 定义添加城市页路由。
- [ ] 定义设置页路由。
- [ ] 定义关于页路由。
- [ ] 定义隐私协议页路由。
- [ ] 支持从城市管理页点击城市进入对应城市天气页。
- [ ] 支持从主天气页进入设置页。
- [ ] 支持从主天气页进入城市管理页。
- [ ] 支持从城市管理页进入添加城市页。
- [ ] 支持设置页进入关于页和隐私协议页。

阶段备注：

```text
待填写。
```

## 阶段 7：主天气页

### 顶部区域

- [ ] 显示城市名。
- [ ] 显示城市管理按钮。
- [ ] 显示设置按钮。
- [ ] 城市名或城市管理按钮可进入城市管理页。
- [ ] 设置按钮可进入设置页。

### 当前天气区域

- [ ] 显示当前温度。
- [ ] 显示天气状态中文描述。
- [ ] 显示最高温和最低温。
- [ ] 显示体感温度。
- [ ] 根据昼夜和天气状态切换背景氛围。
- [ ] 当前天气加载中时显示骨架或进度态。
- [ ] 当前天气失败时显示错误提示和重试入口。

### 小时天气

- [ ] 横向列表展示未来小时天气。
- [ ] 每项显示时间。
- [ ] 每项显示天气图标语义。
- [ ] 每项显示温度。
- [ ] 每项显示降水概率。
- [ ] 当前小时高亮。

### 未来 7 天 / 10 天

- [ ] 默认展示 10 天天气。
- [ ] 每日条目显示日期或星期。
- [ ] 每日条目显示天气状态。
- [ ] 每日条目显示最高温。
- [ ] 每日条目显示最低温。
- [ ] 每日条目显示降水概率。
- [ ] 列表视觉上类似 iOS 天气但保持 Material3 风格。

### 指标模块

- [ ] 显示湿度。
- [ ] 显示风速和风向。
- [ ] 显示气压。
- [ ] 显示能见度。
- [ ] 显示紫外线指数。
- [ ] 显示降水概率。
- [ ] 显示日出和日落。
- [ ] 指标使用 2 列网格布局。
- [ ] 指标卡片在深浅色主题下均可读。

### 刷新与状态

- [ ] 支持进入页面自动加载。
- [ ] 支持切换城市后刷新。
- [ ] 支持设置单位变化后刷新展示。
- [ ] 支持手动重试。
- [ ] 网络失败时保留旧数据优先展示。

阶段备注：

```text
待填写。
```

## 阶段 8：城市管理页

- [ ] 页面视觉参考 iOS 天气城市列表。
- [ ] 顶部显示搜索框。
- [ ] 搜索框可过滤已添加城市。
- [ ] 每个城市卡片显示城市名。
- [ ] 每个城市卡片显示当前温度。
- [ ] 每个城市卡片显示天气状态。
- [ ] 每个城市卡片显示最高温和最低温。
- [ ] 点击城市卡片进入该城市天气页。
- [ ] 支持添加城市入口。
- [ ] 支持删除城市。
- [ ] 支持拖拽排序。
- [ ] 拖拽排序完成后保存新的 `sortIndex`。
- [ ] 删除当前选中城市后自动选择剩余列表第一项。
- [ ] 删除最后一个城市后回到无城市状态，并引导添加城市或定位。
- [ ] 城市天气摘要加载失败时不影响城市列表操作。

阶段备注：

```text
待填写。
```

## 阶段 9：添加城市页

- [ ] 页面顶部显示返回按钮和搜索输入框。
- [ ] 输入城市名后触发搜索。
- [ ] 少于 2 个字符不请求接口。
- [ ] 输入防抖，建议 300ms。
- [ ] 调用 Open-Meteo Geocoding API。
- [ ] 搜索结果显示城市名。
- [ ] 搜索结果显示国家和省份。
- [ ] 搜索结果可显示经纬度作为辅助信息。
- [ ] 点击搜索结果后保存城市。
- [ ] 已添加城市显示“已添加”状态，避免重复保存。
- [ ] 添加成功后返回城市管理页或进入该城市天气页。
- [ ] 无结果时显示空状态。
- [ ] 网络失败时显示错误和重试。
- [ ] 搜索过程中显示加载状态。

阶段备注：

```text
待填写。
```

## 阶段 10：设置页

### 单位设置

- [ ] 温度单位支持摄氏度。
- [ ] 温度单位支持华氏度。
- [ ] 风力单位支持 km/h。
- [ ] 风力单位支持 m/s。
- [ ] 风力单位支持 mph。
- [ ] 风力单位支持节。
- [ ] 气压单位支持 hPa。
- [ ] 气压单位支持 mmHg。
- [ ] 气压单位支持 inHg。
- [ ] 能见度单位支持 km。
- [ ] 能见度单位支持 mile。
- [ ] 单位变更后立即保存。
- [ ] 单位变更后主天气页展示立即更新。

### 主题设置

- [ ] 主题支持跟随系统。
- [ ] 主题支持浅色。
- [ ] 主题支持深色。
- [ ] 主题切换即时生效。

### 其他入口

- [ ] 设置页包含关于入口。
- [ ] 设置页包含隐私协议入口。

阶段备注：

```text
待填写。
```

## 阶段 11：关于页与隐私协议

### 关于页

- [ ] 显示 App 名称。
- [ ] 显示版本号。
- [ ] 版本号从 `BuildConfig.VERSION_NAME` 获取。
- [ ] 显示数据来源说明。
- [ ] 显示 Open-Meteo API 来源。
- [ ] 显示联系方式占位或配置项。

### 隐私协议

- [ ] 说明定位用途：用于获取当前位置天气。
- [ ] 说明城市数据保存方式：保存在本地设备。
- [ ] 说明天气数据来源：Open-Meteo。
- [ ] 说明不主动上传用户保存的城市列表到自有服务器。
- [ ] 说明拒绝定位后仍可手动搜索城市。

阶段备注：

```text
待填写。
```

## 阶段 12：UI/UX 完善

- [ ] 所有页面支持深色模式。
- [ ] 所有页面支持浅色模式。
- [ ] 所有主要按钮有清晰点击反馈。
- [ ] 文本在小屏幕下不截断关键内容。
- [ ] 主天气页视觉层级清晰，当前天气优先级最高。
- [ ] 城市管理页卡片密度适中，适合频繁切换城市。
- [ ] 添加城市页搜索状态明确。
- [ ] 设置页使用清晰分组。
- [ ] 空态、错误态、加载态文案清晰。
- [ ] 避免过度复杂动画，保留轻量过渡。

阶段备注：

```text
待填写。
```

## 阶段 13：质量验证

- [ ] 运行 Gradle 编译。
- [ ] 运行单元测试。
- [ ] 手动验证首次启动定位成功流程。
- [ ] 手动验证首次启动拒绝定位流程。
- [ ] 手动验证定位失败后的搜索城市流程。
- [ ] 手动验证添加城市。
- [ ] 手动验证删除城市。
- [ ] 手动验证拖拽排序。
- [ ] 手动验证切换城市。
- [ ] 手动验证单位设置。
- [ ] 手动验证主题设置。
- [ ] 手动验证无网络错误态。
- [ ] 手动验证 Open-Meteo 返回空数据时的容错。
- [ ] 确认 release/debug flavor 编译不受影响。

阶段备注：

```text
待填写。
```

## 待确认或后续增强

- [ ] 是否需要当前位置城市跟随设备移动自动更新。
- [ ] 是否需要手动刷新按钮或下拉刷新。
- [ ] 是否需要天气通知。
- [ ] 是否需要桌面小组件。
- [ ] 是否需要离线缓存天气数据。
- [ ] 是否需要空气质量数据。
- [ ] 是否需要多语言。
- [ ] 是否需要广告位与天气页面融合。

变更说明：

```text
2026-05-08：确认首次进入默认使用系统定位 API 获取经纬度；定位失败或用户拒绝后，引导用户搜索城市。明确不使用 IP 定位作为首选方案。
```
