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
| [`:core:common`](common/context.md) | KMP, no Compose | Logger with pluggable `LogSink`s, `Result`, `CommonJson`, `DateTimeExt`, pure `Flow.collect()`/`combine`, `AppInfo` | coroutines, serialization-json, datetime |
| `:core:android` | Android library | `ContextExt` (notifications, settings, clear data), `PermissionExt` | `:core:common`, androidx-core-ktx |
| `:core:ui` | KMP + Compose | Dropdown, StatCard, charts, date/time pickers, `ModifierExt`, `ComposeUtils`, `UIMode` (`icon`, `isDarkTheme`), `collectWithLifecycle` | `:core:common`, Compose MP, lifecycle-compose |
| `:core:devtools` | KMP + Compose | `DevToolsScreen`, `DevTool`, builder DSL, `DevToolsTheme` (+ `DevToolsColorScheme`, `DevToolsSystemBars`); Android built-ins (kill app, clear data, permissions, logcat, logs, storage) and the dev-only log store | `:core:android` (androidMain), `:libraries:log-viewer`, `:libraries:storage-viewer` (androidMain), Room |
| `:core:datastore` | KMP | `createPreferencesDataStore`, `pref`, `enumPref`, `serializedPref`, `nullablePref` | datastore-preferences, coroutines, serialization-json, `:core:common` |
| `:core:room` | KMP | Room `TypeConverter`s + `createDatabase` | room-runtime, kotlinx-datetime, sqlite-bundled |
| `:core:network` | KMP | `createHttpClient`, `NetworkModule` (provides `Json` + `HttpClient`); OkHttp + Chucker on Android, where a non-dev build type gets the no-op artifact | ktor, `:core:common`, koin-core |
| `:libraries:qr` | Android library | `QRGenerator` (ZXing) | androidx-core-ktx, zxing |

## Dependency Direction

```text
                  :core:common
        ↑          ↑          ↑          ↑          ↑
   :core:ui  :core:datastore  :core:room  :core:network  :core:android
                                                            ↑
   :core:devtools ───────────────────────────────────────────┘
        ↓ (androidMain)
 :libraries:log-viewer  :libraries:storage-viewer
```

No core module depends on an app, and no core module depends on `:core:devtools`. The dev-tools
theme lives in `:core:devtools` rather than `:core:ui`, because only developer tools render in it:
the two viewer libraries inherit it from the composition they are hosted in and keep no `:core:ui`
dependency. `:core:common` must never gain Compose, Koin, Ktor, Room or DataStore.

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

Built-ins on Android: Kill App, Clear App Data, Open App Settings, Permissions, Open Logcat and
Open Storage. JVM/iOS expose Kill App only. The module is KMP because jest/limitless call it from
`commonMain`.

The screen renders inside `DevToolsTheme`, so a debug tool looks the same in every app: fixed dark
neutral palette, mint primary, default Material type and shapes — the host's theme, dynamic colour
and light/dark choice never reach it.

The theme lives in this module: `DevToolsTheme`, the `DevToolsColorScheme` behind it, and
`DevToolsSystemBars()` (which flips the window's system-bar icons to their light appearance, resolving
the dialog's window or the activity's, and is a no-op on JVM and iOS). Neither viewer library wraps
itself in the theme any more, so the Android hosts here — `DevToolsLogcatHost`,
`DevToolsStorageHost`, `LogcatDevToolsScreen`, `StorageDevToolsScreen` — are the single place it is
applied to them.

The logcat entry is Android-only: tapping **Open Logcat** presents the shared `LogViewer` over
`LogcatSource` as a full-screen dialog (back gesture and the viewer's own back arrow both dismiss
it). Without `READ_LOGS`, which Android 17 grants only over ADB, the dialog shows the command that
grants it and re-checks when the app returns. `isLogcatAvailable` / `DevToolsLogcatHost` are the expect/actual seam;
the JVM and iOS actuals are no-ops. A host that wants it as a navigation destination instead can call
`LogcatDevToolsScreen(onBack = …)` and add its own route.

The storage entry sits beside it in the same Diagnostics block and works the same way: **Open
Storage** presents `StorageViewer` as a full-screen dialog over the app's own databases,
DataStore/SharedPreferences stores and files. `isStorageViewerAvailable` /
`DevToolsStorageHost` are its expect/actual seam, with no-ops on JVM and iOS; the
[storage viewer library](../libraries/storage-viewer/context.md) holds the reader itself, so the
library stays off the common classpath. The dialog keeps its own `ViewModelStore`, cleared on close,
with its state saved into the host's `SavedStateRegistry` so an activity recreation or process death
keeps the viewer's depth. The module's `StorageDevToolsScreen(onBack = …)` is the same viewer as a
navigation destination.

**Open Logs** reads the dev-only log store, which only exists after the host installs the module:

```kotlin
// Application.onCreate, before the first log worth keeping
installDevTools(context)   // Android; `installDevTools()` on JVM and iOS
```

`installDevTools` is the module's one install call: today it opens the log store, and a tool added
later is a line inside it rather than a second call in every app.

The store is a Room database (`dev_logs.db`) holding every `Logger` record. The reader and the store
are shared; only the database location and the container differ per platform. Newest first,
filterable by level and by tag or message, the full message on tap, share, and clear.
`isDevLogsAvailable` reports whether the store was installed, so an app that never calls
`installDevTools` simply has no Logs entry. `DevToolsDevLogsHost` is the container seam: a dialog
on Android, a full-screen overlay on JVM and iOS. See [Logging](logging.md) for the schema, the
batched writer and the retention rule.

### Dev tools in release

Every app module can declare which build types keep dev tools:

```kotlin
appBox {
    devBuildTypes = setOf("debug")     // "stage" too, when it exists
    devDependencies(projects.core.devtools)
}
```

The convention plugin publishes `BuildConfig.DEV_TOOLS` for the chosen types, adds the dependencies
to those variants only, and swaps Chucker for its no-op artifact everywhere else. An app keeps the
build-type-specific parts in `src/<buildType>/` — a real install call and screen in `debug`, no-ops
in `release` — and leaves its nav route declared once in the shared nav graph. The route then
renders nothing in a release build, which cannot reach it: the entry point stays behind
`BuildConfig.DEV_TOOLS`, and no devtools class is on the classpath at all. See
[Build System](../build-system.md#dev-tools-and-chucker).

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
