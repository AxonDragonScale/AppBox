> Context: [Main](../../context.md) > [Jest](./context.md) > KMP Migration Plan

# Jest — KMP Migration Plan

## Status: ✅ COMPLETE (pending: context.md update)

---

## Goal

Migrate Jest from having all code in `androidMain` to a proper KMP app where shared logic and UI live in `commonMain`, with platform-specific code isolated into `androidMain`, `iosMain`, and `jvmMain`. The widget stays Android-only.

---

## Current State

- **62 files** in `androidMain` — the entire app (models, database, network, repository, UI, widget, DI, theme)
- **3 files** in `commonMain` — placeholder `App.kt`, `Greeting.kt`, `Platform.kt` (unused KMP wizard scaffolding)
- **2 files** in `jvmMain` — placeholder `main.kt`, `Platform.jvm.kt`
- **2 files** in `iosMain` — placeholder `MainViewController.kt`, `Platform.ios.kt`
- Build config already has KMP targets (android, jvm, iosArm64, iosSimulatorArm64) and dependencies wired per source set
- Room, Ktor, kotlinx.serialization, Compose Multiplatform, Navigation Compose, Koin are already in `commonMain.dependencies`

---

## Scope

- **In scope**: Android, Desktop (JVM), iOS
- **Out of scope**: JS, WasmJS, Web (targets commented out already)
- **Android-only forever**: Widget (`widget/` package), `JestApplication`, `JestActivity`, Splash screen, Chucker, notification permissions, DevTools built-in tools (Android portion)

---

## Aligned Decisions

### D1: Sharing → `expect`/`actual`

Create `expect fun shareText(text: String)` in `commonMain`. Each platform provides its own `actual`:
- **Android**: `Intent.ACTION_SEND` via `Intent.createChooser`
- **Desktop**: Copy to clipboard (or `Desktop.browse()`)
- **iOS**: `UIActivityViewController`

The common `JokeCard` share button calls `shareText()`. `getShareIntent()` is removed from `Joke.kt` and replaced with `getShareableText()` (already exists as a private function — make it internal).

**File naming**: `ShareUtils.kt` in `commonMain`, `ShareUtils.android.kt` / `ShareUtils.jvm.kt` / `ShareUtils.ios.kt` for actuals.

### D2: Theme → Common static theme + Android wraps with dynamic colors

`JestTheme` in `commonMain` provides static light/dark `ColorScheme` and accepts `darkTheme: Boolean` + optional `colorScheme: ColorScheme?` override. It calls `MaterialTheme(colorScheme, typography, content)`.

`JestActivity` (Android) resolves dynamic colors via `dynamicDarkColorScheme`/`dynamicLightColorScheme` and system bar tinting, then passes the resolved `ColorScheme` to the common `JestTheme`.

Desktop/iOS entry points call `JestTheme(darkTheme = ...)` without dynamic color override.

### D3: Previews → Compose Multiplatform `@Preview` in `commonMain`

Use `org.jetbrains.compose.ui.tooling.preview.Preview` in `commonMain`. This is available via `compose-ui-tooling-preview` (already in the version catalog as a Compose Multiplatform dependency).

