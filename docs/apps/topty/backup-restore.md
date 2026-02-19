> Context: [Main](../../context.md) > [Topty](./context.md) > Backup & Restore

# Topty - Backup & Restore System

## Overview

Topty supports importing and exporting tokens in multiple formats for interoperability with other authenticator apps and for user data portability. The backup system handles encryption, format conversion, and bulk token operations.

## Backup Formats

### 1. TOPTY Format (`.topty`)

The native backup format — encrypted JSON containing all token data.

- **File extension**: `.topty`
- **Encryption**: Uses password-derived encryption (same as token storage — see [security.md](./security.md))
- **Data preserved**: All fields including `group`, `note`, `lastUsedAt`, `isFavorite`
- **Use case**: Full backup/restore within Topty

**Data model** (`ToptyBackupData`):
```kotlin
@Serializable
data class ToptyBackupData(
    val tokens: List<TokenBackupEntry>
)

@Serializable
data class TokenBackupEntry(
    val name: String,
    val issuer: String,
    val secret: String,          // Plaintext (decrypted for export)
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val digits: Int,
    val period: Int,
    val counter: Long,
    val group: String?,
    val note: String?,
    val isFavorite: Boolean,
    val lastUsedAt: Long?,
)
```

### 2. AEGIS Format (`.json`)

Industry-standard JSON vault format compatible with Aegis Authenticator, andOTP, and 2FAS.

- **File extension**: `.json`
- **Encryption**: None (plaintext JSON)
- **Data preserved**: Core token fields only — `name`, `issuer`, `secret`, `protocol`, `hashAlgo`, `digits`, `period`, `counter`
- **Data NOT preserved**: `group`, `note`, `lastUsedAt`, `isFavorite`
- **Use case**: Migration to/from other authenticator apps

**Data model** (`AegisVaultData`):
```kotlin
@Serializable
data class AegisVaultData(
    val version: Int = 1,
    val entries: List<AegisEntry>
)

@Serializable
data class AegisEntry(
    val type: String,           // "totp" or "hotp"
    val name: String,
    val issuer: String,
    val info: AegisEntryInfo
)

@Serializable
data class AegisEntryInfo(
    val secret: String,         // Base32-encoded
    val algo: String,           // "SHA1", "SHA256", "SHA512"
    val digits: Int,
    val period: Int?,           // TOTP only
    val counter: Long?,         // HOTP only
)
```

### 3. OTP Auth URI Format (`.txt`)

Plain text file containing `otpauth://` URIs, one per line.

- **File extension**: `.txt`
- **Encryption**: None (plaintext)
- **Data preserved**: Only what the URI spec supports — `name`, `issuer`, `secret`, `algorithm`, `digits`, `period`/`counter`
- **Use case**: Maximum compatibility with any authenticator that supports URI import

**Example output**:
```
otpauth://totp/ACME:user@email.com?secret=JBSWY3DPEHPK3PXP&issuer=ACME&algorithm=SHA1&digits=6&period=30
otpauth://hotp/GitHub:dev@github.com?secret=GEZDGNBVGY3TQOJQ&issuer=GitHub&algorithm=SHA1&digits=6&counter=0
```

## Format Comparison

| Feature | TOPTY | AEGIS | OTP_AUTH_URI |
|---------|-------|-------|--------------|
| File extension | `.topty` | `.json` | `.txt` |
| Encrypted | Yes | No | No |
| Groups | ✅ | ❌ | ❌ |
| Notes | ✅ | ❌ | ❌ |
| Favorites | ✅ | ❌ | ❌ |
| Last used | ✅ | ❌ | ❌ |
| Interoperable | Topty only | Aegis/andOTP/2FAS | Any authenticator |

## Key Classes

### `BackupFormat` (Enum)

```kotlin
enum class BackupFormat {
    TOPTY,          // Encrypted native format
    AEGIS,          // Industry standard JSON
    OTP_AUTH_URI    // Plain text URIs
}
```

### `BackupExporter`

Serializes and writes tokens to a file at a user-selected URI.

**Key method**:
```kotlin
suspend fun exportToFile(
    tokens: List<Token>,
    format: BackupFormat,
    uri: Uri
)
```

**Flow**:
1. Load all tokens from repository
2. Decrypt each token's secret (via `token.secret` property — see [security.md](./security.md))
3. Convert to the target format's data model
4. Serialize to JSON/text
5. For TOPTY format: encrypt the serialized data with the password-derived key
6. Write to the user-selected file URI via `ContentResolver`

### `BackupImporter`

Parses backup files and imports tokens into the database.

**Key method**:
```kotlin
suspend fun importFromFile(uri: Uri): List<Token>
```

**Flow**:
1. Read file content from URI via `ContentResolver`
2. Detect format (by file extension or content structure)
3. Parse content into token data
4. For TOPTY format: decrypt the content using the current password-derived key
5. Convert parsed data to `Token` entities
6. Auto-generate new IDs for all imported tokens (avoids ID conflicts)
7. Encrypt each token's secret via `Token.fromPlaintext()` factory method
8. Insert all tokens into Room via `TokenRepository`

## Data Flow

### Export Flow

```
User selects format + destination URI
    ↓
BackupExporter.exportToFile(tokens, format, uri)
    ↓
TokenRepository.getTokens() → List<Token>
    ↓
token.secret → auto-decrypts via TokenEncryption
    ↓
Map to format-specific data model
    ↓
Serialize (JSON or text)
    ↓ (TOPTY format only)
Encrypt with password-derived key
    ↓
Write to file via ContentResolver
```

### Import Flow

```
User selects backup file URI
    ↓
BackupImporter.importFromFile(uri)
    ↓
Read file content via ContentResolver
    ↓ (TOPTY format only)
Decrypt with password-derived key
    ↓
Parse content → List<plaintext token data>
    ↓
Token.fromPlaintext() → auto-encrypts each secret
    ↓
TokenRepository.insertAll(tokens) → Room database
    ↓
New IDs auto-generated (no conflicts with existing tokens)
```

### Re-encryption on Password Change

When the user changes their master password, existing TOPTY backup files become unreadable with the new key. The re-encryption of in-database tokens is handled by the password change flow (see [security.md](./security.md#re-encryption)). Backup files are **not** retroactively re-encrypted — users should create a new backup after changing their password.

## UI Integration

### BackupRestore Screen

- **Route**: `Route.BackupRestore`
- **ViewModel**: `BackupRestoreVM`
- **State**: `BackupRestoreUiState`
- **Events**: `BackupRestoreUiEvent`

The screen provides:
1. **Export section**: Format selector (TOPTY/AEGIS/URI) → triggers system file picker for save location
2. **Import section**: Triggers system file picker for backup file selection → auto-detects format → imports

### Navigation

Accessed from Settings screen → Data section → "Backup & Restore"

## Extension

`BackupFormatExt.kt` in `util/` package provides extensions on `BackupFormat`:
- Display names and descriptions for UI presentation
- File extension mappings
- MIME type mappings for file picker intents

## Related Context

- [Topty Context](./context.md) — Module overview, architecture, all features
- [Topty Security](./security.md) — Encryption, password management, key derivation (used by TOPTY format encryption)
- [Commons Module](../../core/commons/context.md) — Shared utilities, serialization setup
- [Main Context](../../context.md) — Project overview, tech stack