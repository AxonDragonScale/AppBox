> Context: [Main](../../context.md) > [Commons](./context.md)

# Commons Module — `:core:commons`

## Overview

The `commons` module is a shared Android library that provides common utilities, extensions, and infrastructure used across all apps in the AppBox monorepo. It serves as the foundation layer that all app modules depend on.

**Package**: `com.axondragonscale.commons`

## Package Structure

```
com.axondragonscale.commons/
├── compose/
│   ├── ComposeUtils.kt      # Window inset height helpers
│   ├── Dropdown.kt          # Reusable Dropdown composable + DropdownItem
│   └── ModifierExt.kt       # Compose Modifier extensions
├── devtools/
│   ├── DevTool.kt           # DevTool sealed interface definitions
│   ├── DevToolItems.kt      # Composables for rendering tools
│   ├── DevToolsBuilder.kt   # Builder DSL for creating tools
│   └── DevToolsScreen.kt    # Main DevTools screen composable
├── di/
│   └── CommonsModule.kt     # Koin DI module
├── network/
│   └── HttpClient.kt        # Ktor HTTP client setup
├── qr/
│   └── QRGenerator.kt       # QR code bitmap generation via ZXing
├── room/
│   ├── IntListConverter.kt      # Room TypeConverter for List<Int>
│   ├── LocalDateConverter.kt    # Room TypeConverter for kotlinx.datetime.LocalDate
│   ├── LocalTimeConverter.kt    # Room TypeConverter for kotlinx.datetime.LocalTime
│   └── StringListConverter.kt   # Room TypeConverter for List<String>
├── ContextExt.kt            # Android Context extensions
├── FlowExt.kt               # Flow collection helpers and multi-flow combine
├── Logger.kt                # Logging utilities
├── PermissionExt.kt         # Runtime permission helpers
├── PreferenceExt.kt         # DataStore preference wrappers
├── Result.kt                # Result type for error handling
├── Serialization.kt         # JSON serialization setup
└── UIMode.kt                # Common Light/Auto/Dark theme enum
```

## Koin DI Module (`CommonsModule`)

```kotlin
val CommonsModule = module {
    single { createJson() }                      // Json instance
    single { createHttpClient(get(), get()) }   // HttpClient instance
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

### Logging (`Logger.kt`)

Flexible logging system with extension functions for any object:

```kotlin
// Usage with TAG
private const val TAG = "MyClass"
TAG.logD { "Debug message" }
TAG.logE { "Error message" }

// Usage with class instance
this.logD { "Debug message from ${this.javaClass.simpleName}" }

// Available log levels: logD, logE, logI, logW, logV, logA

// Global configuration
Logger.isLoggingEnabled = true  // Enable/disable all logging
Logger.usePrintlnLogs = false   // Use println instead of Log (for tests)
```

### Result Type (`Result.kt`)

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

### Flow Extensions (`FlowExt.kt`)

Helpers for Flow collection and multi-flow combining:

```kotlin
// No-op collection — triggers flow without handling emissions
suspend fun <T> Flow<T>.collect() = this.collect {}

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

// Combine 6 flows
fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1, flow2, flow3, flow4, flow5, flow6, transform
): Flow<R>

// Combine 7 flows
fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow1, flow2, flow3, flow4, flow5, flow6, flow7, transform
): Flow<R>
```

The multi-flow `combine` functions extend Kotlin's standard library which only supports up to 5 flows. They use internal `Tuple5`/`Tuple6` data classes to chain standard `combine` calls.

### UIMode (`UIMode.kt`)

Common enum for Light/Dark/Auto theme control, shared across all apps:

```kotlin
enum class UIMode(val displayName: String, val icon: ImageVector) {
    LIGHT("Light", Icons.Default.LightMode),
    AUTO("Auto", Icons.Default.BrightnessAuto),
    DARK("Dark", Icons.Default.DarkMode);

    companion object {
        val DEFAULT get() = AUTO
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}

// Composable extension to resolve to a dark theme boolean
@Composable
fun UIMode.isDarkTheme(): Boolean  // AUTO delegates to isSystemInDarkTheme()
```

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

### DataStore Preferences (`PreferenceExt.kt`)

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

### Permission Utilities (`PermissionExt.kt`)

```kotlin
// Get all dangerous permissions with their grant status
val permissions: List<PermissionState> = context.getDangerousPermissions()

// Check single permission
val hasCamera = context.isPermissionGranted(Manifest.permission.CAMERA)

// Get array of missing permissions for request
val missing: Array<String> = permissions.getMissingPermissions()
launcher.launch(missing)
```

### Context Extensions (`ContextExt.kt`)

```kotlin
// Clear all app data (preferences, databases, cache) and kill process
context.clearAppData()
```

### Compose Modifier Extensions (`compose/ModifierExt.kt`)

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

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, module index
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming conventions
- [Build System](../../build-system.md) — Convention plugins, version catalog, module setup