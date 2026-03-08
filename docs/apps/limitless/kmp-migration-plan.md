> Context: [Main](../../context.md) > [Limitless](./context.md) > KMP Migration Plan

# Limitless — KMP Migration Plan

## Current State

Limitless has been structurally converted to a KMP module (targets: `androidTarget`, `jvm`, `iosArm64`, `iosSimulatorArm64`) but all actual code (~118 files) lives in `androidMain`. The `commonMain`, `jvmMain`, and `iosMain` source sets contain only the default KMP template stubs (`Platform.kt`, `Greeting.kt`, `App.kt`, `main.kt`, `MainViewController.kt`).

### File Inventory

| Source Set | Files | Content |
|---|---|---|
| `commonMain` | 3 | Template stubs (`App.kt`, `Greeting.kt`, `Platform.kt`) |
| `androidMain` | 118 | **All** business logic, UI, DB, DI, reminders, etc. |
| `jvmMain` | 2 | Template stubs (`main.kt`, `Platform.jvm.kt`) |
| `iosMain` | 2 | Template stubs (`MainViewController.kt`, `Platform.ios.kt`) |

### Reference Implementation

Jest has already been fully migrated to KMP. This plan follows the same patterns established there.

---

## Migration Strategy

**Approach**: Move files from `androidMain` → `commonMain`, fixing Android-specific imports as we go. Work bottom-up (models → database → repositories → DI → VMs → UI → theme → entry points). Each phase should compile before moving to the next.

---

## Phase 1 — Models & Constants (Pure Kotlin, no dependencies)

**Move to `commonMain`** — these files have zero Android imports:

| File | Notes |
|---|---|
| `model/Habit.kt` | Domain model, pure data class |
| `model/HabitColor.kt` | Enum |
| `model/HabitView.kt` | Enum |
| `model/HabitHeatMapData.kt` | Data class |
| `model/HabitMaxCount.kt` | Data class |
| `model/UIAlign.kt` | Enum |
| `model/GoalType.kt` | Enum |
| `model/GoalStatus.kt` | Enum |
| `model/ReminderType.kt` | Enum |
| `model/BitWeek.kt` | Utility class |
| `LimitlessConstants.kt` (rename to `Link.kt` if desired) | String constants |
| `ui/Route.kt` | `@Serializable` route objects — pure kotlinx.serialization |
| `ui/model/MonthCount.kt` | UI model, pure data class |
| `ui/model/MonthlyTrendData.kt` | UI model, pure data class |
| `ui/model/WeeklyPatternData.kt` | UI model, pure data class |
| `ui/habit/stats/model/*.kt` (5 files) | `DailyHistoryData`, `GoalCompletionData`, `StreakData`, `SummaryData`, `TimeOfDayData` |
| `ui/stats/model/*.kt` (4 files) | `GlobalHeatMapData`, `HabitConsistencyData`, `TodayProgressData`, `WeekComparisonData` |

**Action**: Straight move, no code changes needed.

---

## Phase 2 — Database Layer

### 2a — Entities, Views, Converters, DAOs → `commonMain`

All Room annotations (`@Entity`, `@Dao`, `@DatabaseView`, `@TypeConverter`) work in KMP Room. Move:

| File | Notes |
|---|---|
| `database/entity/HabitEntity.kt` | Room entity |
| `database/entity/RecordEntity.kt` | Room entity |
| `database/entity/ReminderEntity.kt` | Room entity — uses `kotlinx.datetime.LocalDate`, `LocalTime` (already KMP) |
| `database/entity/GoalEntity.kt` | Room entity |
| `database/view/HabitDayCountView.kt` | `@DatabaseView` |
| `database/view/HabitHourCountView.kt` | `@DatabaseView` |
| `database/converter/GoalTypeConverter.kt` | Pure enum converter |
| `database/converter/HabitColorConverter.kt` | Pure enum converter |
| `database/converter/HabitViewConverter.kt` | Pure enum converter |
| `database/converter/ReminderTypeConverter.kt` | Pure enum converter |
| `database/dao/HabitDao.kt` | Room DAO interface |
| `database/dao/RecordDao.kt` | Room DAO interface |
| `database/dao/HabitDayCountDao.kt` | Room DAO interface |
| `database/dao/HabitHourCountDao.kt` | Room DAO interface |
| `database/dao/ReminderDao.kt` | Room DAO interface |
| `database/dao/GoalDao.kt` | Room DAO interface |

