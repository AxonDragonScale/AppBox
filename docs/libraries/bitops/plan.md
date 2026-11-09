---
title: "Bitops — KmpBitSet plan"
status: in-progress
summary: "API plan for KmpBitSet: 64-bit word storage, dynamic growth, and the public bit operations."
read_when: "Implementing the bitops library."
---

# `class KmpBitSet`

**Constructor**

* `KmpBitSet(initialCapacity: Int = 64)`: Initializes the bitset. Capacity dynamically scales up as needed.

### 1. Internal State & Memory Management (Private)

These handle the raw 64-bit bucketing and array resizing under the hood.

* `private var words: LongArray`: The underlying storage. Each `Long` holds 64 bits.
* `private fun wordIndex(bitIndex: Int): Int`: Calculates which `Long` in the array contains the target bit (`bitIndex ushr 6`).
* `private fun ensureCapacity(wordIdx: Int)`: Grows the `words` array if a requested index exceeds current bounds.

### 2. State Checking & Counting

Methods to query the overall status of the bit vector without mutating it.

* `fun length(): Int`: Returns the "logical size" (the index of the highest set bit + 1). Returns 0 if empty.
* `fun cardinality(): Int`: Returns the total number of bits currently set to `1` (true).
* `fun isEmpty(): Boolean`: Returns `true` if no bits are set to `1`.

### 3. Bit Retrieval

Methods to check the value of specific bits.

* `operator fun get(index: Int): Boolean`: Returns the state of a single bit. Supports `bits[5]` syntax.
* `fun get(range: IntRange): KmpBitSet`: Extracts a slice of bits and returns them as a new `KmpBitSet`.

### 4. Bit Mutation

Methods to modify bits at specific indices or across ranges.

* **Single Bit:**
* `fun set(index: Int, value: Boolean = true)`: Sets a bit to the specified value.
* `fun clear(index: Int)`: Sets a bit to `0` (false).
* `fun toggle(index: Int)`: Flips a bit to its opposite state.

* **Ranges:**
* `fun set(range: IntRange)`: Sets all bits in the range to `1`.
* `fun clear(range: IntRange)`: Clears all bits in the range.
* `fun toggle(range: IntRange)`: Flips all bits in the range.

* **Bulk Reset:**
* `fun clear()`: Clears the entire BitSet, resetting all internal words to `0L`.

### 5. Bitwise & Relational Operations

Operations involving a second `KmpBitSet`.

* **Mutating (In-Place):**
* `fun and(other: KmpBitSet)`: Performs a logical AND.
* `fun or(other: KmpBitSet)`: Performs a logical OR.
* `fun xor(other: KmpBitSet)`: Performs a logical XOR.
* `fun andNot(other: KmpBitSet)`: Clears all bits in this set that are present in the `other` set.

* **Non-Mutating:**
* `fun intersects(other: KmpBitSet): Boolean`: Returns `true` if both sets share at least one set bit.

### 6. Fast Scanning

Hardware-accelerated navigation to find the next active or inactive bits.

* `fun nextSetBit(startIndex: Int): Int`: Finds the first `1` bit at or after the start index.
* `fun nextClearBit(startIndex: Int): Int`: Finds the first `0` bit at or after the start index.
* `fun previousSetBit(startIndex: Int): Int`: Finds the first `1` bit at or before the start index (searching backwards).
* `fun previousClearBit(startIndex: Int): Int`: Finds the first `0` bit at or before the start index (searching backwards).
* `fun countOneBits(): Int`:
* `fun countZeroBits(): Int`:
* `fun countLeadingZeroBits(): Int`:
* `fun countTrailingZeroBits(): Int`:

### 7. Exporting & Serialization

Methods to extract the data out of the custom wrapper for external use.

* `fun toIntArray(): IntArray`: Returns an array of the indices for all bits currently set to `1`.
* `fun toLongArray(): LongArray`: Returns a trimmed copy of the raw `LongArray` (useful for network transmission or saving to disk).
* `fun toHexString(): String`: Returns a compact hexadecimal string representation of the bitset (e.g., "1F4A"). Perfect for debugging or saving state.
* `fun toBinaryString(): String`: Returns a string of 1s and 0s representing the bits. The 0th bit is positioned at the far right.

### 8. Standard Kotlin Overrides

Ensures the object behaves correctly in collections and during debugging.

* `override fun equals(other: Any?): Boolean`: Structurally compares the bits, ignoring trailing empty capacity.
* `override fun hashCode(): Int`: Generates a consistent hash based on the active bits.
* `override fun toString(): String`: Formats the active bits cleanly (e.g., `{0, 4, 12}`).


# Primitive Bit Manipulation Extensions

**1. Bit Retrieval**
Checks the state of a specific bit index within the primitive.

* `fun Byte.getBit(index: Int): Boolean`
* `fun Short.getBit(index: Int): Boolean`
* `fun Int.getBit(index: Int): Boolean`
* `fun Long.getBit(index: Int): Boolean`

**2. Bit Mutation (Returns New Value)**
Returns a copy of the primitive with the specified bit modified.

* **Byte Extensions**
* `fun Byte.setBit(index: Int): Byte`
* `fun Byte.clearBit(index: Int): Byte`
* `fun Byte.toggleBit(index: Int): Byte`
* `fun Byte.withBit(index: Int, value: Boolean): Byte`


