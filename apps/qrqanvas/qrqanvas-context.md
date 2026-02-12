# QRQanvas - LLM/Agent Context

## Overview

QRQanvas is a customizable QR code generator and scanner app within the AppBox monorepo. Users create stylized QR codes with custom colors, shapes, dot patterns, eye styles, and embedded logos, then save/share them. It also scans QR codes via CameraX + ML Kit.

**Package**: `com.axondragonscale.qrqanvas`
**Module path**: `:apps:qrqanvas`
**Current state**: Scaffolded — only has a default template Activity and theme. All features need to be built.

## Tech Stack (Already Declared in build.gradle.kts)

| Category | Libraries | Notes |
|----------|-----------|-------|
| QR Generation | ZXing (`libs.zxing`) | Use `com.google.zxing.qrcode.QRCodeWriter` to generate `BitMatrix`, then render custom visuals on top |
| QR Scanning | CameraX (`libs.bundles.camerax`) + ML Kit (`libs.mlkit.barcode.scanner`) | Camera preview + barcode detection pipeline |
| Database | Room (`libs.androidx.room.*`) + KSP | Persist saved QR designs, templates, scan history |
| Settings | Proto DataStore (`libs.bundles.datastore` + `libs.protobuf.kotlin`) | App preferences (default colors, theme, etc.) |
| Image Loading | Coil 3 (`libs.bundles.coil`) | Loading logo images for QR embedding |
| Permissions | Accompanist (`libs.accompanist.permissions`) | Camera permission flow |
| Navigation | Navigation Compose (`libs.androidx.navigation.compose`) | Type-safe routes with `@Serializable` |
| DI | Koin (`libs.bundles.koin`) | NOT Hilt/Dagger |
| Serialization | kotlinx.serialization (`libs.kotlinx.serialization.json`) | NOT Gson/Moshi |
| Async | Coroutines + Flow (`libs.bundles.coroutines`) | |
| UI | Jetpack Compose + Material 3 (`libs.bundles.compose`) | Via Compose BOM — never specify individual Compose versions |

## Project Conventions (Critical)

### Build
- Convention plugin: `id("app-convention")` — provides AGP, Kotlin, Compose, signing, SDK versions
- Min SDK 31, Target SDK 36, JVM 21
- Room uses **KSP**, not KAPT
- Typesafe project accessor: `implementation(projects.core.commons)`

### Architecture Pattern (follow wallmatic/topty/jest patterns)
```
com.axondragonscale.qrqanvas/
├── QRQanvasActivity.kt          # Single Activity, installs splash screen
├── QRQanvasApplication.kt       # Initializes Koin with CommonsModule + QRQanvasModule
├── di/
│   └── QRQanvasModule.kt        # Koin module: database, repos, managers, viewModels
├── database/
│   ├── QRQanvasDatabase.kt      # Room database with companion create()
│   ├── dao/                     # Room DAOs
│   ├── entity/                  # Room entities
│   └── converter/               # TypeConverters if needed
├── model/                       # Domain/UI models, enums
├── repository/                  # Data repositories
├── core/                        # Business logic managers
├── ui/
│   ├── Route.kt                 # @Serializable route objects
│   ├── QRQanvasApp.kt           # NavHost composable
│   ├── theme/                   # Already exists: Color.kt, Theme.kt, Type.kt
│   ├── common/                  # Shared UI components
│   ├── home/                    # Home screen (list of saved QR codes)
│   ├── creator/                 # QR creation/customization screen
│   ├── scanner/                 # QR scanner screen
│   ├── detail/                  # QR detail/view screen
│   ├── settings/                # Settings screen
│   └── devtools/                # DevTools screen wrapper
└── util/                        # Extension functions, helpers
```

### UI Layer Conventions
- **Modifier is always the first parameter** with default `Modifier = Modifier` in ALL composables
- **Two-tier composable pattern**: Top-level handles ViewModel + navigation; private inner is pure UI for previews
- **ViewModel naming**: `HomeVM`, `CreatorVM` (suffix `VM`, not `ViewModel`)
- **State files per screen**: `HomeUiState.kt`, `HomeUiEvent.kt`, optionally `HomeUiEffect.kt`
- **UiEvent is a sealed interface**; ViewModel has `fun onEvent(event: UiEvent)`
- **UiState is a data class** with `MutableStateFlow` in ViewModel
- **UiEffect** (one-shot events like navigation): `Channel` + `receiveAsFlow().flowOn(Dispatchers.Main.immediate)`
- Navigation events that are purely handled in Compose should return `Unit` in `onEvent`
- Use `koinViewModel()` for ViewModel injection in composables, `koinInject()` for other deps
- Use `collectAsStateWithLifecycle()` to collect state flows
- Include light + dark `@Preview` annotations for major composables
- File header: `// Created by [Name] on DD/MM/YY`