**Action**: Straight move. No code changes unless any DAO uses Android-specific types (unlikely — they use `kotlinx.datetime` already). Verify the commons `LocalDateConverter` and `LocalTimeConverter` are available in `commonMain` of commons (they are — confirmed in commons context).

### 2b — `LimitlessDatabase.kt` → Rewrite for KMP

The current `LimitlessDatabase` uses `android.content.Context` and `Room.databaseBuilder(context, ...)`.

**Rewrite to match Jest's pattern:**

1. Move `LimitlessDatabase` to `commonMain`
2. Remove the `create(context: Context)` companion function
3. Add `@ConstructedBy(LimitlessDatabaseConstructor::class)` annotation
4. Add the KSP-generated constructor expect:

```kotlin
// commonMain — LimitlessDatabase.kt
@Database(
    entities = [...],
    views = [...],
    version = LimitlessDatabase.DB_VERSION,
    exportSchema = true,
)
@TypeConverters(...)
@ConstructedBy(LimitlessDatabaseConstructor::class)
abstract class LimitlessDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "limitless"
        const val DB_VERSION = 3
    }
    abstract fun habitDao(): HabitDao
    abstract fun recordDao(): RecordDao
    abstract fun habitDayCountDao(): HabitDayCountDao
    abstract fun habitHourCountDao(): HabitHourCountDao
    abstract fun reminderDao(): ReminderDao
    abstract fun goalDao(): GoalDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object LimitlessDatabaseConstructor : RoomDatabaseConstructor<LimitlessDatabase> {
    override fun initialize(): LimitlessDatabase
}
```

5. Use commons' `createDatabase<LimitlessDatabase>(LimitlessDatabase.DB_NAME)` in the Koin module (no platform-specific DB creation code needed — commons already provides `actual` implementations for all three platforms).

**Migration note**: The current DB uses `fallbackToDestructiveMigration(dropAllTables = true)`. The commons `createDatabase` helper also uses destructive migration. If this needs to be preserved or customized, the commons helper may need a parameter. **Decision needed: is the current commons `createDatabase` helper sufficient, or does Limitless need custom migration options?**

---

## Phase 3 — Repository Layer

### 3a — `LimitlessRepository.kt` → `commonMain`

**Action**: Straight move. This file only imports:
- Room DAOs (will be in `commonMain` after Phase 2)
- Room entities and views (same)
- `kotlinx.datetime` types (already KMP)
- `kotlinx.coroutines.flow` (already KMP)
- `androidx.compose.ui.util.fastMaxOfOrDefault` — this import needs verification

**Potential issue**: `fastMaxOfOrDefault` is from `androidx.compose.ui.util`. This is available in Compose Multiplatform, so it should work. Verify during implementation.

### 3b — `AppPrefsRepository.kt` → `commonMain`

Current implementation uses Android `Context` + `preferencesDataStore` delegate:

```kotlin
private val Context.appPrefs by preferencesDataStore(APP_PREFS_STORE)

class AppPrefsRepository(private val context: Context) {
    val uiMode = context.appPrefs.enumPref(...)
}
```

**Rewrite to match Jest's pattern** — use commons' `createPreferencesDataStore`:

```kotlin
// commonMain — AppPrefsRepository.kt
class AppPrefsRepository {
    private val dataStore = createPreferencesDataStore("app_prefs_store")

    val uiMode = dataStore.enumPref(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )
    val dynamicTheme = dataStore.pref(
        key = booleanPreferencesKey("dynamicTheme"),
        default = false,
    )
    val uiAlign = dataStore.enumPref(
        key = stringPreferencesKey("uiAlign"),
        default = UIAlign.DEFAULT,
    )
    val highlightCurrentDay = dataStore.pref(
        key = booleanPreferencesKey("highlightCurrentDay"),
        default = true,
    )
}
```

**Change**: Remove `Context` constructor parameter. The commons `createPreferencesDataStore` is platform-aware and doesn't need a context at call-site.

---

## Phase 4 — Reminders System (`expect`/`actual`)

The `core/` package contains four Android-specific classes:
- `ReminderScheduler` — uses `AlarmManager`, `PendingIntent`, `Context`
- `ReminderNotificationManager` — uses `NotificationCompat`, `NotificationManagerCompat`, `PendingIntent`
- `ReminderReceiver` — `BroadcastReceiver`
- `BootReceiver` — `BroadcastReceiver`

