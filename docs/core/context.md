---
title: "Core modules overview"
status: current
summary: "The capability modules that replaced :core:commons: contents, dependency direction, and usage snippets for each."
read_when: "Choosing which core module to depend on, or looking for an existing shared helper."
---

> Context: [Main](../context.md) > Core Modules

# Core Modules — Overview

`:core:commons` no longer exists. It was split into capability modules so a consumer only pays for
what it uses. This file is kept as the entry point for the shared core modules.

## Modules

| Module | Type | Contents | Depends on |
|---|---|---|---|
| [`:core:common`](common/context.md) | KMP, no Compose | Logger, `Result`, `CommonJson`, `DateTimeExt`, pure `Flow.collect()`/`combine`, `AppInfo` | coroutines, serialization-json, datetime |
| `:core:android` | Android library | `ContextExt` (notifications, settings, clear data), `PermissionExt` | androidx-core-ktx |
| `:core:ui` | KMP + Compose | Dropdown, StatCard, charts, date/time pickers, `ModifierExt`, `ComposeUtils`, `UIMode` (`icon`, `isDarkTheme`), `collectWithLifecycle` | `:core:common`, Compose MP, lifecycle-compose |
| `:core:devtools` | KMP + Compose | `DevToolsScreen`, `DevTool`, builder DSL; Android built-ins (kill app, clear data, permissions, logcat) | `:core:ui`, `:core:android` (androidMain), `:libraries:logcat-viewer` (androidMain) |
| `:core:datastore` | KMP | `createPreferencesDataStore`, `pref`, `enumPref`, `serializedPref`, `nullablePref` | datastore-preferences, coroutines, serialization-json, `:core:common` |
| `:core:room` | KMP | Room `TypeConverter`s + `createDatabase` | room-runtime, kotlinx-datetime, sqlite-bundled |
| `:core:network` | KMP | `createHttpClient`, `NetworkModule` (provides `Json` + `HttpClient`); OkHttp/Chucker on Android | ktor, `:core:common`, koin-core |
| `:libraries:qr` | Android library | `QRGenerator` (ZXing) | androidx-core-ktx, zxing |

## Dependency Direction

```text
                  :core:common
        ↑          ↑          ↑          ↑
   :core:ui  :core:datastore  :core:room  :core:network
        ↑
   :core:devtools ──→ :core:android
        ↓ (androidMain)
 :libraries:logcat-viewer
```

No core module depends on an app, and no core module depends on `:core:devtools` or `:core:ui`
except `devtools` itself. `:core:common` must never gain Compose, Koin, Ktor, Room or DataStore.

## Android — `:core:android`

Android-only helpers in the `com.axondragonscale.commons` package:

```kotlin
context.notificationManager
context.openNotificationSettings()
context.openAppSettings()
context.openExactAlarmSettings()
context.canPostNotifications
context.canScheduleExactAlarms
context.clearAppData()

context.getDangerousPermissions()          // List<PermissionState>
context.isPermissionGranted(permission)
permissions.getMissingPermissions()
context.revokeGrantedPermissionsOnKill(permissions)
```

## UI — `:core:ui`

Compose Multiplatform components, all still in `com.axondragonscale.commons.*`:

- `compose/Dropdown.kt` — `Dropdown`, `DropdownItem`
- `compose/StatCard.kt`, `compose/ModifierExt.kt` — `thenIf`, `thenIfElse`, `thenIfNotNull`
- `compose/chart/` — `BarChart`, `RingChart`, `TrendLine`
- `compose/datetime/` — `DatePickerDialog`, `TimePickerDialog`
- `UIMode` + `UIMode.icon` + `UIMode.isDarkTheme()`
- `LifecycleFlowExt.kt` — `Flow.collectWithLifecycle`
- Android-only `compose/ComposeUtils.kt` — `navigationBarsHeight()`, `statusBarHeight()`

Consumers need `implementation(projects.core.ui)` and Compose dependencies (the module exposes
Compose via `api(libs.bundles.compose.mp)`).

## DevTools — `:core:devtools`

```kotlin
DevToolsScreen(
    title = "My App Dev Tools",
    customToolsBuilder = {
        section("App Tools", Icons.Rounded.Build)
        action("Sync Data") { syncManager.sync() }
        toggle("Fast Mode", isFastMode) { setFastMode(it) }
        info("Version", AppInfo.versionName)
    },
)
```

Built-ins on Android: Kill App, Clear App Data, Open App Settings, Permissions, and Open Logcat.
JVM/iOS expose Kill App only. The module is KMP because jest/limitless call it from `commonMain`.

The log viewer entry is Android-only and needs no host wiring: tapping **Open Logcat** presents
`LogcatViewer` as a full-screen dialog (back gesture and the viewer's own back arrow both dismiss
it). `isLogcatAvailable` / `DevToolsLogcatHost` are the expect/actual seam; the JVM and iOS actuals
are no-ops. A host that wants it as a navigation destination instead can call
`LogcatDevToolsScreen(onBack = …)` and add its own route — Devinator does exactly that.

## DataStore — `:core:datastore`

```kotlin
val store = createPreferencesDataStore("settings")

val themeMode = store.enumPref(stringPreferencesKey("ui_mode"), UIMode.AUTO)
themeMode.flow.collectAsState()
themeMode.set(UIMode.DARK)

val deadline = store.nullablePref(longPreferencesKey("deadline"))
val user = store.serializedPref("user", UserSettings())
```

Reads never throw at the caller; failed reads/decodes fall back to the default and are logged.
`CancellationException` still propagates.

> The Android actual of `dataStorePreferencesPath` resolves `Context` through Koin, so apps still
> call `androidContext(...)` in `startKoin`. Keeping that lookup is a candidate for a follow-up.

## Room — `:core:room`

Register the converters on your `@Database`:

```kotlin
@TypeConverters(
    IntListConverter::class,
    StringListConverter::class,
    LocalDateConverter::class,
    LocalTimeConverter::class,
)

val db = createDatabase<MyDatabase>("my.db")
```

Android resolves `Context` through Koin; JVM/iOS use `BundledSQLiteDriver`.

## Network — `:core:network`

Include `NetworkModule` only if the app actually performs network calls (currently only jest):

```kotlin
startKoin {
    androidContext(this@MyApplication)
    modules(NetworkModule, MyModule)
}
```

`NetworkModule` provides `CommonJson` and `createHttpClient()` (15s timeouts, JSON negotiation,
OkHttp + Chucker on Android, CIO on JVM, Darwin on iOS). Apps that don't use HTTP must not include it.

## QR — `:libraries:qr`

```kotlin
val bitmap = QRGenerator.generateQRCode(content, size = 512)
```

Used by topty only. Kept in `libraries` because it is an Android-only utility with an external
(ZXing) dependency, not a core abstraction.

## Related Context

- [Main Context](../context.md) — Project overview, module index
- [Core Common](common/context.md) — Pure Kotlin utilities
- [Coding Conventions](../coding-conventions.md)
- [Build System](../build-system.md)