### Koin DI Setup
```kotlin
// QRQanvasApplication.kt
class QRQanvasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@QRQanvasApplication)
            modules(CommonsModule, QRQanvasModule)
        }
    }
}

// QRQanvasModule.kt — register with singleOf, viewModelOf
val QRQanvasModule = module {
    single { QRQanvasDatabase.create(androidContext()) }
    singleOf(::SomeRepository)
    viewModelOf(::SomeVM)
}
```

### Activity Setup
```kotlin
class QRQanvasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()  // BEFORE super.onCreate()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRQanvasTheme { QRQanvasApp() }
        }
    }
}
```

### Navigation (Type-Safe Routes)
```kotlin
object Route {
    @Serializable data object Home
    @Serializable data object Creator
    @Serializable data class Detail(val qrCodeId: Long)
    @Serializable data object Scanner
    @Serializable data object Settings
    @Serializable data object DevTools
}
```

### Room Database Pattern
```kotlin
@Database(entities = [...], version = DB_VERSION)
abstract class QRQanvasDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "QRQanvasDatabase"
        const val DB_VERSION = 1
        fun create(appContext: Context): QRQanvasDatabase =
            Room.databaseBuilder(appContext, QRQanvasDatabase::class.java, DB_NAME).build()
    }
    abstract fun qrCodeDao(): QRCodeDao
}
```

### Proto DataStore (if needed for settings)
- Proto file goes in `src/main/proto/`
- build.gradle.kts needs `protobuf` plugin and protoc config (not yet added — must be added when implementing)
- See wallmatic's `config.proto` for pattern

## Core Commons Utilities Available

Import from `com.axondragonscale.commons`:

| Utility | Usage |
|---------|-------|
| `Logger` | `TAG.logD { "message" }`, `this.logD { }` |
| `Result<S, F>` | `Result.Success(data)` / `Result.Failure(error)` with `.onSuccess {}` / `.onFailure {}` |
| `PreferenceExt` | `dataStore.setting(key, default)`, `.enumSetting()`, `.serializedSetting()` |
| `ModifierExt` | `Modifier.thenIf(cond) { }`, `.thenIfElse()`, `.thenIfNotNull()` |
| `PermissionExt` | `context.getDangerousPermissions()`, `context.isPermissionGranted()` |
| `ContextExt` | `context.clearAppData()` |
| `DevToolsScreen` | Built-in kill/clear/permissions tools + custom tools via `buildDevTools { }` DSL |
| `CommonsModule` | Koin module providing `Json` and `HttpClient` instances |

## QR Generation with ZXing — Key Technical Notes

- `QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)` returns a `BitMatrix`
- The `BitMatrix` tells you which modules (dots) are on/off — rendering is fully custom
- Error correction level (`EncodeHintType.ERROR_CORRECTION`) should be `ErrorCorrectionLevel.H` (30%) when embedding logos to maintain scannability
- `EncodeHintType.MARGIN` controls quiet zone (set to 0 for custom margin handling)
- Custom rendering: iterate the BitMatrix and draw each module as circles, rounded squares, diamonds, etc. on a `Canvas` or via Compose `Canvas` composable
- Finder patterns (the 3 large corner squares) need special handling — they should be drawn separately with their own style

## QR Scanning with CameraX + ML Kit — Key Technical Notes

- Use `CameraX` with `ImageAnalysis` use case
- ML Kit's `BarcodeScanner` processes frames from `ImageAnalysis.Analyzer`
- Must handle camera permission with Accompanist's `rememberPermissionState`
- `camerax-mlkit-vision` provides `MlKitAnalyzer` for direct integration
- Guava (`libs.guava`) is included for ListenableFuture support required by CameraX

## Manifest Requirements (Not Yet Added)

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

The Application class must be registered:
```xml
<application android:name=".QRQanvasApplication" ... >
```

## What Needs to Be Built (Everything)

The module currently only has:
- `QRQanvasActivity.kt` — default template, needs splash screen + proper setup
- `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` — default template colors, need app-specific theming
- `AndroidManifest.xml` — missing Application class, camera permission
- `res/values/strings.xml` — only has `app_name`
- App icons exist (mipmap + drawable foreground/background)

Everything else — Application class, Koin module, database, Room entities, DAOs, repositories, all UI screens, navigation, QR generation engine, QR scanning, settings — needs to be created from scratch.