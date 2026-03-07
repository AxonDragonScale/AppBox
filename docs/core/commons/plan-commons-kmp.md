# Plan: Convert `:core:commons` to KMP

## Goal
Convert `:core:commons` into a full Kotlin Multiplatform module while preserving Android app compatibility and maximizing shared code via Compose Multiplatform.

---

## Source Set Breakdown

### `commonMain` — pure Kotlin, no platform APIs
| File | Notes |
|------|-------|
| `Result.kt` | Zero platform deps — move as-is |
| `Serialization.kt` | `kotlinx.serialization` only — move as-is |
| `DateTimeExt.kt` | `kotlinx.datetime` + `kotlin.time` only — move as-is |
| `FlowExt.kt` (partial) | `collect()` and `combine` helpers are pure KMP — move these |
| `Logger.kt` (partial) | `Logger` object + log level functions stay here; `android.util.Log` call extracted via `expect/actual` |

### `commonMain` — Compose Multiplatform compatible
All of these use only CMP-supported APIs (Material 3, Compose UI, Canvas, Modifier, ImageVector):

| File | Notes |
|------|-------|
| `compose/ModifierExt.kt` | `Modifier` + `draw.alpha` — fully CMP-compatible |
| `compose/StatCard.kt` | Material3 Surface/Text/Column only — CMP-compatible |
| `compose/Dropdown.kt` | Material3 + `LocalDensity` — CMP-compatible |
| `compose/chart/BarChart.kt` | Canvas-based drawing — CMP-compatible |
| `compose/chart/RingChart.kt` | Canvas-based drawing — CMP-compatible |
| `compose/chart/TrendLine.kt` | Canvas-based drawing — CMP-compatible |
| `devtools/DevTool.kt` | `@Composable`, `Color`, `ImageVector` — CMP-compatible |
| `devtools/DevToolsBuilder.kt` | Pure Kotlin DSL with Compose lambdas — CMP-compatible |
| `UIMode.kt` (partial) | The `UIMode` enum itself is pure Kotlin — move to `commonMain`; `isDarkTheme()` extension splits to `androidMain` |

### `androidMain` — Android-only or Android-specific Compose
| File | Notes |
|------|-------|
| `FlowExt.collectWithLifecycle` | `LocalLifecycleOwner` + `androidx.lifecycle` — Android-only |
| `Logger.kt` (actual) | `android.util.Log` implementation for the `expect/actual` |
| `UIMode.isDarkTheme()` | `isSystemInDarkTheme()` is Android-only in CMP |
| `compose/ComposeUtils.kt` | `WindowInsets.navigationBars/statusBars` — Android-only in CMP |
| `compose/DatePickerDialog.kt` | `rememberDatePickerState` not available in CMP — Android-only |
| `compose/TimePickerDialog.kt` | `rememberTimePickerState` not available in CMP — Android-only |
| `devtools/DevToolsScreen.kt` | Uses `LocalContext`, `Activity`, `rememberLauncherForActivityResult`, `PermissionState`, `clearAppData`, `getDangerousPermissions` — Android-only |
| `devtools/DevToolItems.kt` | Renders Android-specific DevTool types — Android-only |
| `ContextExt.kt` | `Context`, `ActivityManager`, `AlarmManager` etc. — Android-only |
| `PermissionExt.kt` | `PackageManager`, `ContextCompat` — Android-only |
| `PreferenceExt.kt` | `androidx.datastore` — Android-only |
| `room/` (all converters) | Room is Android-only |
| `qr/QRGenerator.kt` | `android.graphics.Bitmap`, ZXing — Android-only |
| `di/CommonsModule.kt` | Koin Android wiring (`koin-android`) — Android-only |
| `network/HttpClient.kt` | OkHttp engine + ChuckerInterceptor — Android-only |

---

## `expect/actual` Required

### `Logger`
- `expect` in `commonMain`: the `log(priority, tag, message)` dispatch function
- `actual` in `androidMain`: calls `android.util.Log.println`
- `actual` in other targets: calls `println`

### `HttpClient`
- `expect` in `commonMain`: `createHttpClient(json: Json): HttpClient` — installs `ContentNegotiation` and `HttpTimeout` with common config
- `actual` in `androidMain`: OkHttp engine + ChuckerInterceptor
- `actual` in other targets (jvm, ios, etc.): CIO engine (Ktor's pure-Kotlin engine)

---

## Build Configuration Changes

1. Replace the `library-convention` plugin with `kotlin.multiplatform` + `android.library` plugins directly, following `:libraries:tlv` as the structural reference.
2. Declare full KMP targets: `androidTarget`, `jvm`, `iosX64`, `iosArm64`, `iosSimulatorArm64`, `linuxX64` (or match the target set agreed for the project).
3. Scope dependencies correctly:
   - `commonMain`: `kotlinx-serialization-json`, `kotlinx-datetime`, `kotlinx-coroutines-core`, `compose-multiplatform` BOM, `koin-core`
   - `androidMain`: `androidx-core-ktx`, `androidx-appcompat`, `androidx-lifecycle`, `koin-android`, `koin-androidx-compose`, `ktor-client-okhttp`, `chucker`, `datastore-prefs`, `room-ktx`, `zxing`, `accompanist-permissions`
   - Other targets: `ktor-client-cio`
4. Consider creating a `kmp-library-convention` plugin in `build-logic` for reuse if more KMP modules are planned.

---

## Migration Steps

1. **Update `build.gradle.kts`** — switch to KMP structure, add all targets, re-scope all dependencies.
2. **Create source set directories** — `src/commonMain/kotlin`, `src/androidMain/kotlin`.
3. **Move `commonMain` files** — pure Kotlin + CMP-compatible Compose files (see table above).
4. **Add `expect/actual` for `Logger`** — common interface, Android + other actuals.
5. **Add `expect/actual` for `HttpClient`** — common signature, per-target engine implementations.
6. **Move `androidMain` files** — all remaining Android-bound files.
7. **Split `FlowExt.kt`** — pure `collect()` and `combine` helpers to `commonMain`, `collectWithLifecycle` to `androidMain`.
8. **Split `UIMode.kt`** — enum to `commonMain`, `isDarkTheme()` extension to `androidMain`.
9. **Fix all Android consumers** — verify all 6 app modules compile after the migration.
10. **Verify non-Android targets compile** — run `./gradlew :core:commons:compileKotlinJvm` (or equivalent) to confirm `commonMain` is clean.
11. **Update `docs/core/commons/context.md`** — document the new source-set structure, `expect/actual` contracts, and which APIs are common vs Android-only.