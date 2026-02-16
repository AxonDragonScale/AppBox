# TLV Library - Context for Agents

## Overview

TLV (Tag-Length-Value) is a Kotlin Multiplatform library for encoding and decoding BER-TLV (Basic Encoding Rules - Tag Length Value) data structures. BER-TLV is a binary data format commonly used in smartcard protocols (ISO 7816), EMV payment cards, and NFC communication.

This library is **independently publishable** to Maven Central via the Vanniktech Maven Publish plugin. It has **no dependencies** on the rest of the AppBox project.

## Tech Stack

| Category       | Technology                          |
|----------------|-------------------------------------|
| Language       | Kotlin Multiplatform                |
| Build System   | Gradle (Kotlin DSL) + Version Catalog |
| Publishing     | Vanniktech Maven Publish            |
| Testing        | kotlin-test                         |
| Targets        | JVM, Android, iOS (x64/arm64/simulatorArm64), Linux x64 |

## Module Location

```
libraries/tlv/
```

Gradle path: `:libraries:tlv`

## Project Structure

```
libraries/tlv/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/        # All source code (KMP common)
    │   ├── Tag.kt                # Tag parsing and construction
    │   ├── Length.kt              # Length encoding/decoding
    │   ├── Value.kt              # Value wrapper
    │   ├── TLV.kt                # Combined TLV with parsing
    │   ├── TLVList.kt            # List of TLV elements
    │   └── Utils.kt              # Byte/ByteArray conversion utilities
    └── commonTest/kotlin/        # Tests
        ├── TagTest.kt            # Tag validation and construction tests
        ├── LengthTest.kt         # Length encoding/decoding tests
        ├── TLVTest.kt            # TLV parsing tests (minimal)
        └── UtilsTest.kt          # Byte utility tests
```

## BER-TLV Format Reference

BER-TLV encodes data as sequential Tag-Length-Value triplets:

### Tag (variable length: 1-N bytes)

```
Byte 1:
  [b8 b7] - Class: 00=Universal, 01=Application, 10=ContextSpecific, 11=Private
  [b6]    - Form:  0=Primitive (value is data), 1=Constructed (value contains nested TLVs)
  [b5-b1] - Tag number (0-30), or 11111 = "long form" (tag number continues in subsequent bytes)

Subsequent bytes (if b5-b1 of byte 1 are all 1):
  [b8]    - 1=more bytes follow, 0=last byte
  [b7-b1] - 7 bits of tag number (big-endian, concatenated across bytes)
```

### Length (variable length: 1-N bytes)

```
Short form (1 byte):   0xxxxxxx = length 0-127
Long form  (N bytes):  1nnnnnnn = n subsequent bytes encode the length (big-endian)
```

### Value (variable length: as specified by Length)

Raw bytes. If the Tag's form is Constructed, the value contains nested TLV elements.

## Core Types

### `Tag` (value class wrapping `ByteArray`)
- **Construction**: `Tag.from(tagBytes)`, `Tag.from(tagHex)`, `Tag.from(tagNumber, tagClass, tagForm)`
- **Validation**: `Tag.isValidTag(bytes)` / `Tag.isValidTag(hex)`
- **Properties**: `tagClass`, `tagForm`, `isPrimitive`, `isConstructed`, `tagNumberBytes`, `tagNumberLong`, `tagNumberInt`, `size`, `asBytes`, `asHex`
- **Enums**: `Tag.Class` (Universal, Application, ContextSpecific, Private), `Tag.Form` (Primitive, Constructed)

### `Length` (value class wrapping `ByteArray`)
- **Construction**: `Length.from(lengthBytes)`, `Length.from(lengthHex)`, `Length.from(lengthLong)`, `Length.from(lengthInt)`
- **Validation**: `Length.isValid(bytes)` / `Length.isValid(hex)`
- **Properties**: `size` (byte count of the length field itself), `lengthAsBytes`, `lengthAsLong`, `lengthAsInt`, `lengthAsHex`