Remove `android.content.res.Configuration.UI_MODE_NIGHT_YES` previews. In `commonMain`, only a single `@Preview` annotation per composable (no dark mode variant via `uiMode` — that's Android-only). If needed, add explicit dark-mode preview composables that pass `darkTheme = true` to the theme.

### D4: App Metadata → `expect`/`actual` in `:core:commons`

Create an `expect`/`actual` for app metadata **in `:core:commons`** so all apps and DevTools can reuse it:

```kotlin
// commons/commonMain — AppInfo.kt
expect object AppInfo {
    val versionName: String
    val versionCode: Int
    val packageName: String
}
```

Platform actuals (in commons):
- **Android** (`AppInfo.android.kt`): Read from `PackageManager` via `init(context)` in `Application.onCreate()`
- **Desktop** (`AppInfo.jvm.kt`): `init(versionName, versionCode, packageName)` called before app launch
- **iOS** (`AppInfo.ios.kt`): Read from `NSBundle.mainBundle` — no init needed

### D5: DataStore → `expect`/`actual` for creation in `:core:commons`

Create the DataStore creation helper **in `:core:commons`** so all apps can reuse it:

```kotlin
// commons/commonMain — DataStoreFactory.kt
expect fun dataStorePreferencesPath(name: String): String

fun createPreferencesDataStore(name: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePreferencesPath(name).toPath() }
    )
```

Platform actuals (in commons):
- **Android** (`DataStoreFactory.android.kt`): Uses `context.filesDir` path via Koin `KoinComponent`
- **Desktop** (`DataStoreFactory.jvm.kt`): Uses `~/.local/share/jest/`
- **iOS** (`DataStoreFactory.ios.kt`): Uses `NSDocumentDirectory`

`PrefsRepository` in Jest calls `createPreferencesDataStore("jest_prefs")` — fully in `commonMain`.

### D6: GitHub Icon → Compose Multiplatform Resources

Move `ic_github.xml` to `commonMain/composeResources/drawable/` and use `org.jetbrains.compose.resources.painterResource(Res.drawable.ic_github)`.

Widget-specific drawables, app icons, splash theme XML, etc. stay in `androidMain/res/`.

### D7: `koinViewModel` → KMP variant

Switch all `org.koin.androidx.compose.koinViewModel` imports to `org.koin.compose.viewmodel.koinViewModel` (from `koin-compose-viewmodel`). This dependency is already in the version catalog and used by `:core:commons` in `commonMain`.

### D8: DevTools → Migrate to KMP in `:core:commons`

Migrate `DevToolsScreen` and related files to `commonMain` in `:core:commons`. The Android-only built-in tools (Kill App, Clear Data, Open App Settings, Permissions) are hidden on non-Android platforms.

**Approach for built-in tools:**
- `expect @Composable fun DevToolsBuiltInContent()` in commons `commonMain`
- **Android actual**: Renders Kill App, Clear Data, Open App Settings, Permissions sections
- **JVM/iOS actual**: Renders Kill App only (via `exitProcess`)

**Impact on Jest:**
- `JestDevTools.kt` moves to `commonMain` — uses common `DevToolsScreen` + `AppInfo`
- `Route.DevTools` and its composable destination in `JestApp.kt` work on all platforms

### Naming Convention for `expect`/`actual` Files

- `commonMain`: `MyFile.kt` (contains `expect` declarations)
- `androidMain`: `MyFile.android.kt` (contains `actual` implementations)
- `jvmMain`: `MyFile.jvm.kt`
- `iosMain`: `MyFile.ios.kt`

---

## Migration Plan — Phased

### ✅ Phase 5: Commons Changes (prerequisite)

**5a: DataStore factory (per D5):** ✅ DONE
- `commons/commonMain`: `DataStoreFactory.kt` — common `createPreferencesDataStore(name)` + `expect fun dataStorePreferencesPath(name: String): String`
- `commons/androidMain`: `DataStoreFactory.android.kt` — uses `context.filesDir` via `KoinComponent`
- `commons/jvmMain`: `DataStoreFactory.jvm.kt` — uses `~/.local/share/jest/`
- `commons/iosMain`: `DataStoreFactory.ios.kt` — uses `NSDocumentDirectory`

**5b: AppInfo (per D4):** ✅ DONE
- `commons/commonMain`: `AppInfo.kt` — `expect object AppInfo { ... }`
- `commons/androidMain`: `AppInfo.android.kt` — reads from `PackageManager`; `init(context)` called from `JestApplication`
- `commons/jvmMain`: `AppInfo.jvm.kt` — `init(versionName, versionCode, packageName)` at startup
- `commons/iosMain`: `AppInfo.ios.kt` — reads `NSBundle.mainBundle`

**5c: DevTools migration to KMP (per D8):** ✅ DONE
- `commons/commonMain/devtools/DevToolItems.kt` — moved from `androidMain`; `Toast`/`LocalContext` removed from `DevToolActionItem`
- `commons/commonMain/devtools/DevToolsScreen.kt` — screen shell + `expect @Composable fun DevToolsBuiltInContent()`
- `commons/androidMain/devtools/DevToolsScreen.android.kt` — `actual` renders Kill App, Clear Data, Open App Settings, Permissions
- `commons/jvmMain/devtools/DevToolsScreen.jvm.kt` — `actual` renders Kill App only (`exitProcess`)
- `commons/iosMain/devtools/DevToolsScreen.ios.kt` — `actual` renders Kill App only (`exitProcess`)
- Old `commons/androidMain/devtools/DevToolItems.kt` and `DevToolsScreen.kt` deleted

---

### ✅ Phase 1: Models & Enums → `commonMain`

All files moved to `commonMain`. Changes applied:
- `model/Category.kt` ✅
- `model/JokeType.kt` ✅
- `model/Language.kt` ✅
- `model/BlacklistFlag.kt` ✅
- `model/Flags.kt` ✅
- `model/serialization/JokeSerializer.kt` ✅
- `network/response/JokeApiResponse.kt` ✅
- `network/response/JokeListApiResponse.kt` ✅
- `model/Joke.kt` ✅ — `getShareIntent()` removed; `getShareableText()` made `internal`; `isOld()` uses `Clock.System.now()` instead of `System.currentTimeMillis()`

---

### ✅ Phase 2: Network Layer → `commonMain`

- `network/JokeApiClient.kt` ✅
- `network/mapper/JokeApiResponseMapper.kt` ✅ — `System.currentTimeMillis()` replaced with `Clock.System.now().toEpochMilliseconds()`

---

### ✅ Phase 3: Database Layer → `commonMain`

- `database/entity/JokeEntity.kt` ✅
- `database/dao/JokeDao.kt` ✅
- `database/converter/CategoryConverter.kt` ✅
- `database/converter/JokeTypeConverter.kt` ✅
- `database/converter/LanguageConverter.kt` ✅
- `database/mapper/JokeMapper.kt` ✅
- `database/JestDbClient.kt` ✅
- `database/JestDatabase.kt` ✅ — `@Database` class in `commonMain`; `create()` factory replaced with `expect fun createJestDatabase()`
  - `androidMain`: `JestDatabase.android.kt` — `Room.databaseBuilder(context)` via `KoinComponent`
  - `jvmMain`: `JestDatabase.jvm.kt` — `Room.databaseBuilder` with `~/.local/share/jest/` path + `BundledSQLiteDriver`
  - `iosMain`: `JestDatabase.ios.kt` — `Room.databaseBuilder` with `NSHomeDirectory()` path

---

### ✅ Phase 4: Repository Layer → `commonMain`

- `repository/JokeRepository.kt` ✅ — no changes needed
- `repository/PrefsRepository.kt` ✅ — `android.content.Context` dependency removed; uses `createPreferencesDataStore("jest_prefs")` from commons

---

### ✅ Phase 6: Sharing → `expect`/`actual` (in Jest)

- `jest/commonMain`: `ShareUtils.kt` ✅ — `expect fun shareText(text: String)`
- `jest/androidMain`: `ShareUtils.android.kt` ✅ — `Intent.ACTION_SEND` via `KoinComponent` context
- `jest/jvmMain`: `ShareUtils.jvm.kt` ✅ — clipboard copy via `Toolkit`
- `jest/iosMain`: `ShareUtils.ios.kt` ✅ — `UIActivityViewController`

---

### ✅ Phase 7: Theme → `commonMain` + `androidMain` (in Jest)

- `ui/theme/Color.kt` ✅ — moved to `commonMain`
- `ui/theme/Type.kt` ✅ — moved to `commonMain`
- `ui/theme/Theme.kt` ✅ — new common `JestTheme` in `commonMain`; accepts `darkTheme: Boolean` + optional `colorScheme: ColorScheme?` override; calls `MaterialTheme`
- Old `androidMain/ui/theme/Theme.kt` deleted ✅
- `JestActivity.kt` updated ✅ — resolves dynamic colors; applies system bar tinting; passes `colorScheme` to common `JestTheme`
- `JestApplication.kt` updated ✅ — calls `AppInfo.init(this)` before Koin setup

---

### ✅ Phase 8: UI Components → `commonMain` (in Jest)

- `ui/navigation/Route.kt` ✅
- `ui/bottombar/Tab.kt` ✅
- `ui/bottombar/BottomBar.kt` ✅ — `Configuration` import removed; CMP `@Preview`
- `ui/component/JestIconButton.kt` ✅ — CMP `@Preview`
- `ui/component/JestTag.kt` ✅ — CMP `@Preview`
- `ui/component/MultiSelector.kt` ✅ — CMP `@Preview`
- `ui/component/Shimmer.kt` ✅
- `ui/component/TypewriterEffect.kt` ✅
- `ui/component/TypewriterText.kt` ✅
- `ui/common/JokeCard.kt` ✅ — `LocalContext`/`getShareIntent()` replaced with `shareText(joke.getShareableText())`; CMP `@Preview`
- `ui/home/HomeUiState.kt` ✅
- `ui/home/HomeUiEvent.kt` ✅
- `ui/home/HomeVM.kt` ✅
- `ui/home/Home.kt` ✅ — `koinViewModel` import switched to `org.koin.compose.viewmodel.koinViewModel`; CMP `@Preview`
- `ui/list/JokeListType.kt` ✅
- `ui/list/JokeListUiState.kt` ✅
- `ui/list/JokeListUiEvent.kt` ✅
- `ui/list/JokeListVM.kt` ✅
- `ui/tune/PrefsUiState.kt` ✅
- `ui/tune/PrefsUiEvent.kt` ✅
- `ui/tune/PrefsVM.kt` ✅
- `ui/settings/SettingsUiState.kt` ✅
- `ui/settings/SettingsUiEvent.kt` ✅
- `ui/settings/SettingsVM.kt` ✅
- `ui/list/JokeList.kt` ✅ — `koinViewModel` import switched; CMP `@Preview`
- `ui/tune/PrefsBottomSheet.kt` ✅ — `koinViewModel` import switched; CMP `@Preview`
- `ui/settings/Settings.kt` ✅ — `BuildConfig` replaced with `AppInfo`; `painterResource(R.drawable.ic_github)` replaced with CMP `painterResource(Res.drawable.ic_github)`; `koinViewModel` switched; CMP `@Preview`
- `ui/MainScreen.kt` ✅ — moved as-is
- `ui/JestApp.kt` ✅ — moved as-is
- `ui/devtools/JestDevTools.kt` ✅ — `BuildConfig` replaced with `AppInfo`; moved to `commonMain`

---

### ✅ Phase 9: Resources (in Jest)

- `ic_github.xml` ✅ — moved from `androidMain/res/drawable/` to `commonMain/composeResources/drawable/`
- All other drawables stay in `androidMain/res/` (widget icons, app icons, splash theme) ✅

---

### ✅ Phase 10: DI Module → `commonMain` + platform modules

- `di/JestModule.kt` ✅ — moved to `commonMain`; `JestDatabase.create(get())` replaced with `createJestDatabase()`; `PrefsRepository(get())` replaced with `PrefsRepository()` (no-arg); `koin-compose-viewmodel` + `koin-core` + `koin-compose` added explicitly to `commonMain.dependencies` in `build.gradle.kts`
- No platform-specific `JestPlatformModule` needed ✅

---

### ✅ Phase 11: Platform Entry Points

**Android (`androidMain`):**
- `JestApplication` ✅ — already calls `AppInfo.init(this)`; already starts Koin with `CommonsModule + JestModule`
- `JestActivity` ✅ — already updated for common `JestTheme`

**Desktop (`jvmMain`):**
- `main.kt` ✅ — replaced placeholder `App()` with `AppInfo.init(...)` + `startKoin { CommonsModule + JestModule }` + `JestTheme { JestApp() }`

**iOS (`iosMain`):**
- `MainViewController.kt` ✅ — replaced placeholder `App()` with `JestTheme { JestApp() }`; `initKoin()` top-level function added for call from Swift entry point

---

### ✅ Phase 12: Widget (Android-only — no changes)

These files stay in `androidMain` untouched:
- `widget/JestAppWidget.kt` ✅
- `widget/JestWidget.kt` ✅
- `widget/JestWidgetActions.kt` ✅
- `widget/JestWidgetReceiver.kt` ✅

---

### ✅ Phase 13: Cleanup

- Deleted placeholder `commonMain` files: `App.kt`, `Greeting.kt`, `Platform.kt` ✅
- Deleted platform actuals for placeholders: `Platform.android.kt`, `Platform.jvm.kt`, `Platform.ios.kt` ✅
- Deleted all superseded `androidMain` source files (model, network, database, repository, UI, DI) ✅
- Deleted empty `jsMain`, `wasmJsMain`, `webMain` source set directories ✅
- Verified zero `android.` imports in `commonMain` ✅
- `build.gradle.kts` updated: `koin-compose-viewmodel`, `koin-core`, `koin-compose` added to `commonMain.dependencies` ✅
- `docs/apps/jest/context.md` — pending update ⬜

---

## Expected Final Structure

```
# :core:commons (prerequisite changes)
commons/src/
├── commonMain/kotlin/com/axondragonscale/commons/
│   ├── AppInfo.kt                           # expect object AppInfo ✅
│   ├── DataStoreFactory.kt                  # expect + common createPreferencesDataStore() ✅
│   ├── devtools/
│   │   ├── DevTool.kt                       # already here ✓
│   │   ├── DevToolsBuilder.kt               # already here ✓
│   │   ├── DevToolItems.kt                  # MOVED from androidMain (Toast removed) ✅
│   │   └── DevToolsScreen.kt                # MOVED: screen shell + expect built-in content ✅
│   └── ...                                  # existing files unchanged
├── androidMain/kotlin/com/axondragonscale/commons/
│   ├── AppInfo.android.kt                   # actual: PackageManager + init(context) ✅
│   ├── DataStoreFactory.android.kt          # actual: context.filesDir path ✅
│   ├── devtools/
│   │   └── DevToolsScreen.android.kt        # actual: built-in tools (Kill, Clear, Permissions) ✅
│   └── ...                                  # existing files unchanged
├── jvmMain/kotlin/com/axondragonscale/commons/
│   ├── AppInfo.jvm.kt                       # actual: init at startup ✅
│   ├── DataStoreFactory.jvm.kt              # actual: app data dir path ✅
│   └── devtools/
│       └── DevToolsScreen.jvm.kt            # actual: Kill App only ✅
└── iosMain/kotlin/com/axondragonscale/commons/
    ├── AppInfo.ios.kt                       # actual: NSBundle ✅
    ├── DataStoreFactory.ios.kt              # actual: NSDocumentDirectory ✅
    └── devtools/
        └── DevToolsScreen.ios.kt            # actual: Kill App only ✅

# :apps:jest
jestComposeApp/src/
├── commonMain/
│   ├── kotlin/com/axondragonscale/jest/
│   │   ├── ShareUtils.kt                   # expect fun shareText(text: String) ✅
│   │   ├── model/                           # All models, enums, serializers ✅
│   │   │   ├── Category.kt ✅
│   │   │   ├── JokeType.kt ✅
│   │   │   ├── Language.kt ✅
│   │   │   ├── BlacklistFlag.kt ✅
│   │   │   ├── Flags.kt ✅
│   │   │   ├── Joke.kt ✅
│   │   │   └── serialization/JokeSerializer.kt ✅
│   │   ├── network/                         # Ktor client, responses, mappers ✅
│   │   │   ├── JokeApiClient.kt ✅
│   │   │   ├── mapper/JokeApiResponseMapper.kt ✅
│   │   │   └── response/
│   │   │       ├── JokeApiResponse.kt ✅
│   │   │       └── JokeListApiResponse.kt ✅
│   │   ├── database/                        # Room entities, DAO, converters, Database ✅
│   │   │   ├── JestDatabase.kt ✅           # @Database class + expect fun createJestDatabase()
│   │   │   ├── JestDbClient.kt ✅
│   │   │   ├── converter/ ✅
│   │   │   ├── dao/JokeDao.kt ✅
│   │   │   ├── entity/JokeEntity.kt ✅
│   │   │   └── mapper/JokeMapper.kt ✅
│   │   ├── repository/
│   │   │   ├── JokeRepository.kt ✅
│   │   │   └── PrefsRepository.kt ✅        # Uses commons createPreferencesDataStore()
│   │   ├── di/
│   │   │   └── JestModule.kt               # ⬜ Move to commonMain
│   │   └── ui/
│   │       ├── JestApp.kt                  # ⬜
│   │       ├── MainScreen.kt               # ⬜
│   │       ├── navigation/Route.kt ✅
│   │       ├── bottombar/
│   │       │   ├── Tab.kt ✅
│   │       │   └── BottomBar.kt ✅
│   │       ├── common/JokeCard.kt ✅
│   │       ├── component/
│   │       │   ├── JestIconButton.kt ✅
│   │       │   ├── JestTag.kt ✅
│   │       │   ├── MultiSelector.kt ✅
│   │       │   ├── Shimmer.kt ✅
│   │       │   ├── TypewriterEffect.kt ✅
│   │       │   └── TypewriterText.kt ✅
│   │       ├── home/
│   │       │   ├── Home.kt ✅
│   │       │   ├── HomeVM.kt ✅
│   │       │   ├── HomeUiState.kt ✅
│   │       │   └── HomeUiEvent.kt ✅
│   │       ├── list/
│   │       │   ├── JokeList.kt             # ⬜
│   │       │   ├── JokeListVM.kt ✅
│   │       │   ├── JokeListType.kt ✅
│   │       │   ├── JokeListUiState.kt ✅
│   │       │   └── JokeListUiEvent.kt ✅
│   │       ├── settings/
│   │       │   ├── Settings.kt             # ⬜
│   │       │   ├── SettingsVM.kt ✅
│   │       │   ├── SettingsUiState.kt ✅
│   │       │   └── SettingsUiEvent.kt ✅
│   │       ├── tune/
│   │       │   ├── PrefsBottomSheet.kt     # ⬜
│   │       │   ├── PrefsVM.kt ✅
│   │       │   ├── PrefsUiState.kt ✅
│   │       │   └── PrefsUiEvent.kt ✅
│   │       ├── devtools/
│   │       │   └── JestDevTools.kt         # ⬜
│   │       └── theme/
│   │           ├── Color.kt ✅
│   │           ├── Type.kt ✅
│   │           └── Theme.kt ✅             # Common JestTheme
│   └── composeResources/
│       └── drawable/
│           └── ic_github.xml              # ⬜ Move from androidMain/res/drawable/
│
├── androidMain/
│   ├── AndroidManifest.xml
│   ├── kotlin/com/axondragonscale/jest/
│   │   ├── JestApplication.kt ✅           # AppInfo.init(this) added
│   │   ├── JestActivity.kt ✅              # Dynamic colors + system bars; passes colorScheme
│   │   ├── ShareUtils.android.kt ✅        # actual: Intent sharing
│   │   ├── database/
│   │   │   └── JestDatabase.android.kt ✅  # actual: Room.databaseBuilder with Context
│   │   └── widget/                          # Glance widget (unchanged) ✅
│   │       ├── JestAppWidget.kt
│   │       ├── JestWidget.kt
│   │       ├── JestWidgetActions.kt
│   │       └── JestWidgetReceiver.kt
│   └── res/                                 # Icons, widget XML, splash theme, etc.
│       └── drawable/
│           └── ic_github.xml               # ⬜ To be moved to commonMain/composeResources/
│
├── jvmMain/kotlin/com/axondragonscale/jest/
│   ├── main.kt                              # ⬜ Replace placeholder with Koin init + JestApp
│   ├── ShareUtils.jvm.kt ✅
│   └── database/
│       └── JestDatabase.jvm.kt ✅
│
└── iosMain/kotlin/com/axondragonscale/jest/
    ├── MainViewController.kt               # ⬜ Replace placeholder with Koin init + JestApp
    ├── ShareUtils.ios.kt ✅
    └── database/
        └── JestDatabase.ios.kt ✅
```

---

## `expect`/`actual` Summary

### In `:core:commons` (reusable by all apps)

| Contract | `commonMain` | `androidMain` | `jvmMain` | `iosMain` |
|---|---|---|---|---|
| App metadata | `expect object AppInfo` ✅ | `PackageManager` + `init(context)` ✅ | `init(...)` at startup ✅ | `NSBundle` ✅ |
| DataStore path | `expect fun dataStorePreferencesPath(name): String` ✅ | `context.filesDir` ✅ | `~/.local/share/jest/` ✅ | `NSDocumentDirectory` ✅ |
| DevTools built-ins | `expect @Composable fun DevToolsBuiltInContent()` ✅ | Kill, Clear Data, App Settings, Permissions ✅ | Kill App only ✅ | Kill App only ✅ |

### In `:apps:jest` (app-specific)

| Contract | `commonMain` | `androidMain` | `jvmMain` | `iosMain` |
|---|---|---|---|---|
| Sharing | `expect fun shareText(text: String)` ✅ | Intent chooser ✅ | Clipboard copy ✅ | UIActivityViewController ✅ |
| Room DB creation | `expect fun createJestDatabase(): JestDatabase` ✅ | `Room.databaseBuilder(context)` ✅ | `Room.databaseBuilder(path)` ✅ | `Room.databaseBuilder(NSHomeDirectory)` ✅ |

---

## Build Configuration Changes

The `build.gradle.kts` dependencies are mostly already correct. Potential changes:
- Move `koin-compose-viewmodel` to `commonMain.dependencies` if not already there (it's available via `:core:commons` dependency, but may need explicit declaration)
- Ensure `compose-ui-tooling-preview` is in `commonMain.dependencies` for multiplatform `@Preview`
- The `koin.bundles` in `androidMain` uses the Android-specific bundle — may need adjustment to avoid duplicate Koin modules

---

## Risks & Notes

1. **`:core:commons` KMP readiness** — `CommonsModule` uses `androidContext` / `androidLogger` in Koin setup. Need to verify non-Android targets can use commons, or if commons needs a platform-agnostic init path. Phase 5 (commons changes) should address this. Potential blocker.
2. **Room KMP maturity** — Room for KMP is relatively new. Schema export and migration story may differ slightly. Current setup already has KSP configured for all targets.
3. **`collectAsStateWithLifecycle`** — Available via `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` (already in `commonMain.dependencies`). Should work cross-platform.
4. **`collectWithLifecycle`** — Used in the screen pattern from coding conventions (from `:core:commons` `FlowExt`). Need to verify it's in `commonMain` of commons.
5. **DevTools `Toast` replacement** — `DevToolActionItem` `completionAlert` Toast dropped (no cross-platform equivalent). The built-in Android tools manage their own feedback directly.
6. **`isSystemInDarkTheme()`** — Available in Compose Multiplatform `material3`. Works cross-platform.
7. **Stale `androidMain` files** — The original `androidMain` files for all moved layers (model, network, database, repository, UI) still exist alongside the new `commonMain` counterparts. They will cause duplicate class errors at compile time. Phase 13 (Cleanup) must delete them before the build is green.
8. **AppInfo initialization** — The `init(context)` pattern means apps must call it early (in `Application.onCreate()`). `JestApplication` already does this.
9. **Other apps** — Changes to `:core:commons` (DataStore factory, AppInfo, DevTools) benefit all apps in the monorepo. However, existing apps that use the Android-only `DevToolsScreen` will need import updates. These should be mechanical.

---

## Related Context

- [Jest Context](./context.md) — Current app architecture
- [Main Context](../../context.md) — Project overview
- [Build System](../../build-system.md) — Convention plugins, KMP library setup
- [Commons Context](../../core/commons/context.md) — Shared utilities
- [Coding Conventions](../../coding-conventions.md) — Code patterns