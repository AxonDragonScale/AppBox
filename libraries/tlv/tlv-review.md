# TLV Library - Code Review

## 🔴 Critical Bugs

### 1. `TLV.from(tlvBytes)` — Length field parsing is broken

**File**: `TLV.kt` (lines 27-38)

```kotlin
val lengthEndIndex = lengthStartIndex + if (tlvBytes[lengthStartIndex] and 0b1000_0000.toByte() == 0.toByte()) {
    0
} else {
    tlvBytes[lengthStartIndex].toInt() and 0b0111_1111
}
val length = Length.from(tlvBytes.sliceArray(lengthStartIndex until lengthEndIndex))

val valueEndIndex = lengthEndIndex + length.lengthAsInt
val value = Value(tlvBytes.sliceArray(lengthEndIndex + 1 .. valueEndIndex))
```

**Problems**:
- **Short-form length** (single byte, MSB=0): `lengthEndIndex = lengthStartIndex + 0`, so `sliceArray(lengthStartIndex until lengthStartIndex)` produces an **empty array** → crash in `Length.isValid`.
- **Long-form length** (e.g. `0x81 0xFF`): the offset adds the count of subsequent octets but doesn't add `+1` for the leading byte itself, so it always slices one byte short.
- **Value start index**: `lengthEndIndex + 1` has an extra `+1` that compounds the error.

**Fix**: The correct calculation should be:
- Short form: total length field = 1 byte → `lengthEndIndex = lengthStartIndex + 1`
- Long form: first byte = `0x8N` → total length field = `1 + N` bytes → `lengthEndIndex = lengthStartIndex + 1 + N`
- Value starts at `lengthEndIndex`, not `lengthEndIndex + 1`

---

### 2. `TLVList.from(ByteArray)` — Infinite loop, always parses from index 0

**File**: `TLVList.kt` (lines 14-21)

```kotlin
fun from(tlvList: ByteArray): TLVList {
    val result = mutableListOf<TLV>()

    var remainingSize = tlvList.size
    while (remainingSize > 0) {
        val tlv = TLV.from(tlvList) // Always parsing from the beginning!
        result.add(tlv)
        remainingSize -= tlv.size
    }
```

**Problem**: `TLV.from(tlvList)` always parses from the **beginning** of the array. It never advances. For any input with more than one TLV, this produces an infinite loop of the same first TLV.

**Fix**: Track an offset and pass a sub-array, or refactor `TLV.from` to accept/return an offset.

---

### 3. `Length.from(length: Long)` — Rejects zero-length values

**File**: `Length.kt` (line 35)

```kotlin
check(length > 0) { "Length must be positive" }
```

**Problem**: Zero-length values are perfectly valid in BER-TLV (e.g. ASN.1 NULL). A TLV with an empty value is a legitimate construct.

**Fix**: Change to `length >= 0`.

---

### 4. `Length.isValid(ByteArray)` — Crashes on empty input

**File**: `Length.kt` (lines 11-20)

```kotlin
fun isValid(lengthBytes: ByteArray): Boolean {
    if (lengthBytes.size == 1) {
        return lengthBytes.first() and 0b1000_0000.toByte() == 0b0.toByte()
    }

    if (lengthBytes.first() and 0b1000_0000.toByte() == 0b0.toByte()) return false
```

**Problem**: When `size == 0`, the first `if` is skipped, then `.first()` throws `NoSuchElementException`.

**Fix**: Add `if (lengthBytes.isEmpty()) return false` as the first check.

---

## 🟠 Design Issues

### 5. No package declaration

All source files are in the default/root package. For a publishable library this is a serious issue:
- Consumers can't import selectively
- Extension functions (`asInt()`, `toLong()`, `toMinimalByteArray()`, etc.) leak into every consumer's global namespace
- Name conflicts with other libraries are likely

**Fix**: Add `package io.github.axondragonscale.tlv` to all source files (matches the Android `namespace` in `build.gradle.kts`).

---

### 6. `@JvmInline value class` wrapping `ByteArray` — Broken equality

`ByteArray.equals()` uses reference equality, not structural equality. This means:

```kotlin
val tag1 = Tag.from("01")
val tag2 = Tag.from("01")
tag1 == tag2 // false!
```

This also breaks:
- `data class TLV` equality (its fields are value classes over `ByteArray`)
- Usage in `Set` / `Map` collections

**Fix**: Value classes cannot override `equals`/`hashCode`. Either switch to regular classes, or wrap with a `List<Byte>` (which has structural equality), or provide explicit comparison utilities.

---

### 7. Mutable internal state exposed

`Tag.tagBytes`, `Length.lengthBytes`, `Value.valueBytes` are all public `ByteArray` properties. Callers can mutate the backing array and silently corrupt the objects.

**Fix**: Return defensive copies from public accessors, or mark the backing properties `internal`.

---

### 8. `TLVList` exposes `MutableList`

**File**: `TLVList.kt` (lines 4-5)

```kotlin
@JvmInline
value class TLVList private constructor(
    val tlvList: MutableList<TLV>
```

External code can freely `.add()` / `.remove()` / `.clear()` on the internal list.

**Fix**: Change to `List<TLV>`. Use `.toMutableList()` internally where mutation is needed.

---

### 9. Public utility extension functions

**File**: `Utils.kt`

`Byte.asInt()`, `Byte.asLong()`, `ByteArray.toInt()`, `ByteArray.toLong()`, `Long.toByteArray()`, `Long.toMinimalByteArray()` are all `public` top-level extensions. These are internal implementation details that will pollute every consumer's autocomplete and namespace.

**Fix**: Mark all of them `internal`.

---

## 🟡 Minor Issues

### 10. Inconsistent public API on `Value`

`Value` has a public constructor **and** `from()` factories. `Tag` and `Length` have private constructors with only `from()` factories.

**Fix**: Make `Value`'s constructor private/internal to match the other types.

---

### 11. Tests use `assertEquals(true, expr)` instead of `assertTrue`

Most `LengthTest` assertions use:
```kotlin
assertEquals(true, length1.size == 1)
```
Instead of:
```kotlin
assertTrue { length1.size == 1 }
// or
assertEquals(1, length1.size)
```

The `assertEquals(true, ...)` pattern gives poor failure messages (just "expected true but was false" with no context).

---

### 12. Thin test coverage

| Area | Coverage |
|------|----------|
| `Tag` validation & construction | ✅ Good |
| `Length` validation & encoding | ✅ Good |
| `Utils` byte conversions | ✅ Good |
| `TLV.from(tag, value)` | ⚠️ Minimal (1 test, no assertions) |
| `TLV.from(bytes)` / `TLV.from(hex)` | ❌ None — this is where the critical bugs are |
| `TLVList` | ❌ No test file |
| `Value` | ❌ No test file |
| Nested/Constructed TLV round-trip | ❌ None |

---

### 13. Unused import in build file

**File**: `build.gradle.kts` (line 1)

```kotlin
import com.vanniktech.maven.publish.SonatypeHost
```

This import is not used anywhere in the build script.

---

### 14. Redundant companion object naming

**File**: `Length.kt`

```kotlin
companion object Companion {
```

The explicit `Companion` name is the default and is redundant. Use `companion object {` instead.