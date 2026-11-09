---
title: "Build system"
status: current
summary: "Convention plugins in build-logic, the version catalog, signing, and the build commands for each module type."
read_when: "Changing Gradle files, adding a dependency, or configuring signing and plugins."
---

> Context: [Main](./context.md) > Build System

# AppBox - Build System

## Convention Plugins

Located in `build-logic/src/main/kotlin/`:

### `android-app-convention.gradle.kts`

Applied to Android application modules (including the thin shells of Compose Multiplatform apps) via `alias(libs.plugins.android.app.convention)`:

- Android Application plugin — AGP 9 has built-in Kotlin support, so `org.jetbrains.kotlin.android` is not applied
- Kotlin Compose compiler plugin
- Signing configuration (shared keystore from `keystore/`)
- Common SDK versions: `minSdk = 31`, `targetSdk = 37`, `compileSdk = 37`
- `compileOptions` targeting JVM 21
- BuildConfig generation enabled
- Compose features enabled

### `android-library-convention.gradle.kts`

Applied to Android-only library modules via `alias(libs.plugins.android.library.convention)`:

- Android Library plugin (built-in Kotlin)
- Common SDK versions and compile options (same as `android-app-convention`)

It is Android-only (not KMP). Used by `:core:android`, `:libraries:qr` and `:libraries:logcat-viewer`.

### `kmp-library-convention.gradle.kts`

Applied to Kotlin Multiplatform library modules via `alias(libs.plugins.kmp.library.convention)`:

- Kotlin Multiplatform plugin
- `com.android.kotlin.multiplatform.library` — the AGP 9 KMP Android target (`androidTarget()` and the `com.android.library` plugin are no longer used)
- Compose Multiplatform plugin + Kotlin Compose compiler plugin
- Targets: `android` (JVM 21, configured via `kotlin { android { … } }`), `jvm`, `iosArm64`, `iosSimulatorArm64`
- Android SDK versions, Android resources, and published consumer keep rules
- Single-variant Android target: no build types or product flavors

Currently used by: `:core:ui`, `:core:devtools`, `:apps:jest:jestComposeApp`, `:apps:limitless:limitlessComposeApp`

### `kmp-plain-library-convention.gradle.kts`

Applied to KMP library modules that must **not** pull in Compose, via `alias(libs.plugins.kmp.plain.library.convention)`:

- Kotlin Multiplatform plugin + `com.android.kotlin.multiplatform.library`
- `kotlin { android { … } }` target with JVM 21, Android SDK/keep-rule config
- Targets: `jvm`, `iosArm64`, `iosSimulatorArm64`
- No Compose Multiplatform or Compose compiler plugin

Currently used by: `:core:common`, `:core:datastore`, `:core:room`, `:core:network`

### `kmp-app-convention.gradle.kts`

Applied to Compose Multiplatform application modules via `alias(libs.plugins.kmp.app.convention)`:

- Kotlin Multiplatform plugin + `com.android.kotlin.multiplatform.library`
- Compose Multiplatform plugin + Kotlin Compose compiler plugin
- Android target (`kotlin { android { … } }`, JVM 21) with Android resources enabled
- Targets: `jvm`, `iosArm64`, `iosSimulatorArm64`
- `jvmToolchain(21)`

It intentionally contains **no dependency declarations** — each app module owns its own `sourceSets` dependencies, KSP processors, and tooling runtime classpath.

Used by `:apps:jest:jestComposeApp` and `:apps:limitless:limitlessComposeApp`.

### `compiler-args-convention.gradle.kts`

Applied by the **root project only** (`alias(libs.plugins.compiler.args.convention)` in `build.gradle.kts`). It adds the shared compiler flags to every KMP module through a `subprojects` hook — including `:libraries:tlv` and `:libraries:bitops`, which apply the Kotlin Multiplatform plugin directly.

### Shared compiler flags

Defined once in `AppBoxCompilerArgs.optIns` (`build-logic/src/main/kotlin/AppBoxCompilerArgs.kt`); add new opt-ins there, never in a module build file. Android modules get them from the `android-*` convention plugins, KMP modules from `compiler-args-convention`.

## Version Catalog

Located at `gradle/libs.versions.toml`. Defines all dependency versions centrally.

Key patterns:
- **Plugins**: every plugin id lives in `[plugins]` — external ones with a `version.ref`, the build-logic convention plugins without a version. Module build files apply them with `alias(libs.plugins.…)`, never a raw `id("…")`
- **BOM-based**: Compose BOM, Koin BOM — never specify individual versions for these
- **Bundles**: `libs.bundles.compose` (AndroidX), `libs.bundles.compose.mp` (Compose Multiplatform), `libs.bundles.koin` (Android), `libs.bundles.koin.common` (KMP), `libs.bundles.ktor`, `libs.bundles.ktor.common`, `libs.bundles.kotlinx`, `libs.bundles.room.kmp`, `libs.bundles.lifecycle.compose`, `libs.bundles.coroutines`, `libs.bundles.coil`, `libs.bundles.camerax`, `libs.bundles.datastore`
- **Individual**: `libs.androidx.room.runtime`, `libs.androidx.room.compiler`, etc.

## Dependency Injection Setup

Each app has its own `Application` class that initializes Koin with only the core modules it needs:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(
                NetworkModule,  // Only if the app makes HTTP calls
                MyAppModule,    // App-specific module
            )
        }
    }
}
```

There is no central `CommonsModule`. DataStore and Room Android actuals still resolve `Context`
through Koin, which `androidContext(...)` already provides.

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
       alias(libs.plugins.android.app.convention)
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
       implementation(projects.core.common)
       implementation(projects.core.ui)
       // App-specific dependencies using version catalog
   }
   ```