* **Short Extensions**
* `fun Short.setBit(index: Int): Short`
* `fun Short.clearBit(index: Int): Short`
* `fun Short.toggleBit(index: Int): Short`
* `fun Short.withBit(index: Int, value: Boolean): Short`


* **Int Extensions**
* `fun Int.setBit(index: Int): Int`
* `fun Int.clearBit(index: Int): Int`
* `fun Int.toggleBit(index: Int): Int`
* `fun Int.withBit(index: Int, value: Boolean): Int`


* **Long Extensions**
* `fun Long.setBit(index: Int): Long`
* `fun Long.clearBit(index: Int): Long`
* `fun Long.toggleBit(index: Int): Long`
* `fun Long.withBit(index: Int, value: Boolean): Long`

### Conversion Extensions

**3. Single Primitive to `KmpBitSet**`
Converts the bits of a single number directly into a dynamic `KmpBitSet`.

* `fun Byte.toKmpBitSet(): KmpBitSet`
* `fun Short.toKmpBitSet(): KmpBitSet`
* `fun Int.toKmpBitSet(): KmpBitSet`
* `fun Long.toKmpBitSet(): KmpBitSet`

**4. Primitive Arrays to `KmpBitSet**`
Highly useful for loading a large amount of raw binary data (like a file payload or network packet) into your dynamic bit vector.

* `fun ByteArray.toKmpBitSet(): KmpBitSet`
* `fun ShortArray.toKmpBitSet(): KmpBitSet`
* `fun IntArray.toKmpBitSet(): KmpBitSet`
* `fun LongArray.toKmpBitSet(): KmpBitSet`

### 5. Range Operations

Methods to manipulate or extract a specific contiguous block of bits.

* **Byte Extensions**
* `fun Byte.getBitRange(range: IntRange): Byte` *(Extracts the bits and shifts them to the 0th position)*
* `fun Byte.setBitRange(range: IntRange): Byte`
* `fun Byte.clearBitRange(range: IntRange): Byte`
* `fun Byte.toggleBitRange(range: IntRange): Byte`


* **Short Extensions**
* `fun Short.getBitRange(range: IntRange): Short`
* `fun Short.setBitRange(range: IntRange): Short`
* `fun Short.clearBitRange(range: IntRange): Short`
* `fun Short.toggleBitRange(range: IntRange): Short`


* **Int Extensions**
* `fun Int.getBitRange(range: IntRange): Int`
* `fun Int.setBitRange(range: IntRange): Int`
* `fun Int.clearBitRange(range: IntRange): Int`
* `fun Int.toggleBitRange(range: IntRange): Int`


* **Long Extensions**
* `fun Long.getBitRange(range: IntRange): Long`
* `fun Long.setBitRange(range: IntRange): Long`
* `fun Long.clearBitRange(range: IntRange): Long`
* `fun Long.toggleBitRange(range: IntRange): Long`

### 6. Fast Scanning (Next / Previous)

Finds the index of a specific bit state within the bounds of the primitive (e.g., 0-7 for `Byte`, 0-63 for `Long`). Returns `-1` if no such bit is found.

* **Byte Extensions**
* `fun Byte.nextSetBit(startIndex: Int = 0): Int`
* `fun Byte.nextClearBit(startIndex: Int = 0): Int`
* `fun Byte.previousSetBit(startIndex: Int = 7): Int`
* `fun Byte.previousClearBit(startIndex: Int = 7): Int`


* **Short Extensions**
* `fun Short.nextSetBit(startIndex: Int = 0): Int`
* `fun Short.nextClearBit(startIndex: Int = 0): Int`
* `fun Short.previousSetBit(startIndex: Int = 15): Int`
* `fun Short.previousClearBit(startIndex: Int = 15): Int`


* **Int Extensions**
* `fun Int.nextSetBit(startIndex: Int = 0): Int`
* `fun Int.nextClearBit(startIndex: Int = 0): Int`
* `fun Int.previousSetBit(startIndex: Int = 31): Int`
* `fun Int.previousClearBit(startIndex: Int = 31): Int`


* **Long Extensions**
* `fun Long.nextSetBit(startIndex: Int = 0): Int`
* `fun Long.nextClearBit(startIndex: Int = 0): Int`
* `fun Long.previousSetBit(startIndex: Int = 63): Int`
* `fun Long.previousClearBit(startIndex: Int = 63): Int`

### 7. Counting Methods

*Note: Kotlin's standard library provides `countOneBits()`, `countLeadingZeroBits()`, and `countTrailingZeroBits()` for `Int` and `Long` out of the box, but defining them for `Byte` and `Short`—along with a `countZeroBits()` helper—creates a unified, predictable API across all types.*

* **Byte Extensions**
* `fun Byte.countOneBits(): Int`
* `fun Byte.countZeroBits(): Int`
* `fun Byte.countLeadingZeroBits(): Int`
* `fun Byte.countTrailingZeroBits(): Int`


* **Short Extensions**
* `fun Short.countOneBits(): Int`
* `fun Short.countZeroBits(): Int`
* `fun Short.countLeadingZeroBits(): Int`
* `fun Short.countTrailingZeroBits(): Int`

* **Int Extensions**
* `fun Int.countZeroBits(): Int` *(Completes the standard library)*
* 
* **Long Extensions**
* `fun Long.countZeroBits(): Int` *(Completes the standard library)*
