---
title: "Core common"
status: current
summary: "The zero-UI, zero-network KMP foundation: Logger, Result, CommonJson, DateTimeExt, Flow helpers and AppInfo."
read_when: "Adding a pure Kotlin helper, or wiring logging, JSON or app metadata."
---

> Context: [Main](../../context.md) > [Core Modules](../context.md) > Core Common

# Core Common — `:core:common`

## Overview

`:core:common` is the **zero-UI, zero-network foundation module**. It holds only pure Kotlin
utilities and platform log/metadata `expect`/`actual`s, so any module — including lightweight
libraries — can depend on it without pulling in Compose, Ktor, Room, Koin, DataStore, ZXing or
Chucker.

**Package**: `com.axondragonscale.commons` (unchanged during the migration from `:core:commons`)

**Targets**: Android, JVM, iOS (arm64, simulatorArm64) — the plain KMP convention, not the Compose one.

## Dependencies

All are `api` because public signatures expose them:

```kotlin
api(libs.kotlinx.coroutines.core)      // Flow<T>
api(libs.kotlinx.serialization.json)   // Json
api(libs.kotlinx.datetime)             // LocalDate, LocalTime, Instant
```

Deliberately absent: Compose, Koin, Ktor, Room, DataStore, lifecycle, ZXing, Chucker.

## Contents

### Logger — `Logger.kt` (+ platform actuals)

Flexible logging system with extension functions for any object. Uses `expect`/`actual` for
platform log dispatch.

```kotlin
private const val TAG = "MyClass"
TAG.logD { "Debug message" }
TAG.logE { "Error message" }

this.logD { "Debug message from ${this::class.simpleName}" }

// Levels: logD, logI, logW, logE, logV, logA, plus log for a Throwable
Logger.isLoggingEnabled = true  // Disable all logging
Logger.usePrintlnLogs = false   // Force println output on all platforms
```

Android dispatches to `android.util.Log`; JVM/iOS dispatch to `println`.

### Result — `Result.kt`

Custom type-safe result wrapper (not `kotlin.Result`):

```kotlin
sealed interface Result<out S, out F> {
    data class Success<S>(val data: S) : Result<S, Nothing>
    data class Failure<F>(val data: F) : Result<Nothing, F>
}
```

Key combinators (`onSuccess`, `onFailure`, `onComplete`, `fold`, `map`, `mapFailure`, `recover`,
`getOrElse`, `getOrDefault`) are top-level `inline` extensions — import them explicitly:

```kotlin
import com.axondragonscale.commons.onSuccess
import com.axondragonscale.commons.onFailure
import com.axondragonscale.commons.onComplete
```

Exception bridge: `runCatching { ... }.toResult { it.message }`. `CancellationException` always
propagates.

`catching` — `Catching.kt` — is the logged bridge for unexpected exceptions:

```kotlin
val result = catching { readConfig() }          // tag = the receiver's class name
val tagged = "Sync".catching { syncAlbums() }   // explicit tag

result
    .onFailure { e -> showError(e) }
    .onComplete { close() }
```

It catches `Exception` (not `Throwable`), rethrows `CancellationException`, and logs the exception
under the receiver's tag. Chain `onFailure` for extra handling and `onComplete` for result cleanup.
Use `runCatching { ... }.toResult { ... }` instead when a failure is expected and should not be logged.

For work that must run on every path, pass `onFinally`; it runs on success, failure, cancellation
and `Error`, before the exception propagates:

```kotlin
catching(onFinally = { cursor.close() }) { read() }
```

When the cleanup itself suspends, use `catchingSuspend`; it is a suspend inline function and takes
a suspend `onFinally`:

```kotlin
catchingSuspend(onFinally = { prefs.set(null) }) { write() }
```

For a resource created inside the block, use `catchingFinally` — `onComplete` is skipped when
`catching` rethrows cancellation or an `Error`:

```kotlin
catchingFinally { scope ->
    val bitmap = decode()
    scope.onFinally { bitmap.recycle() }
    apply(bitmap)
}
```

The registered cleanup runs on success, failure, cancellation and `Error`, like a `finally` block.

For coroutines, use `launchCatching` or `withContextCatching`:

```kotlin
scope.launchCatching { refresh() }
scope.launchCatching(onFinally = { close() }) { refresh() }

val result = withContextCatching(Dispatchers.IO) { read() }
val cleaned = withContextCatching(Dispatchers.IO, onFinally = { close() }) { read() }
```

`launchCatching` returns a `Job`; a logged failure completes it normally. `withContextCatching` is an
`Any` extension returning `Result<T, Exception>`. Both log failures and run `onFinally` on every
path, including cancellation and `Error`; cancellation and `Error`s propagate.

### Serialization — `Serialization.kt`

```kotlin
val CommonJson = createJson()

internal fun createJson() = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
```

There is no Koin binding here; consumers that inject `Json` can provide `CommonJson` in their own
module.

### DateTime — `DateTimeExt.kt`

`kotlinx.datetime` extensions used by the apps: `today()`, `startOfWeek`, `endOfWeek`,
`startOfMonth`, `endOfMonth`, `formatShort()`, `formatMonthYear()`, `daysInMonth()`,
`daysBetween()`, `formatAmPm()`, `now()`, `toEpochMillis()`, `formatDateTime()`, `formatTime()`,
plus `Month.abbreviated/displayName`, `DayOfWeek.abbreviated/letter` and `hourLabel()`.

### Flow — `FlowExt.kt`

```kotlin
// No-op collection — triggers the flow without handling emissions
suspend fun <T> Flow<T>.collect() = this.collect {}

// 6- and 7-flow combine overloads (the stdlib stops at 5)
fun <T1, T2, T3, T4, T5, T6, R> combine(...): Flow<R>
fun <T1, T2, T3, T4, T5, T6, T7, R> combine(...): Flow<R>
```

`collectWithLifecycle` is **not** here — it is a Compose helper and lives in `:core:ui`
(`LifecycleFlowExt.kt`).

### AppInfo — `AppInfo.kt` (+ platform actuals)

```kotlin
expect object AppInfo {
    val versionName: String
    val versionCode: Int
    val packageName: String
}
```

Android: call `AppInfo.init(context)` in `Application.onCreate()` before Koin setup.
Desktop: call `AppInfo.init(versionName, versionCode, packageName)` before starting the app.
iOS: reads from `NSBundle` automatically.

## Conventions

- **Keep this module dependency-free** beyond the three kotlinx artifacts above. A new feature that
  needs Compose, Ktor, Room or DataStore belongs in its own module.
- Use `api` when a type appears in a public signature; otherwise `implementation`.
- Prefer adding a tiny utility here over duplicating it in multiple apps.

## Related Context

- [Main Context](../../context.md) — Project overview, module index
- [Core Modules Overview](../context.md) — Android, UI, DevTools, DataStore, Room, Network, QR
- [Coding Conventions](../../coding-conventions.md)
