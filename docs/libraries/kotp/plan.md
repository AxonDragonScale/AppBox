---
title: "Kotp — OTP library plan"
status: in-progress
summary: "Plan to extract HOTP/TOTP generation, URI parsing and token models out of Topty into a standalone KMP library."
read_when: "Building the kotp library or changing Topty's OTP core."
---

# kotp — KMP OTP Library Plan

## Overview

Extract the OTP generation logic from `:apps:topty` into a standalone Kotlin Multiplatform library
named `kotp` (`:libraries:kotp`). The library handles HMAC-based OTP generation (HOTP/TOTP), URI
parsing/generation, and the related data models. It has no Android or app-layer dependencies.

## Decisions Log

| # | Decision | Choice | Reason |
|---|---|---|---|
| 1 | Target platforms | Truly KMP (JVM, Android, iOS, JS/Wasm) | `expect/actual` for HMAC primitive |
| 2 | Primary model name | `Token` | Short, owns the namespace; app's Room entity renamed to `TokenEntity` |
| 3 | URI parsing dependency | `ktor-http` (commonMain) | Already in the project; provides `Url`, `URLBuilder`, `encodeURLParameter` |
| 4 | `Token` secret | Plaintext only | Encryption is an app concern |
| 5 | Logging | Minimal `println`-based logger | No framework dependency; can be upgraded later |
| 6 | `TokenUriHandler` | Included in library | URI parse/generate is core OTP functionality |
| 7 | Library name | `kotp` | Short, recognizable as Kotlin OTP, fits `:libraries:kotp` and `com.axondragonscale.kotp` |

---

## Scope

### In scope

- Data models: `Token`, `HashAlgo`, `Protocol`, `HOTPCode`, `TOTPCode`
- Core generators: `HMACGenerator`, `HOTPGenerator`, `TOTPGenerator`
- URI handling: `TokenUriHandler` (parse + generate)
- Internal utility: `Base32` decoder (pure Kotlin, replaces Guava)

### Out of scope

- Encryption / secret storage (app concern)
- Database / persistence (app concern)
- UI, ViewModel, DI wiring (app concern)

---

## Android Dependency Audit & Resolution

| Current Dependency | Used In | Resolution |
|---|---|---|
| `javax.crypto.Mac` / `SecretKeySpec` | `HMACGenerator` | `expect/actual` — `javax.crypto` on JVM/Android, platform crypto elsewhere |
| `com.google.common.io.BaseEncoding` | `HMACGenerator` | Replace with pure Kotlin `Base32` decoder in `commonMain` |
| `java.nio.ByteBuffer` | `HMACGenerator` | Replace with pure Kotlin byte manipulation (bit shifting on `ByteArray`) |
| `androidx.core.net.toUri` | `TokenUriHandler` | Replace with `ktor-http` `Url` parser (available in `commonMain`) |
| `io.ktor.http.encodeURLParameter` | `TokenUriHandler` | Already `ktor-http` — works in `commonMain` unchanged |
| `com.axondragonscale.commons.logD` | `TOTPGenerator`, `TokenUriHandler` | Replace with minimal internal `KotpLogger` using `println` |

---

## Architecture

### expect/actual boundary

The only platform-specific code is the HMAC primitive. Everything else is pure `commonMain`.

```
// commonMain
internal expect fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray

// jvmMain + androidMain
internal actual fun hmac(algorithm: HmacAlgorithm, key: ByteArray, data: ByteArray): ByteArray {
    val mac = Mac.getInstance(algorithm.hmacName)
    mac.init(SecretKeySpec(key, "RAW"))
    return mac.doFinal(data)
}

// iosMain — CommonCrypto / CryptoKit
// jsMain  — SubtleCrypto (Web Crypto API)
```

`HmacAlgorithm` is an internal enum mapping to platform algorithm names:

```
internal enum class HmacAlgorithm { SHA1, SHA256, SHA512 }
```

### Data Models (`commonMain`)

```
// HashAlgo.kt
enum class HashAlgo { SHA1, SHA256, SHA512 }

// Protocol.kt
enum class Protocol(val code: String) {
    TOTP("totp"),
    HOTP("hotp"),
}

// Token.kt — plain data, plaintext secret, no DB annotations
data class Token(
    val name: String,
    val issuer: String,
    val secret: String,       // plaintext Base32
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val digits: Int,          // 6–8
    val period: Int,          // seconds, TOTP only (default 30)
    val counter: Long,        // HOTP only (default 0)
)

// HOTPCode.kt
data class HOTPCode(
    val code: String,
    val token: Token,
)

// TOTPCode.kt
data class TOTPCode(
    val code: String,
    val token: Token,
    val periodStart: Long,    // epoch ms
    val periodEnd: Long,      // epoch ms
)
```

