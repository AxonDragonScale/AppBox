> Context: [Main](../../context.md) > [Commons](./context.md)

# Commons Module — `:core:commons`

## Overview

The `commons` module is a shared Android library that provides common utilities, extensions, and infrastructure used across all apps in the AppBox monorepo. It serves as the foundation layer that all app modules depend on.

**Package**: `com.axondragonscale.commons`

## Package Structure

```
com.axondragonscale.commons/
├── compose/
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
├── ContextExt.kt            # Android Context extensions
├── Logger.kt                # Logging utilities
├── PermissionExt.kt         # Runtime permission helpers
├── PreferenceExt.kt         # DataStore preference wrappers
├── Result.kt                # Result type for error handling
└── Serialization.kt         # JSON serialization setup
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

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, module index
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming conventions
- [Build System](../../build-system.md) — Convention plugins, version catalog, module setup