> Context: [Main](../../context.md) > [Commons](./context.md)

# Commons Module — `:core:commons`

## Overview

The `commons` module is a **Kotlin Multiplatform** library that provides shared utilities, Compose UI components, and infrastructure used across all apps in the AppBox monorepo. It targets Android, JVM, and iOS (x64, arm64, simulatorArm64).

**Package**: `com.axondragonscale.commons`

## Source Set Structure

```
src/
├── commonMain/          # Pure Kotlin + Compose Multiplatform code
├── androidMain/         # Android-only code (Context, DataStore, Room, Permissions, etc.)
├── jvmMain/             # JVM actual implementations (Logger, HttpClient engine)
└── iosMain/             # iOS actual implementations (Logger, HttpClient engine)
```

## Package Structure

### `commonMain` — shared across all targets

```
com.axondragonscale.commons/
├── compose/
│   ├── chart/
│   │   ├── BarChart.kt       # Configurable bar chart (vertical/horizontal, labels, highlight)
│   │   ├── RingChart.kt      # Circular progress arc with center label
│   │   └── TrendLine.kt      # Smoothed line overlay (Catmull-Rom spline)
│   ├── Dropdown.kt          # Reusable Dropdown composable + DropdownItem
│   └── ModifierExt.kt       # Compose Modifier extensions
│   └── StatCard.kt          # Titled card shell for stat/chart widgets
├── devtools/
│   ├── DevTool.kt           # DevTool sealed interface definitions
│   └── DevToolsBuilder.kt   # Builder DSL for creating tools
├── network/
│   └── HttpClient.kt        # createHttpClient() — common entry point, expect/actual engine
├── DateTimeExt.kt           # kotlinx.datetime extensions
├── FlowExt.kt               # Flow collect() helper and 6/7-flow combine overloads
├── Logger.kt                # Logging utilities (expect/actual dispatch per platform)
├── Result.kt                # Result type for error handling
├── Serialization.kt         # JSON serialization setup
└── UIMode.kt                # Light/Auto/Dark theme enum (icon + isDarkTheme in androidMain)
```

### `androidMain` — Android-only

```
com.axondragonscale.commons/
├── compose/
│   ├── ComposeUtils.kt      # Window inset height helpers (Android-specific WindowInsets)
│   ├── DatePickerDialog.kt  # Material3 date picker dialog
│   └── TimePickerDialog.kt  # Material3 time picker dialog
├── devtools/
│   ├── DevToolItems.kt      # Composables for rendering DevTool items (uses LocalContext)
│   └── DevToolsScreen.kt    # Full DevTools screen (permissions, clear data, kill app)
├── di/
│   └── CommonsModule.kt     # Koin DI module (Android-scoped)
├── network/
│   └── HttpClientActual.kt  # actual createPlatformHttpClient — OkHttp engine + Chucker
├── qr/
│   └── QRGenerator.kt       # QR code bitmap generation via ZXing (android.graphics.Bitmap)
├── room/
│   ├── IntListConverter.kt      # Room TypeConverter for List<Int>
│   ├── LocalDateConverter.kt    # Room TypeConverter for kotlinx.datetime.LocalDate
│   ├── LocalTimeConverter.kt    # Room TypeConverter for kotlinx.datetime.LocalTime
│   └── StringListConverter.kt   # Room TypeConverter for List<String>
├── ContextExt.kt            # Android Context extensions
├── FlowExt.android.kt       # collectWithLifecycle (requires androidx.lifecycle)
├── LoggerActual.kt          # actual dispatchLog — delegates to android.util.Log
├── PermissionExt.kt         # Runtime permission helpers
├── PreferenceExt.kt         # DataStore preference wrappers
└── UIMode.android.kt        # UIMode.icon (ImageVector) + UIMode.isDarkTheme() extensions
```

## `expect`/`actual` Contracts

### `Logger` — `dispatchLog`
- **expect** in `commonMain`: `internal expect fun dispatchLog(priority: LogPriority, tag: String, message: String)`
- **actual** in `androidMain`: delegates to `android.util.Log.println`
- **actual** in `jvmMain` / `iosMain`: delegates to `println`