`ReminderReceiver`, `BootReceiver`, and `ReminderNotificationManager` stay in `androidMain` — they are inherently Android-only (BroadcastReceivers, system notifications).

`ReminderScheduler` becomes an `expect`/`actual` class so that `EditorVM` and other common code can depend on it directly.

### `expect`/`actual` ReminderScheduler

**Decision: `expect`/`actual` class. iOS and Desktop get no-op actuals for now.**

```kotlin
// commonMain — core/ReminderScheduler.kt
expect class ReminderScheduler {
    fun schedule(reminder: ReminderEntity)
    fun cancel(reminderId: Long, habitId: Long)
    suspend fun rescheduleAll()
}

// androidMain — core/ReminderScheduler.android.kt
actual class ReminderScheduler(
    private val context: Context,
    private val repository: LimitlessRepository,
) {
    // Full AlarmManager implementation (existing code)
    actual fun schedule(reminder: ReminderEntity) { ... }
    actual fun cancel(reminderId: Long, habitId: Long) { ... }
    actual suspend fun rescheduleAll() { ... }
}

// jvmMain — core/ReminderScheduler.jvm.kt
actual class ReminderScheduler {
    actual fun schedule(reminder: ReminderEntity) {}  // no-op
    actual fun cancel(reminderId: Long, habitId: Long) {}  // no-op
    actual suspend fun rescheduleAll() {}  // no-op
}

// iosMain — core/ReminderScheduler.ios.kt
actual class ReminderScheduler {
    actual fun schedule(reminder: ReminderEntity) {}  // no-op
    actual fun cancel(reminderId: Long, habitId: Long) {}  // no-op
    actual suspend fun rescheduleAll() {}  // no-op
}
```

- `EditorVM` depends on `ReminderScheduler` directly — no interface, no null-checking
- The existing `ReminderScheduler` code moves to `androidMain` as the `actual` implementation
- Desktop/iOS actuals are empty stubs to be implemented later
- Internal helper methods (e.g. `computeNextTriggerMillis`, `createPendingIntent`) stay private inside the Android actual — they're not part of the expect contract

---

## Phase 5 — DI Module

### 5a — Split `LimitlessModule` into common + Android

**`commonMain` — `LimitlessModule.kt`:**

```kotlin
val LimitlessModule = module {
    // Database (uses commons createDatabase)
    single { createDatabase<LimitlessDatabase>(LimitlessDatabase.DB_NAME) }
    single { get<LimitlessDatabase>().habitDao() }
    single { get<LimitlessDatabase>().recordDao() }
    single { get<LimitlessDatabase>().habitDayCountDao() }
    single { get<LimitlessDatabase>().habitHourCountDao() }
    single { get<LimitlessDatabase>().reminderDao() }
    single { get<LimitlessDatabase>().goalDao() }

    // Repository
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)

    // Dev Tools
    singleOf(::SampleDataSeeder)

    // ViewModels
    viewModelOf(::HomeVM)
    viewModelOf(::EditorVM)
    viewModelOf(::HabitVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::HabitCalendarVM)
    viewModelOf(::HabitDayVM)
    viewModelOf(::HabitStatsVM)
    viewModelOf(::StatsVM)
    viewModelOf(::SettingsVM)
}
```

