# Commons Module Context

## Overview

The `commons` module is a shared Android library that provides common utilities, extensions, and infrastructure used across all apps in the AppBox monorepo. It serves as the foundation layer that all app modules depend on.

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

## Module Dependencies

```kotlin
dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    // Networking
    implementation(libs.bundles.ktor)

    // Kotlin
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // DataStore
    implementation(libs.datastore.prefs)
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

// Available log levels
logD { } // Debug
logE { } // Error
logI { } // Info
logW { } // Warning
logV { } // Verbose
logA { } // Assert

// Global configuration
Logger.isLoggingEnabled = true  // Enable/disable all logging
Logger.usePrintlnLogs = false   // Use println instead of Log (for tests)
```

### Result Type (`Result.kt`)

Type-safe result wrapper for operations that can succeed or fail:

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

// Handling results
result
    .onSuccess { data -> /* use data */ }
    .onFailure { error -> /* handle error */ }

// Checking state
if (result.isSuccess()) {
    val data = result.getSuccessResult()
}

// Safe access
val dataOrNull = result.getSuccessResultOrNull()
```

### DataStore Preferences (`PreferenceExt.kt`)

Wrappers for type-safe DataStore preference access:

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

Helpers for runtime permission handling:

```kotlin
// Get all dangerous permissions with their grant status
val permissions: List<PermissionState> = context.getDangerousPermissions()

// PermissionState contains:
data class PermissionState(
    val name: String,      // e.g., "android.permission.CAMERA"
    val granted: Boolean
)

// Check single permission
val hasCamera = context.isPermissionGranted(Manifest.permission.CAMERA)

// Get array of missing permissions for request
val missing: Array<String> = permissions.getMissingPermissions()
launcher.launch(missing)
```

### Context Extensions (`ContextExt.kt`)

Android Context utility extensions:

```kotlin
// Clear all app data (preferences, databases, cache) and kill process
context.clearAppData()
```

### Compose Modifier Extensions (`compose/ModifierExt.kt`)

Conditional modifier utilities:

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

Pre-configured Ktor HTTP client:

```kotlin
// Provided via Koin DI
val httpClient: HttpClient by inject()

// Configuration includes:
// - OkHttp engine
// - JSON content negotiation with kotlinx.serialization
// - 15 second timeouts (request, connect, socket)
// - Chucker interceptor for debugging (shows network calls in notification)
```

### Serialization (`Serialization.kt`)

Configured JSON serializer:

```kotlin
// Provided via Koin DI
val json: Json by inject()

// Configuration:
// - ignoreUnknownKeys = true (lenient parsing)
// - encodeDefaults = true (include default values in output)
```

### Koin DI Module (`di/CommonsModule.kt`)

```kotlin
val CommonsModule = module {
    single { createJson() }                      // Json instance
    single { createHttpClient(get(), get()) }   // HttpClient instance
}

// Usage in app's Application class:
startKoin {
    modules(
        CommonsModule,  // Always include this
        AppSpecificModule,
    )
}
```

## Usage in App Modules

### Adding Dependency

```kotlin
// In app's build.gradle.kts
dependencies {
    implementation(projects.core.commons)
}
```

### Initializing in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(
                CommonsModule,  // From commons
                MyAppModule,    // App-specific
            )
        }
    }
}
```

## Developer Tools (`devtools/`)

A reusable developer tools screen component that provides common debugging utilities.

### DevTool Types (`DevTool.kt`)

```kotlin
sealed interface DevTool {
    data class Action(...)    // Clickable action
    data class Toggle(...)    // Switch/toggle
    data class Info(...)      // Read-only info display
    data class Section(...)   // Section header
    data class Custom(...)    // Custom composable
}
```

### Building Tools (`DevToolsBuilder.kt`)

```kotlin
// Using builder DSL
val tools = buildDevTools {
    section("Database", Icons.Rounded.Storage)
    action("Clear Cache") { clearCache() }
    toggle("Debug Mode", isDebug) { setDebug(it) }
    info("Version", "1.0.0")
}

// Or using builder class
val tools = DevToolsBuilder()
    .section("Settings")
    .action("Reset", destructive = true) { reset() }
    .build()
```

### DevToolsScreen (`DevToolsScreen.kt`)

Built-in tools included automatically:
- **Kill App**: Force stops the application
- **Clear Data**: Clears all app data and restarts
- **Permissions**: Shows dangerous permissions with request button

```kotlin
// Basic usage with custom tools
DevToolsScreen(
    title = "My App Dev Tools",
    customToolsBuilder = {
        section("App Tools", Icons.Rounded.Build)
        action("Sync Data") { syncManager.sync() }
        toggle("Fast Mode", isFastMode) { setFastMode(it) }
    }
)

// Or with pre-built list
DevToolsScreen(
    customTools = myToolsList,
    extraContent = {
        // Additional composable content
    }
)
```

### Integration Example

```kotlin
// 1. Create app-specific DevTools screen
@Composable
fun MyAppDevTools(
    viewModel: MyViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    DevToolsScreen(
        title = "MyApp Dev Tools",
        customToolsBuilder = {
            section("App Info")
            info("Version", BuildConfig.VERSION_NAME)
            
            section("Debug Tools")
            action("Clear Cache") { viewModel.clearCache() }
            toggle("Debug Logging", state.debugLogging) { 
                viewModel.setDebugLogging(it) 
            }
        }
    )
}

// 2. Add route
@Serializable data object DevTools

// 3. Add to navigation
composable<Route.DevTools> {
    MyAppDevTools()
}
```

## Best Practices

1. **Logging**: Use lazy message evaluation with lambda syntax `{ }` to avoid string concatenation when logging is disabled

2. **Result Type**: Prefer `Result` over throwing exceptions for expected failure cases

3. **Preferences**: Use the typed wrappers (`setting`, `enumSetting`, `serializedSetting`) instead of raw DataStore access

4. **Modifiers**: Use `thenIf` extensions to keep modifier chains readable

5. **DI**: Always include `CommonsModule` when setting up Koin in your app

6. **DevTools**: Create an app-specific wrapper around `DevToolsScreen` to add custom tools

## Notes for AI Assistance

1. This module uses **Ktor** for networking, not Retrofit
2. Uses **kotlinx.serialization**, not Gson or Moshi
3. Uses **Koin** for DI, not Hilt or Dagger
4. The `Result` type is custom, not `kotlin.Result`
5. Chucker is included for network debugging in debug builds
6. All utilities are designed to be used across multiple independent apps
7. **DevToolsScreen** provides built-in kill app, clear data, and permissions tools
8. Use `buildDevTools { }` DSL or `DevToolsBuilder()` for creating custom tools
9. Apps should create their own DevTools screen wrapper that adds app-specific tools
10. DevTool.Action with `destructive = true` renders in error/red color