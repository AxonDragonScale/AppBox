# KMP Single-App Migration Playbook (Reusable)

> Context: [Main](../context.md) > Migration > KMP Single-App Playbook
>
> **Purpose:** Repeatable, execution-ready plan to migrate **one existing Android app** in AppBox to Kotlin Multiplatform (Android, iOS, Desktop, Browser) using the **KMP Wizard-style structure**.
>
> **Mode:** Planning only (no implementation in this document).

---

## 1) Scope and Decision Baseline

### Chosen structural baseline (approved)

Use **KMP Wizard style** as default:

- `composeApp` → shared Compose Multiplatform + shared logic (with strict internal package boundaries)
- `iosApp` → thin iOS host consuming shared framework via Xcode

### Why this baseline

- Faster migration start — single shared module instead of a module-per-layer explosion
- Lower Gradle overhead — fewer modules = faster configuration and sync
- Better alignment with Compose Multiplatform ecosystem, official templates, and docs
- Easy to harden/split later if complexity grows (see [Section 15](#15-module-splitting-triggers))
- Already proven in the repo — `:libraries:tlv` is a working KMP module with `commonMain`/`commonTest`, JVM, Android, iOS, and Linux targets

### Guardrail

Do **not** let `composeApp` become a monolith. Enforce package boundaries from day 1:

```/dev/null/packages.txt#L1-4
domain/      — pure business logic, zero platform imports
data/        — repository interfaces, DTOs, serialization contracts
platform/    — expect declarations and platform capability interfaces
ui/          — screen state, presenters, Compose UI
```

---

## 2) Target Layout for a Single Migrated App

Use this template under one app folder:

```/dev/null/layout.txt#L1-17
apps/<app>/
├── composeApp/                         # KMP module: shared code for all targets
│   ├── src/
│   │   ├── commonMain/kotlin/<pkg>/    # Shared domain, data, platform contracts, UI
│   │   ├── commonTest/kotlin/<pkg>/    # Shared tests
│   │   ├── androidMain/kotlin/<pkg>/   # Android platform actuals + Android-specific DI
│   │   ├── iosMain/kotlin/<pkg>/       # iOS platform actuals
│   │   ├── desktopMain/kotlin/<pkg>/   # Desktop (JVM) platform actuals
│   │   └── wasmJsMain/kotlin/<pkg>/    # Browser platform actuals
│   └── build.gradle.kts               # KMP + Compose Multiplatform plugin config
├── iosApp/                             # Xcode project consuming shared framework
│   ├── iosApp/
│   │   ├── iOSApp.swift
│   │   └── ContentView.swift           # Thin host calling ComposeView
│   └── iosApp.xcodeproj
└── (legacy Android module retained during migration, then retired)
```

### Package naming convention

Each migrated app keeps its existing package namespace:

| App | Package |
|-----|---------|
| Jest | `com.axondragonscale.jest` |
| Limitless | `com.axondragonscale.limitless` |
| NPinner | `com.axondragonscale.npinner` |
| QRQanvas | `com.axondragonscale.qrqanvas` |
| Topty | `com.axondragonscale.topty` |
| Wallmatic | `com.axondragonscale.wallmatic` |

> **Note:** Keep migration incremental. The legacy Android module coexists with `composeApp` until feature parity is reached. Both can share the same Room database file and DataStore during the transition.

---

## 3) Pre-Migration: Commons Module Strategy

`:core:commons` is a dependency of every app. It **must** be addressed before or during the first app migration.

### 3.1 Commons portability audit

| Component | Location | KMP-Ready | Migration Action |
|-----------|----------|-----------|-----------------|
| `Logger.kt` | root | ✅ Pure Kotlin wrapper | Move to `commonMain` as-is |
| `Result.kt` | root | ✅ Pure Kotlin | Move to `commonMain` as-is |
| `FlowExt.kt` | root | ✅ Coroutines/Flow | Move to `commonMain` as-is |
| `Serialization.kt` | root | ✅ kotlinx.serialization | Move to `commonMain` as-is |
| `UIMode.kt` | root | ✅ Pure enum | Move to `commonMain` as-is |
| `PreferenceExt.kt` | root | ⚠️ DataStore Android | `expect`/`actual` or Android-only source set |
| `PermissionExt.kt` | root | ❌ Android Context | `androidMain` only |
| `ContextExt.kt` | root | ❌ Android Context | `androidMain` only |
| `HttpClient.kt` | network/ | ⚠️ Ktor (engine varies) | `expect`/`actual` for engine; client config in `commonMain` |
| `QRGenerator.kt` | qr/ | ❌ ZXing + Android Bitmap | Platform-specific; define interface in `commonMain` |
| `ModifierExt.kt` | compose/ | ✅ Compose Multiplatform | Move to `commonMain` once Compose MP is adopted |
| `ComposeUtils.kt` | compose/ | ⚠️ Uses AndroidX insets | Compose MP equivalent needed |
| `Dropdown.kt` | compose/ | ✅ Pure Compose | Move to `commonMain` |
| Chart composables | compose/chart/ | ✅ Pure Compose Canvas | Move to `commonMain` |
| Room TypeConverters | room/ | ❌ Room Android-only | `androidMain` only; not needed on other platforms |
| DevTools | devtools/ | ✅ Pure Compose UI | Move to `commonMain` |

### 3.2 Commons migration approach

**Option A (recommended): Migrate commons to KMP first as a standalone step.**

1. Convert `:core:commons` to a KMP library module (similar to `:libraries:tlv`)
2. Move portable code to `commonMain`, Android-specific code to `androidMain`
3. Define `expect`/`actual` for Ktor engine and DataStore
4. All existing apps continue consuming the `androidMain` surface — zero breakage
5. New KMP app modules consume `commonMain` surface

**Option B: Migrate commons inline with the first app.**

Faster to start but risks blocking the first app migration on commons issues. Use only if the first app has minimal commons usage.

### 3.3 Ktor client KMP pattern

```/dev/null/ktor-kmp.kt#L1-18
// commonMain — shared config
expect fun createPlatformHttpEngine(): HttpClientEngine

fun createHttpClient(json: Json): HttpClient = HttpClient(createPlatformHttpEngine()) {
    install(ContentNegotiation) { json(json) }
    // shared config...
}

// androidMain
actual fun createPlatformHttpEngine(): HttpClientEngine = OkHttp.create()

// iosMain
actual fun createPlatformHttpEngine(): HttpClientEngine = Darwin.create()

// desktopMain
actual fun createPlatformHttpEngine(): HttpClientEngine = CIO.create()
```

### 3.4 Koin KMP pattern

Koin already supports KMP. The pattern stays the same:

```/dev/null/koin-kmp.kt#L1-14
// commonMain — shared DI
val CommonModule = module {
    single { createJson() }
    single { createHttpClient(get()) }
}

// androidMain — platform additions
val AndroidPlatformModule = module {
    // Android-specific bindings (Context-dependent services)
}

// Each app's host wires: CommonModule + AppModule + PlatformModule
```

---

## 4) Dependency Migration Map

Every library currently used must have a KMP migration path. This table is the **source of truth** for dependency decisions.

### 4.1 Already KMP-compatible (no change needed)

| Current | KMP Status | Notes |
|---------|-----------|-------|
| Kotlin Coroutines + Flow | ✅ Native KMP | Works on all targets |
| kotlinx.serialization | ✅ Native KMP | Works on all targets |
| kotlinx.datetime | ✅ Native KMP | Works on all targets |
| Ktor Client | ✅ Native KMP | Swap engine per platform (OkHttp → Darwin/CIO) |
| Koin | ✅ Native KMP | `koin-core` is multiplatform; compose/navigation extensions available |
| Coil 3 | ✅ KMP support | Coil 3 supports Compose Multiplatform |
| kotlin.time | ✅ Stdlib | Works everywhere |

### 4.2 Requires replacement on non-Android targets

| Current (Android) | KMP Replacement | Scope |
|-------------------|-----------------|-------|
| Room | **SQLDelight** | Database — define schema in common, platform drivers per target |
| DataStore Preferences | **multiplatform-settings** or KMP DataStore (experimental) | Simple key-value prefs |
| Proto DataStore | Custom serialization + file storage or **Okio** multiplatform | Complex typed settings |
| Jetpack Navigation Compose | **Compose Multiplatform navigation** (from JetBrains) or **Voyager**/**Decompose** | Navigation — evaluate during bootstrap |
| WorkManager | **expect/actual** pattern with platform schedulers | Background work — iOS BGTaskScheduler, Desktop timers |
| Jetpack Glance | **Android-only** (no KMP equivalent) | Widgets — P2 Android-only feature |
| CameraX + ML Kit | **expect/actual** or platform-specific screens | Camera/scanning — heavy platform dependency |
| BiometricPrompt | **expect/actual** with `SecureStore` contract | Auth — iOS FaceID/TouchID, Desktop keychain |
| EncryptedSharedPreferences | **expect/actual** `SecureStore` | Secure storage |
| Accompanist Permissions | **moko-permissions** or custom `expect`/`actual` | Runtime permissions |
| WallpaperManager (Android) | ❌ No equivalent | Android-only — P2/dropped for other platforms |
| Quick Settings TileService | ❌ No equivalent | Android-only — P2 |

### 4.3 Navigation decision

**Recommended: Compose Multiplatform Navigation** (official JetBrains fork of AndroidX Navigation).

- Closest API match to the existing `@Serializable` type-safe routes already used across all apps
- Minimizes code changes during migration
- `NavController`, `NavHost`, `composable<Route>` all work cross-platform

If type-safe routes are not yet supported in the multiplatform fork at migration time, evaluate **Decompose** as the fallback (more mature KMP navigation, but different API surface).

### 4.4 Database decision

**Recommended: SQLDelight** for apps that need cross-platform persistence.

- SQL-first approach — schema is `.sq` files, generates type-safe Kotlin
- Platform drivers: `AndroidSqliteDriver`, `NativeSqliteDriver` (iOS), `JdbcSqliteDriver` (Desktop/JVM)
- Supports Flow-based reactive queries
- Migration path from Room: export Room schema → rewrite as `.sq` files → adapt DAO call sites to generated queries

**For apps with simple persistence needs**, consider keeping Room on Android and using SQLDelight only on non-Android targets behind a common repository interface.

---

## 5) App Risk Assessment and Migration Order

Rank apps by migration complexity to de-risk the process. Migrate the simplest app first.

| Rank | App | Complexity | Platform-Bound Features | Risk Level | Recommended Order |
|------|-----|-----------|------------------------|------------|------------------|
| 1 | **Jest** | Low | Glance widget (P2, Android-only) | 🟢 Low | **First** — API client + Room + simple UI. Excellent pilot. |
| 2 | **Limitless** | Low-Medium | None significant | 🟢 Low | **Second** — Room + DataStore + statistics UI. No platform-bound features. |
| 3 | **QRQanvas** | Medium | CameraX + ML Kit scanning, ZXing generation | 🟡 Medium | **Third** — Camera is platform-specific but well-bounded. App is still scaffolded, so migration and build can happen simultaneously. |
| 4 | **NPinner** | Medium-High | Notifications, WorkManager, BroadcastReceivers, scheduling | 🟡 Medium | **Fourth** — Heavy Android platform integration but domain is straightforward. |
| 5 | **Wallmatic** | High | WallpaperManager, WorkManager, TileService, SAF/DocumentFile, Proto DataStore | 🔴 High | **Fifth** — Most platform-bound app. Many Android-only APIs with no cross-platform equivalents. |
| 6 | **Topty** | High | Keystore encryption, BiometricPrompt, CameraX, EncryptedSharedPreferences | 🔴 High | **Sixth** — Security-critical. Encryption must work identically across platforms. Highest risk for security regressions. |

### Why Jest first

- Simplest dependency graph: Ktor API → Room cache → Compose UI
- The Glance widget is cleanly isolated and can remain Android-only (P2)
- Validates the full migration pipeline: build config, commons consumption, CI, testing
- Low user-facing risk if something breaks
- Fast feedback loop for the team

---

## 6) Migration Strategy (Single-App, Vertical Slices)

Use a **strangler fig approach** in small, releasable slices:

### Phase A — Discovery (1-2 days)

1. **Build feature parity matrix** for this app (see [Section 7](#7-feature-parity-matrix-template))
2. **Inventory platform-bound code** — grep for `android.`, `androidx.`, `android.content.Context` imports
3. **Identify security/data risk areas** — any encryption, secure storage, or sensitive data handling
4. **Document first 3 vertical slices** to migrate

### Phase B — Bootstrap (1-2 days)

1. **Add `composeApp` module** with KMP + Compose Multiplatform plugins
2. **Add `iosApp` Xcode project** (use KMP wizard or `File → New → Kotlin Multiplatform App` template)
3. **Wire all declared targets**: Android, iOS (arm64 + simulatorArm64), Desktop (JVM), wasmJs
4. **Configure Koin** in `commonMain` with platform module stubs
5. **Verify builds** on all targets with a "Hello World" screen
6. **Add CI jobs** — at minimum: `composeApp:assemble`, `composeApp:allTests`, iOS framework build

### Phase C — Incremental Migration (bulk of the work)

**Step 1: Extract pure domain first**
- Move models, enums, validation rules, formatters to `commonMain/domain/`
- Move serialization DTOs and mappers to `commonMain/data/`
- Add `commonTest` immediately for all moved logic
- **Milestone:** All model/domain classes compile in `commonMain`

**Step 2: Extract one feature slice end-to-end**
- Pick the simplest, most self-contained feature
- Move: use case + repository interface + state holder/presenter to common
- Keep platform implementations (Room DAO, Android services) in `androidMain`
- Add stub/mock implementations for iOS/Desktop/Web
- **Milestone:** One screen works on Android via `composeApp`, with compile-passing stubs on other targets

**Step 3: Bridge UI**
- Reuse shared logic from Android first (existing Android app consumes `composeApp` as dependency)
- Enable Compose Multiplatform screens progressively
- Start with the simplest screen — verify theming, navigation, and state management work cross-platform
- **Milestone:** One screen renders on Desktop (easiest non-Android target to test)

**Step 4: Repeat per feature**
- Each slice follows the same pattern: domain → data contract → platform impl → UI
- Each slice must be independently releasable on Android (no regressions)
- Expand platform implementations as confidence grows

### Phase D — Platform Completion

1. Fill in iOS implementations for all P0/P1 features
2. Fill in Desktop implementations for all P0/P1 features
3. Make Web parity/deferral decisions (Web is typically the most limited target)
4. Run platform-specific QA passes

### Phase E — Closure

1. Decommission legacy Android-only module (or repurpose as thin host)
2. Ensure all CI pipelines pass across all declared targets
3. Update context docs and this playbook with lessons learned
4. Release readiness review

---

## 7) Feature Parity Matrix Template

For each app, classify every feature before writing migration code.

**Legend:**
- ✅ Full support
- ⚠️ Partial / degraded (document what's missing)
- ❌ Not supported / deferred
- 🚫 Not applicable to this platform

**Classifications:**
- **P0 Core Shared** — must work identically on all targets
- **P1 Shared with platform adapters** — same behavior, platform-specific implementation
- **P2 Android-only / deferred** — stays in `androidMain` or dropped for now

```/dev/null/parity-template.md#L1-18
## App: <name>

| # | Feature | Android | iOS | Desktop | Web | Classification | Notes |
|---|---------|---------|-----|---------|-----|----------------|-------|
| 1 |         | ✅/⚠️/❌ | ✅/⚠️/❌ | ✅/⚠️/❌ | ✅/⚠️/❌ | P0/P1/P2 |       |

### Example (Jest):
| # | Feature | Android | iOS | Desktop | Web | Classification | Notes |
|---|---------|---------|-----|---------|-----|----------------|-------|
| 1 | Fetch joke from API | ✅ | ✅ | ✅ | ✅ | P0 | Ktor works on all targets |
| 2 | Display joke (single/two-part) | ✅ | ✅ | ✅ | ✅ | P0 | Compose MP |
| 3 | Joke categories & filters | ✅ | ✅ | ✅ | ✅ | P0 | Pure domain logic |
| 4 | Favorites (persist) | ✅ | ✅ | ✅ | ⚠️ | P1 | SQLDelight on all; Web limited to IndexedDB |
| 5 | History (persist) | ✅ | ✅ | ✅ | ⚠️ | P1 | Same as favorites |
| 6 | Preferences (categories, blacklist) | ✅ | ✅ | ✅ | ⚠️ | P1 | multiplatform-settings or expect/actual |
| 7 | Home screen widget | ✅ | 🚫 | 🚫 | 🚫 | P2 | Glance — Android-only, keep in androidMain |
| 8 | Chucker (debug networking) | ✅ | 🚫 | 🚫 | 🚫 | P2 | Debug-only, Android-only |
```

**Human-owned decisions:** Parity scope, acceptable degradation, P2 deferrals.
**Agent-owned support:** Generate first draft from code inventory, flag ambiguities.

---

## 8) Architecture Rules Inside `composeApp`

### 8.1 Domain (`commonMain/<pkg>/domain/`)

- Business entities / domain models
- Use cases / interactors
- Validation and business rules
- Formatters and mappers
- **Zero platform imports** — no `android.*`, no `platform.*`, no `UIKit.*`

### 8.2 Data contracts (`commonMain/<pkg>/data/`)

- Repository **interfaces** (implementations are platform-specific or in common if pure)
- DTO models with `@Serializable`
- API client interfaces and response models
- Domain ↔ DTO mappers

### 8.3 Platform boundary (`commonMain/<pkg>/platform/`)

- `expect` declarations for platform capabilities
- Interface contracts for platform services

Required contracts vary per app. Common ones:

| Contract | Android Actual | iOS Actual | Desktop Actual | Web Actual |
|----------|---------------|------------|---------------|------------|
| `SecureStore` | Keystore / EncryptedSharedPrefs | Keychain | Encrypted file | ⚠️ Documented risk |
| `NotificationService` | NotificationManager | UNUserNotificationCenter | System tray | Web Notifications API |
| `CameraScanner` | CameraX + ML Kit | AVFoundation | ❌ Deferred | ❌ Deferred |
| `BackgroundScheduler` | WorkManager | BGTaskScheduler | Timer/Coroutines | ❌ N/A |
| `ClipboardService` | ClipboardManager | UIPasteboard | AWT Toolkit | navigator.clipboard |
| `ShareService` | Intent.ACTION_SEND | UIActivityViewController | ❌ Deferred | navigator.share |
| `BiometricAuth` | BiometricPrompt | LAContext (FaceID/TouchID) | ❌ Deferred | WebAuthn (experimental) |
| `FileSystem` | SAF / DocumentFile | FileManager | java.io.File / Okio | ❌ Limited |
| `PlatformContext` | Android Context | — | — | — |

### 8.4 UI (`commonMain/<pkg>/ui/`)

- Screen state models (`UiState`, `UiEvent`, `UiEffect`)
- Presenters / state holders (avoid `ViewModel` in common — use `ScreenModel` or plain class with `CoroutineScope`)
- Compose Multiplatform UI (shared when practical)
- Theme definitions (colors, typography, shapes)

### 8.5 ViewModel strategy

Android's `ViewModel` is not available in `commonMain`. Options:

**Option A (recommended): Use Koin's KMP ViewModel support.**
`koin-compose-viewmodel` provides `koinViewModel()` that works on all Compose Multiplatform targets. The `ViewModel` class comes from `androidx.lifecycle:lifecycle-viewmodel` which has KMP artifacts.

**Option B: Wrap in expect/actual.**
```/dev/null/viewmodel-pattern.kt#L1-6
// commonMain
expect abstract class PlatformViewModel() {
    val viewModelScope: CoroutineScope
    open fun onCleared()
}
// androidMain — actual typealias = androidx.lifecycle.ViewModel
```

**Option C: Plain class with injected scope.** Use for simple cases where lifecycle awareness isn't needed.

**Recommendation:** Start with Option A. The AndroidX Lifecycle library now publishes KMP artifacts, and Koin integrates with them. This preserves the existing `*VM` naming convention and `viewModelOf(::MyVM)` Koin DSL.

---

## 9) Data & Persistence Strategy

### 9.1 Database

| Approach | When to use |
|----------|-------------|
| **Room (Android) + SQLDelight (other)** behind common repo interface | Migration phase — minimize Android-side changes |
| **SQLDelight everywhere** | Post-migration — single schema source of truth |
| **No DB (in-memory / file cache)** | Early milestones on non-Android targets with low persistence needs |

### 9.2 Room → SQLDelight migration steps (per app)

1. Export Room schema JSON (`exportSchema = true` is already enabled)
2. Create equivalent `.sq` files in `commonMain`
3. Implement `SqlDriver` `expect`/`actual` per platform
4. Adapt repository implementations to use SQLDelight generated queries
5. Write data migration test: Room export → SQLDelight import
6. Remove Room dependency once all targets use SQLDelight

### 9.3 DataStore

- **Preferences DataStore** (used by Jest, Limitless, NPinner, Topty): Replace with **multiplatform-settings** (`com.russhwolf:multiplatform-settings`) — similar API, KMP-native
- **Proto DataStore** (used by Wallmatic): Replace with **kotlinx.serialization** + **Okio** multiplatform file I/O. Serialize settings to JSON/Protobuf, read/write via Okio's `FileSystem`

### 9.4 Repository pattern

```/dev/null/repo-pattern.kt#L1-16
// commonMain — interface
interface JokeRepository {
    fun getAllJokes(): Flow<List<Joke>>
    fun getFavorites(): Flow<List<Joke>>
    suspend fun saveJoke(joke: Joke)
    suspend fun toggleFavorite(jokeId: Int)
}

// androidMain — Room implementation
class AndroidJokeRepository(private val dao: JokeDao) : JokeRepository { ... }

// iosMain / desktopMain — SQLDelight implementation
class SqlDelightJokeRepository(private val db: JokeDatabase) : JokeRepository { ... }

// Koin wires the correct implementation per platform
```

---

## 10) Security Strategy (Mandatory for Topty, Recommended for Others)

### 10.1 Security classification per app

| App | Classification | Rationale |
|-----|---------------|-----------|
| Topty | 🔴 **High** | TOTP/HOTP secrets, master password, AES-256-GCM encryption, biometrics |
| NPinner | 🟢 **Low** | No sensitive data — notification text only |
| Jest | 🟢 **Low** | No sensitive data — jokes and preferences |
| Limitless | 🟢 **Low** | No sensitive data — habit names and records |
| QRQanvas | 🟡 **Medium** | QR codes may contain sensitive URLs/data |
| Wallmatic | 🟢 **Low** | Wallpaper URIs and scheduling config |

### 10.2 SecureStore contract (for High/Medium security apps)

```/dev/null/secure-store.kt#L1-20
// commonMain
interface SecureStore {
    suspend fun store(key: String, value: ByteArray)
    suspend fun retrieve(key: String): ByteArray?
    suspend fun delete(key: String)
    suspend fun clear()
}

// androidMain
class AndroidSecureStore(context: Context) : SecureStore {
    // EncryptedSharedPreferences or Android Keystore
}

// iosMain
class IosSecureStore : SecureStore {
    // Keychain Services API
}

// desktopMain / wasmJsMain
class DesktopSecureStore : SecureStore {
    // Encrypted file with documented threat model
}
```

### 10.3 Crypto portability (Topty-specific)

- **HMAC generation** (`HMACGenerator`): Use `kotlinx-crypto` or `Korlibs/Krypto` for KMP HMAC — avoid `javax.crypto` in common
- **AES-256-GCM** (`TokenEncryption`): Needs KMP crypto library or `expect`/`actual` wrapping platform crypto APIs
- **PBKDF2** (`PasswordManager`): Same — use KMP crypto or platform-specific key derivation
- **Cross-platform test vectors**: Add deterministic test cases in `commonTest` to verify TOTP/HOTP output matches across all platforms — this is non-negotiable for a 2FA app

### 10.4 Security requirements

- Define threat model per platform (Desktop/Web have weaker isolation than mobile)
- Document what is and isn't encrypted at rest per platform
- Biometric auth: `expect`/`actual` with graceful degradation (no biometrics on Desktop/Web → fallback to password)
- **Human-owned:** Final threat model sign-off before Topty migration

---

## 11) UI Sharing Policy

Default policy: **logic-first, then UI sharing.**

### 11.1 Sharing tiers

| Tier | What | Where | Priority |
|------|------|-------|----------|
| 1 | Domain models, use cases, business rules | `commonMain/domain/` | Always share |
| 2 | State management (UiState, UiEvent, reducers) | `commonMain/ui/` | Always share |
| 3 | Compose UI screens | `commonMain/ui/` | Share when practical |
| 4 | Platform-specific UI (widgets, tiles, notifications) | `androidMain/`, `iosMain/` | Never force into common |

### 11.2 When to keep UI platform-specific

- Home screen widgets (Glance) — Android-only, no equivalent elsewhere
- Quick Settings tiles — Android-only
- Notification UIs with custom layouts — platform notification APIs differ fundamentally
- Camera preview composables — CameraX vs AVFoundation have different lifecycle models
- Platform-specific UX patterns (iOS swipe-back, Desktop window management)

### 11.3 Theme migration

Each app has a custom theme (e.g., Limitless uses Kodchasan font, Jest has its own palette). For Compose Multiplatform:

- Material 3 theming works cross-platform in Compose MP
- Custom fonts: bundle `.ttf` files in `commonMain/composeResources/font/`
- Dynamic Color (Material You): Android-only — detect platform and fall back to static colors elsewhere

---

## 12) Dependency Injection Policy

Respect AppBox constraints — **Koin only**, never Hilt/Dagger.

### 12.1 Module structure

```/dev/null/di-structure.kt#L1-21
// commonMain — shared DI definitions
val AppCommonModule = module {
    // Domain
    factoryOf(::SomeUseCase)
    // Data
    singleOf(::SomeApiClient)
    // Platform (expect-based)
    // Bound by platform modules below
}

// androidMain
val AndroidPlatformModule = module {
    single<SecureStore> { AndroidSecureStore(androidContext()) }
    single<SomeRepository> { RoomSomeRepository(get()) }
    single { SomeDatabase.create(androidContext()) }
}

// iosMain
val IosPlatformModule = module {
    single<SecureStore> { IosSecureStore() }
    single<SomeRepository> { SqlDelightSomeRepository(get()) }
}
```

### 12.2 Koin initialization per platform

- **Android**: `startKoin { }` in `Application.onCreate()` (same as today)
- **iOS**: `startKoin { }` called from Swift via a `fun initKoin()` helper in `iosMain`
- **Desktop**: `startKoin { }` in `main()` function
- **Web**: `startKoin { }` in `main()` or `onWasmReady { }`

### 12.3 ViewModel registration

```/dev/null/vm-di.kt#L1-4
// commonMain — works with koin-compose-viewmodel KMP
val ViewModelModule = module {
    viewModelOf(::HomeVM)
}
```

---

## 13) Testing & Quality Gates (Per Slice)

### 13.1 Minimum test requirements

| Layer | Test Type | Location | Required |
|-------|-----------|----------|----------|
| Domain models & use cases | Unit tests | `commonTest` | ✅ Every slice |
| State holders / presenters | Unit tests | `commonTest` | ✅ Every slice |
| Repository implementations | Integration tests | `androidTest`, platform tests | ✅ At least Android |
| API clients | Unit tests with mock server | `commonTest` | ✅ For network-dependent features |
| Platform adapters | Platform-specific tests | `androidTest`, `iosTest` | ✅ For security/crypto |
| Compose UI | Preview-based + screenshot tests | `commonTest` or `androidTest` | ⚠️ Nice to have |
| Cross-platform crypto | Deterministic test vectors | `commonTest` | ✅ Mandatory for Topty |

### 13.2 Regression gates

- Existing Android behavior must not regress — run the full existing test suite after each migration step
- Build verification on **all declared targets** before merging any migration PR
- No slice is "done" without tests for moved logic

### 13.3 CI pipeline additions

```/dev/null/ci-jobs.txt#L1-8
# Per migrated app, add these CI jobs:
- composeApp:assembleDebug              # Android target builds
- composeApp:assembleRelease            # Android release builds
- composeApp:allTests                   # commonTest + platform tests
- composeApp:jvmJar                     # Desktop target builds
- composeApp:linkDebugFrameworkIos*     # iOS framework builds
- composeApp:wasmJsBrowserTest          # Web target tests (if applicable)
- iosApp Xcode build                    # Full iOS app build (separate Xcode step)
```

---

## 14) Agent vs Human Effectiveness Matrix

### Better done by Agent

- Inventory portable vs platform-bound code (grep for `android.*` imports, categorize files)
- Scaffold `composeApp` module structure and `build.gradle.kts` boilerplate
- Generate `expect`/`actual` stubs from existing platform-specific classes
- Move pure Kotlin files from legacy module to `commonMain` (models, enums, use cases)
- Create repository interfaces from existing concrete repositories
- Add `commonTest` tests for moved domain logic
- Draft feature parity matrix from code inventory
- Generate SQLDelight `.sq` schema from Room entity annotations
- Create Koin module definitions for common and platform source sets

### Better done by Human

- Feature parity decisions — which features to share, which to defer
- Migration ordering by product risk and business priority
- UX tradeoffs for Desktop and Web (no established patterns to follow)
- Security and compliance acceptance for Topty
- Navigation library selection (if Compose MP Navigation doesn't meet needs)
- Final approval of irreversible architecture decisions (e.g., dropping Room entirely)
- iOS-specific UX polish and App Store requirements
- Performance benchmarking and acceptable thresholds per platform

---

## 15) Module Splitting Triggers

Start with a single `composeApp` module. Split **only** when these warning signs appear:

| Signal | Symptom | Action |
|--------|---------|--------|
| Frequent merge conflicts | Multiple features editing the same `composeApp` files | Extract domain into `:shared:domain` module |
| Build time growth | `composeApp` takes >2 minutes to compile incrementally | Profile and split heaviest packages |
| Security boundary violations | Security-sensitive code (crypto, secrets) mixes with generic UI | Extract `:shared:security` module |
| Unstable builds | Unrelated changes break other features | Split along feature boundaries |
| Shared library opportunity | Multiple migrated apps need the same common KMP code | Extract to `:core:commons-kmp` or dedicated library |

When splitting, prefer **horizontal layers** (domain, data, platform, ui) over **vertical features** — consistent with AppBox's existing architecture patterns.

---

## 16) Build Configuration Reference

### 16.1 `composeApp/build.gradle.kts` template

```/dev/null/build.gradle.kts#L1-61
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)  // or android.library if thin host
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
                }
            }
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.bundles.coroutines)
            implementation(libs.bundles.koin)
            implementation(libs.bundles.ktor)  // common ktor modules
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.core.commons)  // once commons is KMP
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            // Android-specific dependencies
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
```

### 16.2 Version catalog additions needed

Add these to `gradle/libs.versions.toml` before the first migration:

```/dev/null/versions.toml#L1-17
[versions]
compose-multiplatform = "1.7.x"  # Match latest stable
sqldelight = "2.0.x"
multiplatform-settings = "1.2.x"

[libraries]
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
sqldelight-jvm-driver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }

[plugins]
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

### 16.3 Convention plugin strategy

The existing `app-convention` and `library-convention` plugins are Android-only. Options:

1. **Create a `kmp-app-convention` plugin** — applies KMP + Compose MP + common targets config
2. **Extend existing plugins** — add KMP support conditionally (higher complexity)
3. **Inline build config** — configure per-app `build.gradle.kts` directly (simplest to start)

**Recommendation:** Start with option 3 (inline) for the first migrated app. Extract to a convention plugin once the config stabilizes after 2-3 apps are migrated.

---

## 17) Phase Checklist (Copy for Each App)

```/dev/null/checklist.md#L1-38
## App: <name>

### Phase A — Discovery
- [ ] Feature parity matrix drafted and reviewed
- [ ] Platform capability gap list created
- [ ] Security/data risk areas identified and classified
- [ ] First 3 vertical slices chosen and ordered
- [ ] Commons module dependencies audited for this app
- [ ] Approvals: parity scope, architecture, security (if applicable)

### Phase B — Bootstrap
- [ ] `composeApp` module created with KMP + Compose MP plugins
- [ ] `iosApp` Xcode project scaffolded
- [ ] All target builds verified (Android, iOS, Desktop, wasmJs)
- [ ] Koin initialized on all platforms with stub modules
- [ ] CI jobs added for all targets
- [ ] "Hello World" screen renders on all platforms

### Phase C — Incremental Migration
- [ ] Domain models and enums moved to `commonMain`
- [ ] `commonTest` added for all moved domain logic
- [ ] First vertical slice migrated end-to-end
- [ ] Repository interfaces in `commonMain`, Android impl connected
- [ ] Second vertical slice migrated
- [ ] Third vertical slice migrated
- [ ] All remaining slices migrated
- [ ] Android regression suite passing

### Phase D — Platform Completion
- [ ] iOS: all P0/P1 features implemented
- [ ] Desktop: all P0/P1 features implemented
- [ ] Web: parity / deferral decisions finalized and documented
- [ ] Platform-specific QA pass completed
- [ ] Performance benchmarks acceptable on all targets

### Phase E — Closure
- [ ] Legacy Android-only module retired or repurposed as thin host
- [ ] Multi-target CI green on all jobs
- [ ] App context doc updated (docs/apps/<app>/context.md)
- [ ] This playbook updated with lessons learned
- [ ] Release readiness reviewed and signed off
```

---

## 18) Done Criteria for Single-App Migration

Migration for one app is "done" when **all** of these are true:

- [ ] Core domain and use cases are in `commonMain`
- [ ] Agreed P0/P1 parity matrix is satisfied for all target platforms
- [ ] Security and storage policy is implemented and signed off (if applicable)
- [ ] CI builds and tests pass across all declared targets
- [ ] Android app behavior is unchanged for existing users (no regressions)
- [ ] Performance is within acceptable thresholds on all platforms
- [ ] Context documentation is updated for maintainers
- [ ] Lessons learned are fed back into this playbook

---

## 19) Risk Register Template

Track these risks **per app** throughout migration. Each risk must have an owner, mitigation plan, and exit criteria.

| Risk | Likelihood | Impact | Mitigation | Owner | Exit Criteria |
|------|-----------|--------|------------|-------|--------------|
| Build complexity / Gradle sync time explosion | Medium | High | Start inline, extract convention plugins after 2-3 apps | Human | Incremental build < 2 min |
| Performance regression on iOS/Web | Medium | Medium | Benchmark early, set thresholds before platform completion | Human | Matches or exceeds native perf targets |
| Platform API mismatch (camera, scheduling, background) | High | Medium | Audit upfront in discovery phase, accept P2 deferrals | Human | All P0/P1 features working |
| Security regression in Topty crypto | Low | 🔴 Critical | Cross-platform test vectors, threat model review | Human | All test vectors pass, security sign-off |
| Web storage limitations (no filesystem, no Keychain) | High | Low | Document risk, use IndexedDB with encryption for Web | Human | Documented and accepted |
| Scope creep from "share everything" pressure | Medium | Medium | Strict P0/P1/P2 classification, defer non-essential sharing | Human | Parity matrix unchanged from approved version |
| Commons module migration blocks app migration | Medium | High | Migrate commons first (Option A) or identify exact commons surface needed | Agent/Human | Commons `commonMain` compiles on all targets |
| iOS build toolchain complexity (Xcode, CocoaPods/SPM) | Medium | Medium | Use KMP wizard template, avoid CocoaPods if possible | Human | iOS builds in CI without manual intervention |

---

## 20) Practical Notes for AppBox

1. **Start with Jest** — lowest risk, validates the entire pipeline, fast feedback
2. **Migrate `:core:commons` to KMP first** (or in parallel with Jest) — every app depends on it
3. **Leverage `:libraries:tlv` as a reference** — it's already a working KMP module in the repo with `commonMain`/`commonTest`, multiple targets, and a `build.gradle.kts` you can reference for KMP configuration patterns
4. **Reuse this playbook unchanged** for each app — only swap the app-specific parity matrix and checklist
5. **Keep existing convention plugins** for non-migrated apps — they continue using `app-convention` until migrated
6. **Don't migrate all apps at once** — complete one, learn, then start the next
7. **Web (wasmJs) is the lowest priority target** — it has the most limitations. Wire it during bootstrap but defer feature implementation until Android, iOS, and Desktop are stable
8. **Harden module boundaries** when warning signs appear (see [Section 15](#15-module-splitting-triggers))

---

## Reusable Single-App Plan Header (Fill Before Starting)

```/dev/null/plan-header.md#L1-22
App:
Package:
Owner:
Start date:
Target platforms: Android / iOS / Desktop / Web
Web target mode: wasmJs
Security classification: Low / Medium / High
Chosen persistence approach: Room+SQLDelight / SQLDelight-only / In-memory
Navigation library: Compose MP Navigation / Decompose / Other
Commons dependency surface: (list which commons components this app uses)

First 3 vertical slices:
1)
2)
3)

Parity target date:

Approvals:
- [ ] Feature parity matrix reviewed and approved
- [ ] Architecture approach approved
- [ ] Security approach approved (if High/Medium classification)
- [ ] Migration order confirmed (not blocking higher-priority work)
```

---

## Related Context

- [Main Context](../context.md) — Project overview, tech stack, architecture patterns, module index
- [Build System](../build-system.md) — Convention plugins, version catalog, module setup
- [Coding Conventions](../coding-conventions.md) — Compose patterns, Kotlin idioms, naming conventions
- [Commons Module](../core/commons/context.md) — Shared utilities that must be KMP-migrated
- [TLV Library](../libraries/tlv/context.md) — Existing KMP module in the repo (reference for build config)