> Context: [Main](./context.md) > Build System

# AppBox - Build System

## Convention Plugins

Located in `build-logic/src/main/kotlin/`:

### `app-convention.gradle.kts`

Applied to all app modules via `id("app-convention")`:

- Android Application plugin
- Kotlin Android + Compose compiler
- Signing configuration (shared keystore from `keystore/`)
- Common SDK versions: `minSdk = 31`, `targetSdk = 36`, `compileSdk = 36`
- `compileOptions` targeting JVM 21
- BuildConfig generation enabled
- Compose features enabled

### `library-convention.gradle.kts`

Applied to Android-only library modules via `id("library-convention")`:

- Android Library plugin
- Kotlin Android
- Common SDK versions and compile options (same as app-convention)

### `kmp-library-convention.gradle.kts`

Applied to Kotlin Multiplatform library modules via `id("kmp-library-convention")`:

- Kotlin Multiplatform plugin
- Android Library plugin
- Compose Multiplatform plugin + Kotlin Compose compiler plugin
- Targets: `androidTarget` (JVM 21), `jvm`, `iosX64`, `iosArm64`, `iosSimulatorArm64`
- Android SDK versions and compile options (same as other conventions)
- Compose `buildFeatures` enabled for Android target

Currently used by: `:core:commons`

## Version Catalog

Located at `gradle/libs.versions.toml`. Defines all dependency versions centrally.

Key patterns:
- **BOM-based**: Compose BOM, Koin BOM — never specify individual versions for these
- **Bundles**: `libs.bundles.compose`, `libs.bundles.koin`, `libs.bundles.ktor`, `libs.bundles.coroutines`, `libs.bundles.coil`, `libs.bundles.camerax`, `libs.bundles.datastore`
- **Individual**: `libs.androidx.room.runtime`, `libs.androidx.room.compiler`, etc.

## Dependency Injection Setup

Each app has its own `Application` class that initializes Koin with `CommonsModule` + its own module:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(
                CommonsModule,  // From :core:commons — always include
                MyAppModule,    // App-specific module
            )
        }
    }
}
```

Koin DSL conventions:
- `singleOf(::MyRepository)` for singletons
- `viewModelOf(::MyVM)` for ViewModels
- `factoryOf(::MyUseCase)` for factories
- `single { MyDatabase.create(androidContext()) }` when custom creation is needed

## Module Configuration

### Adding a New App Module

1. Create directory under `apps/`
2. Create `build.gradle.kts`:
   ```kotlin
   plugins {
       id("app-convention")
   }

   android {
       namespace = "com.axondragonscale.myapp"
       defaultConfig {
           applicationId = "com.axondragonscale.myapp"
           versionCode = 1
           versionName = "1.0.0"
       }
   }

   dependencies {
       implementation(projects.core.commons)
       // App-specific dependencies using version catalog
   }
   ```
3. Register in `settings.gradle.kts`: `include(":apps:myapp")`
4. Apply `app-convention` plugin — it handles signing, SDK versions, Compose setup

### Adding a New Library Module

1. Create directory under `core/`
2. Create `build.gradle.kts`:
   - Use `id("library-convention")` for Android-only library modules
   - Use `id("kmp-library-convention")` for Kotlin Multiplatform library modules
3. Register in `settings.gradle.kts`

### Project Accessors

Use typesafe project accessors for inter-module dependencies:
```kotlin
// Preferred
implementation(projects.core.commons)

// Avoid
implementation(project(":core:commons"))
```

## Room with KSP

Room annotation processing uses KSP, not KAPT:

```kotlin
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
```

## Proto DataStore (when needed)

For typed settings (used in wallmatic, npinner uses Preferences DataStore):

1. Add protobuf plugin and dependencies to `build.gradle.kts`
2. Place `.proto` files in `src/main/proto/`
3. Use `protobuf-kotlin-lite` for generated classes

## Building

```bash
# Build all apps
./gradlew assembleDebug

# Build specific app
./gradlew :apps:jest:assembleDebug
./gradlew :apps:wallmatic:assembleDebug
./gradlew :apps:topty:assembleDebug
./gradlew :apps:limitless:assembleDebug
./gradlew :apps:npinner:assembleDebug
./gradlew :apps:qrqanvas:assembleDebug

# Run tests
./gradlew test

# Lint checks
./gradlew lint
```

## Signing

All apps share a common keystore located in `keystore/`. The signing configuration is applied automatically by `app-convention`.

## Related Context

- [Main Context](./context.md) — Project overview, tech stack, architecture patterns
- [Coding Conventions](./coding-conventions.md) — Code patterns and style guidelines
- [Commons Module](./core/commons/context.md) — Shared utilities and DI module