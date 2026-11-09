---
title: "TLV library"
status: current
summary: "BER-TLV encoder and decoder for KMP, used by Cardy for EMV parsing and independently publishable to Maven Central."
read_when: "Changing TLV parsing or encoding, or publishing the library."
---

> Context: [Main](../../context.md) > TLV Library

# TLV Library — `:libraries:tlv`

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
| Targets        | JVM, Android, iOS (arm64/simulatorArm64), Linux x64 |

## Project Structure

```
libraries/tlv/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/        # All source code (KMP common)
    │   ├── Tag.kt                # Tag parsing and construction
    │   ├── Length.kt             # Length encoding/decoding
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

### Utility Extensions (`Utils.kt`)

- `Byte.asInt()` / `Byte.asLong()` — unsigned conversion (masks sign extension)
- `ByteArray.toInt()` / `ByteArray.toLong()` — big-endian byte array to numeric
- `Long.toByteArray()` — to 8-byte array
- `Long.toMinimalByteArray()` — to minimal-size byte array (strips leading zeros)

## Parser behaviour and limits

Verified against source — do not "fix" these:

- **Length parsing** handles both forms: short form ends the length at its start byte, long form
  takes the low 7 bits of the first byte as the byte count.
- **Value bounds**: starts at `lengthEndIndex + 1` and spans `lengthAsInt` bytes.
- **`TLVList.from(ByteArray)`** skips `0x00` / `0xFF` padding and advances by `tlv.size`.
- **`Length.from(Long)`** accepts `0`.

Still open (design, not bugs):

- **No package declaration** — every type is in the default package, so consumers write `import TLV`.
- **`Tag` equality is referential** — it wraps a `ByteArray` in a value class, so two tags with
  identical bytes are not equal, and `TLV` / `TLVList` inherit that. Avoid `Set` / `Map` keyed on tags.
- **`TLVList.tlvList` is an exposed `MutableList`** — callers can modify the internal list.
- **Utility extensions are public** — `Byte.asInt()`, `Long.toMinimalByteArray()` and friends land in
  consumer autocomplete.

### Serialization contract

`TLV` is `@Serializable(with = TLVSerializer::class)`. This is what lets Cardy persist `rawTlvs` as
JSON inside its encrypted blob, so any change to `TLV`'s shape has to keep `TLVSerializer` and
`TLVSerializerTest` in step.

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

## Related Context

- [Main Context](../../context.md) — Project overview, module index
- [Build System](../../build-system.md) — Gradle, version catalog, convention plugins