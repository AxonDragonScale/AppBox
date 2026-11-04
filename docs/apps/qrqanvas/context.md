> Context: [Main](../../context.md) > QRQanvas

# QRQanvas - QR Code Generator & Scanner

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

## Planned Architecture

Follow the same patterns as wallmatic/topty/jest:

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

## Navigation Routes (Planned)

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

## Koin DI Setup (Planned)

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

## Activity Setup (Planned)

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

## Room Database Pattern

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

## What Currently Exists

- `QRQanvasActivity.kt` — default template, needs splash screen + proper setup
- `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` — default template colors, need app-specific theming
- `AndroidManifest.xml` — missing Application class, camera permission
- `res/values/strings.xml` — only has `app_name`
- App icons exist (mipmap + drawable foreground/background)

Everything else — Application class, Koin module, database, Room entities, DAOs, repositories, all UI screens, navigation, QR generation engine, QR scanning, settings — needs to be created from scratch.

## Build Info

- **Namespace**: `com.axondragonscale.qrqanvas`
- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36
- **Java/Kotlin**: JVM 21
- **Convention Plugin**: `id("android-app-convention")`

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Commons Module](../../core/commons/context.md) — Shared utilities available to all apps (Logger, Result, DevToolsScreen, etc.)
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming conventions
- [Build System](../../build-system.md) — Convention plugins, version catalog, build commands