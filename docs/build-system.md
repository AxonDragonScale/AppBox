> Context: [Main](./context.md) > Build System

# AppBox - Build System

## Convention Plugins

Located in `build-logic/src/main/kotlin/`:

### `android-app-convention.gradle.kts`

Applied to Android application modules (including the thin shells of Compose Multiplatform apps) via `id("android-app-convention")`:

- Android Application plugin — AGP 9 has built-in Kotlin support, so `org.jetbrains.kotlin.android` is not applied
- Kotlin Compose compiler plugin
- Signing configuration (shared keystore from `keystore/`)
- Common SDK versions: `minSdk = 31`, `targetSdk = 37`, `compileSdk = 37`
- `compileOptions` targeting JVM 21
- BuildConfig generation enabled
- Compose features enabled

### `android-library-convention.gradle.kts`

Applied to Android-only library modules via `id("android-library-convention")`:

- Android Library plugin (built-in Kotlin)
- Common SDK versions and compile options (same as `android-app-convention`)

It is Android-only (not KMP) and is currently unused — `:core:commons` uses `kmp-library-convention` instead.

### `kmp-library-convention.gradle.kts`

Applied to Kotlin Multiplatform library modules via `id("kmp-library-convention")`:

- Kotlin Multiplatform plugin
- `com.android.kotlin.multiplatform.library` — the AGP 9 KMP Android target (`androidTarget()` and the `com.android.library` plugin are no longer used)
- Compose Multiplatform plugin + Kotlin Compose compiler plugin
- Targets: `android` (JVM 21, configured via `kotlin { android { … } }`), `jvm`, `iosArm64`, `iosSimulatorArm64`
- Android SDK versions, Android resources, and published consumer keep rules
- Single-variant Android target: no build types or product flavors

Currently used by: `:core:commons`

### `kmp-app-convention.gradle.kts`

Applied to Compose Multiplatform application modules via `id("kmp-app-convention")`:

- Kotlin Multiplatform plugin + `com.android.kotlin.multiplatform.library`
- Compose Multiplatform plugin + Kotlin Compose compiler plugin
- Android target (`kotlin { android { … } }`, JVM 21) with Android resources enabled
- Targets: `jvm`, `iosArm64`, `iosSimulatorArm64`
- `jvmToolchain(21)`

It intentionally contains **no dependency declarations** — each app module owns its own `sourceSets` dependencies, KSP processors, and tooling runtime classpath.

Used by `:apps:jest:jestComposeApp` and `:apps:limitless:limitlessComposeApp`.

## Version Catalog

Located at `gradle/libs.versions.toml`. Defines all dependency versions centrally.

Key patterns:
- **BOM-based**: Compose BOM, Koin BOM — never specify individual versions for these
- **Bundles**: `libs.bundles.compose` (AndroidX), `libs.bundles.compose.mp` (Compose Multiplatform), `libs.bundles.koin` (Android), `libs.bundles.koin.common` (KMP), `libs.bundles.ktor`, `libs.bundles.ktor.common`, `libs.bundles.kotlinx`, `libs.bundles.room.kmp`, `libs.bundles.lifecycle.compose`, `libs.bundles.coroutines`, `libs.bundles.coil`, `libs.bundles.camerax`, `libs.bundles.datastore`
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

### Adding a New Android App Module

1. Create directory under `apps/`
2. Create `build.gradle.kts`:
   ```kotlin
   plugins {
       id("android-app-convention")
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
4. Apply `android-app-convention` plugin — it handles signing, SDK versions, Compose setup

### Adding a New KMP App Module

AGP 9 does not allow `com.android.application` in a Kotlin Multiplatform module, so a multiplatform app is three modules:

1. Create the app directory:
   - `<app>ComposeApp/` — KMP + Compose library (`id("kmp-app-convention")`)
   - `<app>Android/` — Android application shell (`id("android-app-convention")`)
   - `<app>IosApp/` — Xcode project (not a Gradle module)
2. The Compose module keeps only app-specific config:
   ```kotlin
   plugins {
       id("kmp-app-convention")
       alias(libs.plugins.ksp)
       alias(libs.plugins.room)
       alias(libs.plugins.kotlin.serialization)
   }

   kotlin {
       android {
           namespace = "com.axondragonscale.myapp"
       }
   }
   ```
3. The Android shell owns `applicationId` and the manifest, and depends on the Compose module:
   ```kotlin
   plugins {
       id("android-app-convention")
   }

   android {
       namespace = "com.axondragonscale.myapp.android"
       defaultConfig {
           applicationId = "com.axondragonscale.myapp"
           versionCode = 1
           versionName = "1.0.0"
       }
   }

   dependencies {
       implementation(project(":apps:myapp:myappComposeApp"))
   }
   ```
4. Register both Gradle modules in `settings.gradle.kts`:
   ```kotlin
   include(":apps:myapp:myappComposeApp")
   include(":apps:myapp:myappAndroid")
   ```
5. Activities, receivers, and services live in the Compose module's `androidMain`; the shell manifest references them by fully qualified name.

### Adding a New Library Module

1. Create directory under `core/`
2. Create `build.gradle.kts`:
   - Use `id("android-library-convention")` for Android-only library modules
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
./gradlew :apps:jest:jestAndroid:assembleDebug
./gradlew :apps:wallmatic:assembleDebug
./gradlew :apps:topty:assembleDebug
./gradlew :apps:limitless:limitlessAndroid:assembleDebug
./gradlew :apps:npinner:assembleDebug
./gradlew :apps:qrqanvas:assembleDebug

# Run tests
./gradlew test

# Lint checks
./gradlew lint
```

## Signing

All apps share a common keystore located in `keystore/`. The signing configuration is applied automatically by `android-app-convention`.

## Related Context

- [Main Context](./context.md) — Project overview, tech stack, architecture patterns
- [Coding Conventions](./coding-conventions.md) — Code patterns and style guidelines
- [Commons Module](./core/commons/context.md) — Shared utilities and DI module