### `Value` (value class wrapping `ByteArray`)
- **Construction**: `Value(bytes)`, `Value.from(bytes)`, `Value.from(hex)`
- **Properties**: `size`, `asHex`, `valueBytes`

### `TLV` (data class)
- **Construction**: `TLV.from(tag, value)` (auto-calculates length), `TLV.from(tlvHex)`, `TLV.from(tlvBytes)`
- **Properties**: `tag`, `length`, `value`, `size`, `bytes`, `hex`, `isNested`, `nestedTlvs`

### `TLVList` (value class wrapping `MutableList<TLV>`)
- **Construction**: `TLVList.from(mutableListOf(...))`, `TLVList.from(bytes)`
- **Properties**: `tlvList`, `size`, `bytes`, `hex`

### Utility Extensions (in `Utils.kt`)
- `Byte.asInt()` / `Byte.asLong()` — unsigned conversion (masks sign extension)
- `ByteArray.toInt()` / `ByteArray.toLong()` — big-endian byte array to numeric
- `Long.toByteArray()` — to 8-byte array
- `Long.toMinimalByteArray()` — to minimal-size byte array (strips leading zeros)

## Known Bugs (as of current state)

### Critical

1. **`TLV.from(tlvBytes)` — Length field parsing is broken**
   - Short-form: computes an empty slice for the length field → crash
   - Long-form: off-by-one, doesn't include the leading length byte
   - Value start index has an extra `+1` that compounds the error
   - **Root cause**: `lengthEndIndex` calculation is wrong for both code paths

2. **`TLVList.from(ByteArray)` — Infinite loop**
   - Always calls `TLV.from(tlvList)` from index 0, never advances through the byte array
   - Produces the same TLV repeatedly until `remainingSize` underflows

3. **`Length.from(length: Long)` — Rejects zero-length values**
   - Uses `check(length > 0)` but zero-length values are valid in BER-TLV

4. **`Length.isValid(ByteArray)` — Crashes on empty input**
   - Calls `.first()` on empty array instead of returning `false`

### Design

5. **No package declaration** — All types and extension functions are in the default package, polluting consumer namespaces

6. **Value class + ByteArray = broken equality** — `ByteArray.equals()` is referential; two Tags with identical bytes are not equal. Also breaks `data class TLV` equality and Set/Map usage

7. **Mutable state exposed** — `tagBytes`, `lengthBytes`, `valueBytes` are public and mutable; callers can corrupt internal state

8. **`TLVList` exposes `MutableList`** — External code can modify the internal list

9. **Utility extensions are public** — `asInt()`, `toMinimalByteArray()`, etc. leak into consumer autocomplete

## Building

```bash
# Build all targets
./gradlew :libraries:tlv:assemble

# Build JVM only
./gradlew :libraries:tlv:jvmJar

# Run tests (JVM)
./gradlew :libraries:tlv:jvmTest

# Run tests (all targets)
./gradlew :libraries:tlv:allTests
```

## Notes for AI Assistance

1. This is a **pure Kotlin Multiplatform** module — no Android-specific APIs, no platform-specific source sets currently used
2. All source code is in `commonMain` — truly cross-platform
3. The library has **zero runtime dependencies** — only `kotlin-test` for tests
4. When fixing bugs, add corresponding tests — current test coverage is thin (especially `TLV.from(bytes)`, `TLVList`, and `Value` have no/minimal tests)
5. Respect the BER-TLV specification (ISO 8825-1 / ISO 7816-4) when making changes
6. The library is meant to be published to Maven Central — public API surface matters
7. Use `@OptIn(ExperimentalStdlibApi::class)` for `hexToByteArray()` / `toHexString()` usage
8. Value classes cannot override `equals`/`hashCode` — fixing the equality issue requires converting to regular classes
9. Files currently have no package — when adding one, use `io.github.axondragonscale.tlv` (matches the namespace and Android namespace in build.gradle.kts)