The common module includes `ReminderScheduler` directly (it's an `expect` class, resolved per-platform):
```kotlin
singleOf(::ReminderScheduler)
```

**`androidMain` — `LimitlessAndroidModule.kt`:**

Android-only components that aren't part of the expect/actual contract:
```kotlin
val LimitlessAndroidModule = module {
    singleOf(::ReminderNotificationManager)
}
```

### 5b — Platform Koin initialization

Follow Jest's `initKoin` pattern:

```kotlin
// commonMain
fun initKoin(platformConfig: KoinApplication.() -> Unit = {}) {
    startKoin {
        platformConfig()
        modules(CommonsModule, LimitlessModule)
    }
}
```

- **Android**: `initKoin { androidContext(this@App); androidLogger(); modules(LimitlessAndroidModule) }`
- **Desktop**: `initKoin()`
- **iOS**: `initKoin()`

---

## Phase 6 — ViewModels

**Move to `commonMain`** — all VMs:

| File | Android-specific issues |
|---|---|
| `ui/home/HomeVM.kt` | None — clean |
| `ui/home/HomeUiState.kt` | None |
| `ui/home/HomeUiEvent.kt` | None |
| `ui/editor/EditorVM.kt` | `java.util.concurrent.atomic.AtomicLong` → use `kotlinx.atomicfu.atomic(Long)` or plain `Long` with a `var` (VMs are single-threaded). `ReminderScheduler` → replace with `ReminderScheduling` interface |
| `ui/editor/EditorUiState.kt` | None |
| `ui/editor/EditorUiEvent.kt` | None |
| `ui/editor/EditorUiEffect.kt` | None |
| `ui/habit/HabitVM.kt` | Check for Android imports |
| `ui/habit/HabitUiState.kt` | None |
| `ui/habit/HabitUiEvent.kt` | None |
| `ui/habit/calendar/HabitCalendarVM.kt` | Check |
| `ui/habit/calendar/HabitCalendarUiState.kt` | None |
| `ui/habit/calendar/HabitCalendarUiEvent.kt` | None |
| `ui/habit/history/HistoryVM.kt` | Check |
| `ui/habit/history/HistoryUiState.kt` | None |
| `ui/habit/history/HistoryUiEvent.kt` | None |
| `ui/habit/stats/HabitStatsVM.kt` | Check |
| `ui/habit/stats/HabitStatsUiState.kt` | None |
| `ui/habit/stats/HabitStatsUiEvent.kt` | None |
| `ui/day/HabitDayVM.kt` | Check |
| `ui/day/HabitDayUiState.kt` | None |
| `ui/day/HabitDayUiEvent.kt` | None |
| `ui/stats/StatsVM.kt` | Check |
| `ui/stats/StatsUiState.kt` | None |
| `ui/stats/StatsUiEvent.kt` | None |
| `ui/settings/SettingsVM.kt` | Check |
| `ui/settings/SettingsUiState.kt` | None |
| `ui/settings/SettingsUiEvent.kt` | None |

### Specific fixes needed in VMs:

1. **`EditorVM.kt`**: Replace `java.util.concurrent.atomic.AtomicLong` with a plain `var` counter. The counter is only accessed from `addReminder()`/`addGoal()` which are called from `onEvent()` — not concurrent, so a plain `var` with `--` is sufficient. `ReminderScheduler` dependency stays as-is (it's now an `expect` class available in `commonMain`).

2. **All VMs**: `collectAsStateWithLifecycle` is actually used in the composable layer, not in VMs. VMs themselves are clean — they only use `MutableStateFlow`, `Channel`, `viewModelScope`, `Flow` which are all KMP-compatible via `androidx.lifecycle` multiplatform.

---

## Phase 7 — UI Composables

This is the bulk of the work (~40+ composable files). Most of them have only two categories of Android-specific imports:

### 7a — Preview annotations

**Current**: Uses `androidx.compose.ui.tooling.preview.Preview` with `uiMode = Configuration.UI_MODE_NIGHT_YES` (Android-only `Configuration` class).

**Fix**: Keep both light and dark mode previews. Replace `android.content.res.Configuration` import with CMP's `AndroidUiModes`:
- `import android.content.res.Configuration` → `import androidx.compose.ui.tooling.preview.AndroidUiModes`
- `import androidx.compose.ui.tooling.preview.Preview` stays (same import in CMP)
- `uiMode = Configuration.UI_MODE_NIGHT_YES` → `uiMode = AndroidUiModes.UI_MODE_NIGHT_YES`

This affects ~25+ files. Mechanical find-and-replace:

```
// Before
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)

// After
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
```

Remove `name = "..."` parameters (redundant with the uiMode-based differentiation). Keep `showBackground = true`.

### 7b — `RemindersSection.kt` — Notification permission

This file uses:
- `android.Manifest.permission.POST_NOTIFICATIONS`
- `android.os.Build`
- `androidx.activity.compose.rememberLauncherForActivityResult`
- `androidx.activity.result.contract.ActivityResultContracts`

**Decision: `expect`/`actual` composable for the permission effect.**

Extract a small `expect`/`actual` composable for just the permission request. The rest of `RemindersSection.kt` (reminder configuration UI — time picker, type selector, day-of-week picker) is pure Compose and moves to `commonMain`.

```kotlin
// commonMain
@Composable
expect fun RequestNotificationPermissionEffect(onResult: (Boolean) -> Unit)

// androidMain
@Composable
actual fun RequestNotificationPermissionEffect(onResult: (Boolean) -> Unit) {
    // rememberLauncherForActivityResult + POST_NOTIFICATIONS logic
}

// jvmMain / iosMain
@Composable
actual fun RequestNotificationPermissionEffect(onResult: (Boolean) -> Unit) {
    // No-op — notifications not supported on desktop/iOS yet
}
```

### 7c — `BuildConfig` usage in `LimitlessDevTools.kt`

Current code references `BuildConfig.VERSION_NAME`, `BuildConfig.VERSION_CODE`, `BuildConfig.APPLICATION_ID`.

**Fix**: Replace with `AppInfo` from commons (matching Jest's pattern):
- `AppInfo.versionName`, `AppInfo.versionCode`, `AppInfo.packageName`

This requires `AppInfo.init(...)` to be called at each platform's entry point (already the pattern in Jest).

### 7d — `SampleDataSeeder.kt`

Check if this has Android-specific imports. From the first 1KB it appears to be pure Kotlin + Room DAOs + kotlinx.datetime. If confirmed, move to `commonMain` directly.

### 7e — All other composables

The remaining composables use only:
- Compose Multiplatform APIs (`foundation`, `material3`, `ui`)
- `collectAsStateWithLifecycle` — available in KMP `androidx.lifecycle.runtime.compose`
- `koinViewModel()` — from `org.koin.compose.viewmodel` (KMP-compatible)
- `NavController` — from `navigation.compose` (KMP-compatible)
- Room entities/models (will be in `commonMain`)

**Action**: Move to `commonMain`. The only mechanical change is the `@Preview` import fix described in 7a.

---

## Phase 8 — Theme

### 8a — `Color.kt` → `commonMain`

Pure Compose `Color` definitions + `HabitColor.color` extension. No Android imports. Straight move.

### 8b — `HabitTheme.kt` → `commonMain`

Uses `MaterialTheme`, `rememberDynamicColorScheme` from MaterialKolor. No Android imports. Straight move.

### 8c — `Type.kt` — Font loading

**Current**: Uses Android `Font(R.font.kodchasan_*, ...)` resource loading.

**Fix**:
1. Move Kodchasan `.ttf` files from `androidMain/res/font/` → `commonMain/composeResources/font/`
2. Rewrite `Type.kt` to use Compose Multiplatform resource loading:

```kotlin
// commonMain — Type.kt
@Composable
fun kodchasanFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.kodchasan_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.kodchasan_bold, FontWeight.Bold, FontStyle.Normal),
        // ... etc
    )
}
```

Note: CMP `Font()` from resources is `@Composable` (unlike Android's non-composable `Font()`). The `Typography` object may need to be created inside a `@Composable` scope. Jest doesn't use custom fonts, so check how other CMP projects handle this. A `@Composable` function that returns `Typography` is the standard CMP pattern.

### 8d — `Theme.kt` (`LimitlessTheme`) — Platform split

**Current**: Uses `dynamicDarkColorScheme`/`dynamicLightColorScheme` (Android 12+ Material You), `LocalView`, `WindowCompat`, `Build.VERSION.SDK_INT`.

**Fix** — follow Jest's pattern:

**`commonMain`**: Create a `LimitlessTheme` that accepts an optional `colorScheme` override:

```kotlin
@Composable
fun LimitlessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme? = null,  // Android passes Material You colors
    content: @Composable () -> Unit,
) {
    val resolvedScheme = colorScheme
        ?: rememberDynamicColorScheme(Color.Black, darkTheme, style = PaletteStyle.Monochrome)

    MaterialTheme(
        colorScheme = resolvedScheme,
        typography = kodchasanTypography(),  // composable
        content = content,
    )
}
```

**`androidMain`**: `LimitlessActivity` resolves dynamic colors and passes them as the `colorScheme` override (same as Jest's `JestActivity`). System bar configuration stays in `LimitlessActivity`.

**Desktop / iOS**: Call `LimitlessTheme()` without a `colorScheme` override; the static MaterialKolor scheme is used.

---

## Phase 9 — `LimitlessApp.kt` (NavHost) → `commonMain`

Move the root `NavHost` composable to `commonMain`. It only depends on:
- Navigation Compose routes (already common after Phase 1)
- Screen composables (already common after Phase 7)
- `MaterialTheme.colorScheme.background` (common)

**Action**: Straight move after Phases 1-8 are complete.

---

## Phase 10 — Platform Entry Points

### 10a — `androidMain` (stays, update)

- **`LimitlessApplication.kt`**: Update to use `initKoin` + add `AppInfo.init(this)` (matching Jest)
- **`LimitlessActivity.kt`**: Update to resolve dynamic colors and pass to common `LimitlessTheme`. Keep splash screen, edge-to-edge, system bar logic
- **`core/BootReceiver.kt`**: Stays (Android-only)
- **`core/ReminderReceiver.kt`**: Stays (Android-only)
- **`core/ReminderScheduler.kt`**: Stays (Android-only), implements `ReminderScheduling`
- **`core/ReminderNotificationManager.kt`**: Stays (Android-only)

### 10b — `jvmMain` (new)

Replace template stubs with:

```kotlin
// jvmMain — main.kt
fun main() {
    AppInfo.init(versionName = "1.0.0", versionCode = 1, packageName = "com.axondragonscale.limitless")
    initKoin()

    application {
        Window(onCloseRequest = ::exitApplication, title = "Limitless") {
            LimitlessTheme {
                LimitlessApp()
            }
        }
    }
}
```

### 10c — `iosMain` (new)

Replace template stubs with:

```kotlin
// iosMain — MainViewController.kt
fun MainViewController() = ComposeUIViewController {
    initKoin()
    LimitlessTheme {
        LimitlessApp()
    }
}
```

---

## Phase 11 — Cleanup

1. **Delete template stubs**: Remove `Platform.kt`, `Greeting.kt`, `App.kt` from `commonMain`; `Platform.android.kt` from `androidMain`; `Platform.jvm.kt` from `jvmMain`; `Platform.ios.kt` from `iosMain`
2. **Remove `UIMode.kt` from `model/`**: The app uses `UIMode` from commons (`com.axondragonscale.commons.UIMode`). Verify there's no local duplicate — the context doc mentions a local `UIMode.kt` in the model package. If it's a duplicate, delete it; if it differs, reconcile
3. **Verify `build.gradle.kts`**: Ensure Ktor engine dependencies are in the correct platform source sets (currently no Ktor engines are declared — add if networking is used):
   - `androidMain`: OkHttp engine (if using Ktor directly; may come via commons)
   - `jvmMain`: CIO engine
   - `iosMain`: Darwin engine
4. **Remove `util/` package**: `FlowExt.kt` and `Log.kt` in `androidMain/util/` — check if these duplicate commons utilities. If so, delete and use commons versions
5. **Update Room KSP**: Verify all four KSP targets (`kspAndroid`, `kspIosSimulatorArm64`, `kspIosArm64`, `kspJvm`) are configured (they already are in the current `build.gradle.kts`)
6. **Update `build.gradle.kts`**: Add platform-specific Koin dependencies:
   - `androidMain`: `koin-android` bundle (for `androidContext`, `androidLogger`)
   - Check if already present (it's not in the current file but is needed for `androidContext`)

---

## Decisions Log

All decisions have been finalized:

| # | Topic | Decision |
|---|-------|----------|
| 1 | Reminder Scheduling Abstraction | `expect`/`actual` class `ReminderScheduler` with `schedule`, `cancel`, `rescheduleAll`. Android has the full AlarmManager implementation; Desktop/iOS get no-op actuals for now. |
| 2 | Notification Permission in RemindersSection | **Option B** — Small `expect`/`actual` composable for just the permission effect. |
| 3 | Database Migration Strategy | Use commons `createDatabase` helper (destructive migration matches current behavior). |
| 4 | `AtomicLong` in `EditorVM` | **Option A** — Plain `var` (VM event handling is sequential, no concurrency concern). |
| 5 | Preview Annotations | Keep both light and dark mode previews. Use `AndroidUiModes.UI_MODE_NIGHT_YES` from CMP instead of `Configuration.UI_MODE_NIGHT_YES`. |

---

## Expected Final Source Set Layout

```
limitlessComposeApp/src/
├── commonMain/
│   ├── composeResources/
│   │   ├── drawable/
│   │   └── font/                    # Kodchasan TTF files (moved from androidMain/res/font/)
│   └── kotlin/com/axondragonscale/limitless/
│       ├── Link.kt
│       ├── core/
│       │   └── ReminderScheduler.kt       # expect class
│       ├── database/
│       │   ├── LimitlessDatabase.kt       # @ConstructedBy pattern
│       │   ├── converter/                 # All type converters
│       │   ├── dao/                       # All DAOs
│       │   ├── entity/                    # All entities
│       │   └── view/                      # All database views
│       ├── di/
│       │   └── LimitlessModule.kt         # Common Koin module + initKoin()
│       ├── model/                         # All domain models
│       ├── repository/
│       │   ├── AppPrefsRepository.kt      # DataStore (via commons createPreferencesDataStore)
│       │   └── LimitlessRepository.kt
│       └── ui/
│           ├── LimitlessApp.kt            # Root NavHost
│           ├── Route.kt
│           ├── common/                    # All shared UI components
│           ├── day/                       # HabitDay screen + VM
│           ├── devtools/                  # DevTools + SampleDataSeeder
│           ├── editor/                    # Editor screen + VM
│           ├── habit/                     # Habit screen + tabs + VM
│           ├── home/                      # Home screen + VM
│           ├── model/                     # UI-specific models
│           ├── settings/                  # Settings screen + VM
│           ├── stats/                     # Stats screen + VM
│           └── theme/                     # Color, Type, Theme, HabitTheme
│
├── androidMain/
│   ├── AndroidManifest.xml
│   ├── res/                               # Android resources (launcher icons, etc.)
│   └── kotlin/com/axondragonscale/limitless/
│       ├── LimitlessActivity.kt           # Activity + dynamic colors + splash
│       ├── LimitlessApplication.kt        # Koin init + AppInfo.init
│       ├── core/
│       │   ├── BootReceiver.kt            # Android-only
│       │   ├── ReminderReceiver.kt        # Android-only
│       │   ├── ReminderScheduler.android.kt  # actual — full AlarmManager implementation
│       │   └── ReminderNotificationManager.kt  # Android-only
│       ├── di/
│       │   └── LimitlessAndroidModule.kt  # Android-specific Koin bindings (ReminderNotificationManager)
│       └── ui/editor/
│           └── NotificationPermission.android.kt  # actual @Composable
│
├── jvmMain/
│   └── kotlin/com/axondragonscale/limitless/
│       ├── main.kt                        # Desktop entry point
│       └── core/
│           └── ReminderScheduler.jvm.kt   # actual — no-op stub
│
└── iosMain/
    └── kotlin/com/axondragonscale/limitless/
        ├── MainViewController.kt          # iOS entry point
        ├── core/
        │   └── ReminderScheduler.ios.kt   # actual — no-op stub
        └── ui/editor/
            └── NotificationPermission.ios.kt  # actual @Composable (no-op)
```

---

## Migration Order Summary

| Phase | What | Files | Risk |
|-------|------|-------|------|
| 1 | Models & constants | ~20 | None — pure Kotlin |
| 2a | DB entities, views, converters, DAOs | ~16 | Low — Room KMP is well-supported |
| 2b | `LimitlessDatabase` rewrite | 1 | Medium — `@ConstructedBy` pattern change |
| 3 | Repositories | 2 | Low — `AppPrefsRepository` context removal |
| 4 | Reminders abstraction | 1 new interface | Low — depends on decision |
| 5 | DI module split | 1 → 2 | Low |
| 6 | ViewModels | ~28 | Low — `AtomicLong` fix in EditorVM |
| 7 | UI composables | ~40+ | Medium — `@Preview` bulk fix, `RemindersSection` permission split |
| 8 | Theme | 4 | Medium — font loading CMP pattern, theme split |
| 9 | `LimitlessApp` NavHost | 1 | None |
| 10 | Platform entry points | 3 | Low |
| 11 | Cleanup | N/A | None |

---

## Related Context

- [Limitless Context](./context.md) — Current module documentation
- [Jest Context](../jest/context.md) — Reference KMP implementation
- [Jest KMP Migration Plan](../jest/kmp-migration-plan.md) — Completed migration reference
- [Commons Module](../../core/commons/context.md) — Shared utilities, `createDatabase`, `createPreferencesDataStore`, `AppInfo`
- [Build System](../../build-system.md) — Convention plugins, version catalog