3. Register in `settings.gradle.kts`: `include(":apps:myapp")`
4. Apply `android-app-convention` plugin — it handles signing, SDK versions, Compose setup

### Adding a New KMP App Module

AGP 9 does not allow `com.android.application` in a Kotlin Multiplatform module, so a multiplatform app is three modules:

1. Create the app directory:
   - `<app>ComposeApp/` — KMP + Compose library (`libs.plugins.kmp.app.convention`)
   - `<app>Android/` — Android application shell (`libs.plugins.android.app.convention`)
   - `<app>IosApp/` — Xcode project (not a Gradle module)
2. The Compose module keeps only app-specific config:
   ```kotlin
   plugins {
       alias(libs.plugins.kmp.app.convention)
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
       alias(libs.plugins.android.app.convention)
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

### Choosing a Convention Plugin

| The module is… | Plugin | Copy this module |
|---|---|---|
| an Android app, or the thin shell of a Compose Multiplatform app | `alias(libs.plugins.android.app.convention)` | `:apps:npinner` |
| a Compose Multiplatform app shell | `alias(libs.plugins.kmp.app.convention)` | `:apps:jest:jestComposeApp` |
| a KMP library that has Compose UI | `alias(libs.plugins.kmp.library.convention)` | `:core:ui` |
| a KMP library with no Compose (coroutines, Ktor, Room, DataStore) | `alias(libs.plugins.kmp.plain.library.convention)` | `:core:network` |
| an Android-only library | `alias(libs.plugins.android.library.convention)` | `:core:android` |

Wrong choice is obvious in the build file: if you reach for `kmp.library.convention` on a module with
no UI, you have pulled Compose Multiplatform and the Compose compiler into it for nothing.

Never apply `compiler-args-convention` in a module — the root build applies it to every KMP subproject.

### Adding a New Module

1. Create the directory under `core/` (or `libraries/` if it is independently publishable).
2. Create `build.gradle.kts` with the plugin from the table above.
3. Register in `settings.gradle.kts` — `include(":core:media")`. Typesafe accessors are on, so
   consumers write `implementation(projects.core.media)`.
4. Declare the Android `namespace` and the Kotlin `package` (they differ by design, see below).
5. Copy `consumer-rules.pro` / `proguard-rules.pro` stubs when the plugin references them.
6. Same change, docs: `docs/core/<module>/context.md`, a row in the Core modules table of
   `docs/context.md`, a line in `docs/core/context.md`, and an entry in that plugin's
   "Currently used by" list.

**Namespace vs package.** Both are convention, matched from the neighbouring modules:

- Android `namespace` — `com.axondragonscale.core.<module>`
- Kotlin `package` — `com.axondragonscale.commons.<module>` (plain `com.axondragonscale.commons` for `:core:common`)

**Signing.** Only apps are signed, by `android-app-convention`. Library modules need no signing config.

### Adding a Dependency

1. `gradle/libs.versions.toml` → `[versions]`: add a camelCase key.
2. Same file → `[libraries]`: add a kebab-case alias with `version.ref`, or omit the version when a
   BOM or bundle supplies it.
3. Same file → `[bundles]`: only when several modules take the same set.
4. Consuming `build.gradle.kts`: `implementation(libs.alias.here)`.
5. KMP modules: put it in the right source set — `commonMain` if the artifact ships iOS klibs,
   `androidMain` if it is Android-only.
6. Annotation processors: `ksp(...)`, never `kapt(...)`.
7. A Gradle plugin goes in `[plugins]`, applied with `alias(libs.plugins.x)`.

Never write a raw `"group:artifact:version"` in a module build file.

`libs.bundles.ktor` and `libs.bundles.coroutines` both include Android-only artifacts, so they are
wrong for `commonMain` — declare the individual `-core` artifacts there instead.

### Compose Versions

Two stacks. Do not mix them.

- **Android modules** (`androidx.compose.*`) take versions from `composeBom` in the catalog. No
  convention plugin applies the BOM — each Android module declares
  `implementation(platform(libs.androidx.compose.bom))` itself. Add that line before using a
  versionless `androidx-compose-*` alias, or the build fails with "Could not find … required version".
- **Compose Multiplatform modules** (`:apps:jest`, `:apps:limitless`) take versions from the
  `compose-multiplatform` plugin and `libs.bundles.compose.mp`, and never use the AndroidX BOM.

Bump the BOM to move the whole Compose set at once. Artifacts outside it — `material3-adaptive`,
`material-icons-extended`, `navigation-compose` — are pinned individually in the catalog.

### Project Accessors

Use typesafe project accessors for inter-module dependencies:
```kotlin
// Preferred
implementation(projects.core.common)
implementation(projects.core.ui)

// Avoid
implementation(project(":core:ui"))
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

# Library test suites — apps have no tests, see Coding Conventions
./gradlew :libraries:tlv:jvmTest :libraries:bitops:jvmTest

# Lint checks
./gradlew lint
```

## Signing

All apps share a common keystore located in `keystore/`. The signing configuration is applied automatically by `android-app-convention`.

## Related Context

- [Main Context](./context.md) — Project overview, tech stack, architecture patterns
- [Coding Conventions](./coding-conventions.md) — Code patterns and style guidelines
- [Core Modules Overview](./core/context.md) — Android, UI, DevTools, DataStore, Room, Network, QR
- [Core Common Module](./core/common/context.md) — Pure Kotlin utilities