### `HttpClient` — `createPlatformHttpClient`
- **expect** in `commonMain`: `internal expect fun createPlatformHttpClient(json: Json): HttpClient`
- **actual** in `androidMain`: OkHttp engine + ChuckerInterceptor (requires `HttpClientContext.context` seeded in `CommonsModule`)
- **actual** in `jvmMain` / `iosMain`: CIO engine (Ktor's pure-Kotlin engine)

Public entry point (common): `fun createHttpClient(json: Json): HttpClient`

## Koin DI Module (`CommonsModule`)

```kotlin
val CommonsModule = module {
    single { CommonJson }
    single {
        HttpClientContext.context = get<Context>()  // seeds Chucker interceptor
        createHttpClient(get())
    }
}
```

Every app must include `CommonsModule` when initializing Koin:

```kotlin
startKoin {
    androidLogger()
    androidContext(this@MyApplication)
    modules(
        CommonsModule,  // Always include this
        AppSpecificModule,
    )
}
```

## Components

### Logging (`Logger.kt`) — `commonMain`

Flexible logging system with extension functions for any object. Uses `expect`/`actual` for platform log dispatch.

```kotlin
// Usage with TAG
private const val TAG = "MyClass"
TAG.logD { "Debug message" }
TAG.logE { "Error message" }

// Usage with class instance
this.logD { "Debug message from ${this::class.simpleName}" }

// Available log levels: logD, logE, logI, logW, logV, logA

// Global configuration
Logger.isLoggingEnabled = true  // Enable/disable all logging
Logger.usePrintlnLogs = false   // Force println output on all platforms (useful for tests)
```

Android dispatches to `android.util.Log`; JVM/iOS dispatch to `println`.

### Result Type (`Result.kt`) — `commonMain`

Custom type-safe result wrapper (not `kotlin.Result`):

```kotlin
sealed interface Result<out S, out F> {
    data class Success<S>(val data: S) : Result<S, Nothing>
    data class Failure<F>(val data: F) : Result<Nothing, F>
}

// Usage
fun fetchData(): Result<Data, Error> {
    return try {
        Result.Success(api.getData())
    } catch (e: Exception) {
        Result.Failure(Error(e.message))
    }
}

// Handling
result
    .onSuccess { data -> /* use data */ }
    .onFailure { error -> /* handle error */ }

// Safe access
val dataOrNull = result.getSuccessResultOrNull()
```

### Flow Extensions (`FlowExt.kt` / `FlowExt.android.kt`)

Helpers for Flow collection and multi-flow combining:

**`commonMain` (`FlowExt.kt`)**:
```kotlin
// No-op collection — triggers flow without handling emissions
suspend fun <T> Flow<T>.collect() = this.collect {}

// Combine 6 flows
fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1, flow2, flow3, flow4, flow5, flow6, transform
): Flow<R>

// Combine 7 flows
fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow1, flow2, flow3, flow4, flow5, flow6, flow7, transform
): Flow<R>
```

**`androidMain` (`FlowExt.android.kt`)**:
```kotlin
// Lifecycle-aware one-shot effect collection (for UiEffect channels)
// Starts on STARTED, stops on STOPPED, dispatches on Main.immediate
@Composable
fun <T> Flow<T>.collectWithLifecycle(
    context: CoroutineContext = Dispatchers.Main.immediate,
    onEffect: suspend (T) -> Unit,
)

// Usage in a screen composable:
viewModel.uiEffect.collectWithLifecycle { effect ->
    when (effect) {
        is MyUiEffect.NavigateBack -> navController.popBackStack()
    }
}
```

The multi-flow `combine` functions extend Kotlin's standard library which only supports up to 5 flows. They use internal `Tuple5`/`Tuple6` data classes to chain standard `combine` calls.

### UIMode (`UIMode.kt` / `UIMode.android.kt`)

Common enum for Light/Dark/Auto theme control, shared across all apps.

**`commonMain`** — `UIMode.kt`:
```kotlin
enum class UIMode(val displayName: String) {
    LIGHT("Light"), AUTO("Auto"), DARK("Dark");

    companion object {
        val DEFAULT get() = AUTO
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
```

**`androidMain`** — `UIMode.android.kt` (requires explicit import):
```kotlin
// Icon for use in Android Compose UI
val UIMode.icon: ImageVector  // import com.axondragonscale.commons.icon

// Resolves to dark theme boolean; AUTO delegates to isSystemInDarkTheme()
@Composable
fun UIMode.isDarkTheme(): Boolean  // import com.axondragonscale.commons.isDarkTheme
```

> **Important**: `icon` and `isDarkTheme` are extension properties/functions defined in a separate file. App modules must import them explicitly:
> ```kotlin
> import com.axondragonscale.commons.icon
> import com.axondragonscale.commons.isDarkTheme
> ```

Usage in apps:
```kotlin
// In a preferences repository
val uiMode = dataStore.enumSetting(
    key = stringPreferencesKey("ui_mode"),
    default = UIMode.AUTO
)

// In a theme composable
val isDark = uiMode.isDarkTheme()
MyAppTheme(darkTheme = isDark) { ... }
```

### DataStore Preferences (`PreferenceExt.kt`) — `androidMain`

Typed wrappers for DataStore preference access:

```kotlin
// Simple preference
val themeMode = dataStore.setting(
    key = stringPreferencesKey("theme_mode"),
    default = "system"
)

// Enum preference (stored as string)
val uiMode = dataStore.enumSetting(
    key = stringPreferencesKey("ui_mode"),
    default = UIMode.AUTO
)

// Serializable object preference (stored as JSON)
val userSettings = dataStore.serializedSetting(
    key = "user_settings",
    default = UserSettings()
)

// Usage
val currentTheme = themeMode.flow.collectAsState()
themeMode.set("dark")
val value = themeMode.get() // suspend function
```

### Permission Utilities (`PermissionExt.kt`) — `androidMain`

```kotlin
// Get all dangerous permissions with their grant status
val permissions: List<PermissionState> = context.getDangerousPermissions()

// Check single permission
val hasCamera = context.isPermissionGranted(Manifest.permission.CAMERA)

// Get array of missing permissions for request
val missing: Array<String> = permissions.getMissingPermissions()
launcher.launch(missing)
```

### Context Extensions (`ContextExt.kt`) — `androidMain`

```kotlin
// Clear all app data (preferences, databases, cache) and kill process
context.clearAppData()
```

### Compose Modifier Extensions (`compose/ModifierExt.kt`) — `commonMain`

```kotlin
// Apply modifier only if condition is true
Modifier.thenIf(isSelected) {
    background(Color.Blue)
}

// Apply different modifiers based on condition
Modifier.thenIfElse(
    condition = isEnabled,
    ifTrue = { clickable { onClick() } },
    ifFalse = { alpha(0.5f) }
)

// Apply modifier only if value is not null
Modifier.thenIfNotNull(borderColor) { color ->
    border(1.dp, color)
}
```

### Compose Utilities (`compose/ComposeUtils.kt`)

Composable helpers for window inset measurements:

```kotlin
// Get the navigation bar height as Dp
@Composable
fun navigationBarsHeight(): Dp

// Get the status bar height as Dp
@Composable
fun statusBarHeight(): Dp
```

Useful for manual padding calculations when not using `Scaffold` or `WindowInsets` modifiers directly.

### Dropdown (`compose/Dropdown.kt`)

A reusable, generic dropdown selector composable used across multiple apps (topty settings, limitless editor, backup format selection, etc.):

```kotlin
data class DropdownItem<T>(
    val name: String,
    val value: T,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
)

@Composable
fun <T> Dropdown(
    modifier: Modifier = Modifier,
    selectedItem: DropdownItem<T>,
    items: List<DropdownItem<T>>,
    onItemSelected: (DropdownItem<T>) -> Unit,
    contentPadding: PaddingValues = PaddingValues(start = 12.dp, end = 8.dp),
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.outline,
)
```

Features:
- Generic type parameter — works with any value type
- Optional leading/trailing icons per item
- Customizable shape, border width/color, and content padding
- Auto-sizes the dropdown menu to match the trigger width
- Shows a down-arrow indicator

Usage:
```kotlin
// UIMode picker with icons
Dropdown(
    modifier = Modifier.size(width = 128.dp, height = 32.dp),
    selectedItem = DropdownItem(uiMode.displayName, uiMode, leadingIcon = uiMode.icon),
    items = UIMode.entries.map { DropdownItem(it.displayName, it, leadingIcon = it.icon) },
    onItemSelected = { onEvent(MyUiEvent.UIModeChanged(it.value)) },
)

// Simple text dropdown
Dropdown(
    modifier = Modifier.size(width = 128.dp, height = 32.dp),
    selectedItem = DropdownItem(selected.name, selected),
    items = options.map { DropdownItem(it.name, it) },
    onItemSelected = { onEvent(MyUiEvent.OptionChanged(it.value)) },
    shape = RoundedCornerShape(4.dp),
)
```

### Chart Primitives (`compose/chart/`)

Reusable, data-agnostic chart composables shared across apps. All use `Modifier.drawWithCache` internally so draw commands and `Path` objects are only recomputed when input data changes.

#### BarChart

Configurable bar chart supporting both vertical and horizontal orientations:

```kotlin
data class BarEntry(
    val value: Float,
    val label: String = "",
    val color: Color = Color.Unspecified,      // falls back to style.barColor
    val labelColor: Color = Color.Unspecified,  // falls back to style.labelColor
)

enum class BarChartOrientation { Vertical, Horizontal }

data class BarChartStyle(
    val orientation: BarChartOrientation,
    val barColor: Color,
    val barCornerRadius: Dp,
    val barSpacing: Dp,
    val labelStyle: TextStyle,
    val labelColor: Color,
) {
    companion object {
        @Composable
        fun default(
            orientation: BarChartOrientation = BarChartOrientation.Vertical,
            barColor: Color = MaterialTheme.colorScheme.primary,
            barCornerRadius: Dp = 4.dp,
            barSpacing: Dp = 4.dp,
            labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
            labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        ) = BarChartStyle(...)
    }
}

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    bars: List<BarEntry>,
    style: BarChartStyle = BarChartStyle.default(),
)
```

- **`BarEntry`**: each bar carries its own value, label, and optional color overrides (`Color.Unspecified` falls back to `style.barColor`/`style.labelColor`)
- **`BarChartStyle`**: groups all visual config. `@Composable default()` factory resolves theme-aware defaults from `MaterialTheme`; override only the fields you need
- **Vertical**: bars grow upward, labels below bars
- **Horizontal**: bars grow rightward, labels to the left
- Per-bar colors make highlighting trivial — just set colors directly on the entries
- Labels are rendered via `TextMeasurer` — set `BarEntry.label` per bar, or leave empty for no labels

#### RingChart

Circular progress arc with optional center label (Apple Watch ring style):

```kotlin
data class RingChartTrackStyle(
    val color: Color,                   // progress arc color
    val trackColor: Color,              // defaults to color at 15% alpha
    val strokeWidth: Dp = 8.dp,
    val strokeCap: StrokeCap = StrokeCap.Round,
) {
    companion object {
        @Composable
        fun default(
            color: Color = MaterialTheme.colorScheme.primary,
            trackColor: Color = color.copy(alpha = 0.15f),
            strokeWidth: Dp = 8.dp,
            strokeCap: StrokeCap = StrokeCap.Round,
        ) = RingChartTrackStyle(...)
    }
}

data class RingChartLabelStyle(
    val style: TextStyle,
    val color: Color,
) {
    companion object {
        @Composable
        fun default(
            style: TextStyle = MaterialTheme.typography.titleMedium,
            color: Color = MaterialTheme.colorScheme.onSurface,
        ) = RingChartLabelStyle(...)
    }
}

@Composable
fun RingChart(
    modifier: Modifier = Modifier,
    progress: Float,                    // 0f..1f, coerced
    startAngle: Float = -90f,           // top (12 o'clock)
    label: String = "",
    trackStyle: RingChartTrackStyle = RingChartTrackStyle.default(),
    labelStyle: RingChartLabelStyle = RingChartLabelStyle.default(),
)
```

- **`RingChartTrackStyle`**: arc color, track color, stroke geometry. `trackColor` defaults to `color.copy(alpha = 0.15f)` via preceding param reference in the factory. `@Composable default()` factory resolves arc color from `MaterialTheme`.
- `startAngle` stays top-level — it's a layout concern, not a style property
- **`RingChartLabelStyle`**: center label text appearance. `@Composable default()` factory resolves from `MaterialTheme`.
- Center label (e.g., "82%") rendered via `TextMeasurer`
- Starts from top (-90°) by default with round stroke caps

#### TrendLine

Smoothed line overlay for layering on top of bar charts:

```kotlin
@Composable
fun TrendLine(
    modifier: Modifier = Modifier,
    points: List<Float>,                // ≥ 2 points required
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
    smooth: Boolean = true,
)
```

- Points are evenly spaced across the composable width
- `smooth = true` uses Catmull-Rom spline → cubic Bézier interpolation
- `smooth = false` draws straight line segments
- Give it the same size as a `BarChart` to overlay aligned trend lines

### QR Code Generation (`qr/QRGenerator.kt`)

Simple QR code bitmap generator using ZXing:

```kotlin
object QRGenerator {
    fun generateQRCode(content: String, size: Int = 512): Bitmap
}
```

- Uses `QRCodeWriter` from ZXing to encode content as a `BitMatrix`
- Renders a black-and-white `Bitmap` (RGB_565 format)
- Default size: 512×512 pixels
- Used by topty for token sharing QR codes

### Networking (`network/HttpClient.kt`)

Pre-configured Ktor HTTP client provided via Koin:

```kotlin
val httpClient: HttpClient by inject()

// Configuration:
// - OkHttp engine
// - JSON content negotiation with kotlinx.serialization
// - 15 second timeouts (request, connect, socket)
// - Chucker interceptor for debugging (shows network calls in notification)
```

### Serialization (`Serialization.kt`)

Configured JSON serializer provided via Koin:

```kotlin
val json: Json by inject()

// Configuration:
// - ignoreUnknownKeys = true (lenient parsing)
// - encodeDefaults = true (include default values in output)
```

## Room TypeConverters (`room/`)

Shared Room TypeConverters — register with `@TypeConverters(...)` on your `RoomDatabase` class. All handle nullability.

- **`IntListConverter`** — `List<Int>` ↔ JSON string (e.g. `"[1,2,3]"`)
- **`StringListConverter`** — `List<String>` ↔ JSON string
- **`LocalDateConverter`** — `kotlinx.datetime.LocalDate?` ↔ ISO-8601 string (`YYYY-MM-DD`)
- **`LocalTimeConverter`** — `kotlinx.datetime.LocalTime?` ↔ ISO-8601 string (`HH:MM:SS`)

## Developer Tools (`devtools/`)

A reusable developer tools screen that provides common debugging utilities.

### DevTool Types

```kotlin
sealed interface DevTool {
    data class Action(...)    // Clickable action
    data class Toggle(...)    // Switch/toggle
    data class Info(...)      // Read-only info display
    data class Section(...)   // Section header
    data class Custom(...)    // Custom composable
}
```

### Building Tools

```kotlin
// Using builder DSL
val tools = buildDevTools {
    section("Database", Icons.Rounded.Storage)
    action("Clear Cache") { clearCache() }
    toggle("Debug Mode", isDebug) { setDebug(it) }
    info("Version", "1.0.0")
}
```

### DevToolsScreen

Built-in tools included automatically:
- **Kill App**: Force stops the application
- **Clear Data**: Clears all app data and restarts
- **Permissions**: Shows dangerous permissions with request button

```kotlin
// Usage — wrap in an app-specific composable
DevToolsScreen(
    title = "My App Dev Tools",
    customToolsBuilder = {
        section("App Tools", Icons.Rounded.Build)
        action("Sync Data") { syncManager.sync() }
        toggle("Fast Mode", isFastMode) { setFastMode(it) }
    }
)
```

`DevTool.Action` with `destructive = true` renders in error/red color.

## Adding as a Dependency

```kotlin
// In app's build.gradle.kts
dependencies {
    implementation(projects.core.commons)
}
```

## Best Practices

1. **Logging**: Use lazy message evaluation with lambda syntax `{ }` to avoid string concatenation when logging is disabled
2. **Result Type**: Prefer `Result` over throwing exceptions for expected failure cases
3. **Preferences**: Use the typed wrappers (`setting`, `enumSetting`, `serializedSetting`) instead of raw DataStore access
4. **Modifiers**: Use `thenIf` extensions to keep modifier chains readable
5. **DI**: Always include `CommonsModule` when setting up Koin in your app
6. **DevTools**: Create an app-specific wrapper around `DevToolsScreen` to add custom tools
7. **UIMode**: Use the shared `UIMode` enum instead of defining app-specific theme mode enums
8. **Room Converters**: Use the shared converters from `commons.room` instead of duplicating them per app
9. **Flow Effects**: Use `Flow.collectWithLifecycle` for one-shot UiEffect collection in composables
10. **QR Codes**: Use `QRGenerator` for simple black-and-white QR codes; for customized QR rendering (colors, shapes, logos), build on top of ZXing's `BitMatrix` directly
11. **Dropdown**: Use the shared `Dropdown` composable for all selector/picker UI instead of building custom ones per app
12. **Charts**: Use the shared `BarChart`, `RingChart`, and `TrendLine` composables from `commons.compose.chart` instead of writing custom Canvas code per app

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, module index
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming conventions
- [Build System](../../build-system.md) — Convention plugins, version catalog, module setup