### Core Generators (`commonMain`)

`HMACGenerator`, `HOTPGenerator`, `TOTPGenerator` are pure `commonMain`. They take `Token` directly
(no Room entity). `TOTPGenerator` uses `kotlin.time` / `Clock` for current time — no
`System.currentTimeMillis()`.

`TOTPGenerator` accepts an injectable `kotlinx.datetime.Clock` for testability, defaulting to
`Clock.System`.

### `TokenUriHandler` (`commonMain`)

Uses `ktor-http` for URI parsing and encoding. No `androidx` dependency.
Constants (`Scheme.OTPAUTH`, `UriParam.*`) move into the library as internal constants or a
companion object on `TokenUriHandler`.

### Logging (`commonMain`)

```
// internal, not part of public API
internal object KotpLogger {
    var enabled: Boolean = false
    fun d(tag: String, message: () -> String) {
        if (enabled) println("D/$tag: ${message()}")
    }
}
```

Internal only. Can be upgraded to an injectable interface later without a breaking API change.
No public `Kotp.init()` entry point — logging stays internal.

### `Base32` (`commonMain/internal`)

Pure Kotlin RFC 4648 Base32 decoder. Replaces Guava's `BaseEncoding.base32()`. Not part of the
public API (`internal`).

---

## Proposed File Structure

```
libraries/kotp/
└── src/
    ├── commonMain/kotlin/com/axondragonscale/kotp/
    │   ├── model/
    │   │   ├── Token.kt
    │   │   ├── HashAlgo.kt
    │   │   ├── Protocol.kt
    │   │   ├── HOTPCode.kt
    │   │   └── TOTPCode.kt
    │   ├── core/
    │   │   ├── HMACGenerator.kt        # uses internal expect hmac()
    │   │   ├── HOTPGenerator.kt
    │   │   ├── TOTPGenerator.kt
    │   │   └── TokenUriHandler.kt
    │   └── internal/
    │       ├── Base32.kt               # pure Kotlin Base32 decoder
    │       ├── HmacAlgorithm.kt        # internal enum for expect/actual
    │       ├── HmacProvider.kt         # expect fun hmac(...)
    │       └── KotpLogger.kt
    ├── jvmMain/kotlin/.../HmacProvider.kt     # actual — javax.crypto
    ├── androidMain/kotlin/.../HmacProvider.kt # actual — javax.crypto (same as JVM, or shared)
    ├── iosMain/kotlin/.../HmacProvider.kt     # actual — CommonCrypto
    └── jsMain/kotlin/.../HmacProvider.kt      # actual — SubtleCrypto
```

> **Note on jvmMain vs androidMain:** `javax.crypto` is available on both. These two actuals can
> share implementation via a `jvmCommon` intermediate source set, or just duplicate the two-liner.
> Decide at implementation time.

---

## Migration Plan (`:apps:topty`)

1. Add `:libraries:kotp` dependency to `:apps:topty`
2. Rename `Token` (Room entity) → `TokenEntity`
3. Add mapper functions `TokenEntity.toToken(): Token` and `Token.toTokenEntity(): TokenEntity`
4. Replace usages of `HMACGenerator`, `HOTPGenerator`, `TOTPGenerator`, `TokenUriHandler` with
   the library versions
5. Remove `HashAlgo`, `Protocol`, `HOTPCode`, `TOTPCode` from the app module (now in library)
6. Remove Guava `BaseEncoding` dependency from `:apps:topty`

---

## Open Questions

| # | Question | Default if not resolved |
|---|---|---|
| 1 | ~~Injectable `Clock` in `TOTPGenerator`?~~ | **Resolved:** Yes — `Clock` param defaulting to `Clock.System` |
| 2 | Share jvmMain/androidMain HMAC actual via intermediate source set? | Duplicate the two-liner |
| 3 | ~~`Kotp.init(debug: Boolean)` public entry point?~~ | **Resolved:** No — logging stays internal |

---

## Related Context

- [AppBox Root Context](../../context.md)
- [Topty App Context](../../apps/topty/context.md)
- [TLV Library Context](../tlv/context.md) — reference for standalone KMP library structure