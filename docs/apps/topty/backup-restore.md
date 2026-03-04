> Context: [Main](../../context.md) > [Topty](./context.md) > Backup & Restore

# Topty - Backup & Restore System

## Overview

Topty supports importing and exporting tokens in multiple formats for interoperability with other authenticator apps and for user data portability. The backup system handles encryption, format conversion, and bulk token operations.

## Backup Formats

### 1. TOPTY Format (`.topty`)

The native backup format. Encryption is configurable at export time.

- **File extension**: `.topty`
- **Encryption**: Configurable — none, app password, or custom backup password
- **Data preserved**: All fields — `name`, `issuer`, `group`, `note`, `encryptedSecret`, `protocol`, `hashAlgo`, `digits`, `period`, `counter`, `lastUsedAt`
- **Use case**: Full backup/restore within Topty, cross-device migration

#### File Structure

The `.topty` file is always a **plaintext JSON** `ToptyBackupFile` envelope, regardless of encryption mode. This allows the importer to read the metadata (version, encryption type, salts) before attempting any decryption.

```kotlin
@Serializable
data class ToptyBackupFile(
    val version: Int,               // CURRENT_VERSION = 2
    val timestamp: Long,
    val encryptionType: BackupEncryptionType,  // NONE or ENCRYPTED

    // Present when encryptionType == ENCRYPTED:
    val keyDerivationSalt: String?,          // Base64 — PBKDF2 salt for key re-derivation
    val passwordVerificationHash: String?,   // Base64 — fast pre-check before decryption
    val passwordVerificationSalt: String?,   // Base64 — salt for the verification hash

    val payload: String,  // Plaintext or encrypted JSON of ToptyBackupPayload
)

@Serializable
data class ToptyBackupPayload(
    val tokens: List<BackupToken>,
)
```

#### Encryption Options (`ToptyEncryptionOption`)

| Option | Behaviour |
|--------|-----------|
| `AppPassword` *(default)* | Encrypted with the app's current master password. Key and salts read directly from `PasswordManager` / DataStore — no user input needed at export time. Restoring on a new device requires the app password. |
| `CustomPassword(password)` | Encrypted with a one-off password set specifically for this backup. Key derived fresh via PBKDF2. App password unaffected. |
| `None` | Payload is plaintext JSON. Anyone with the file can read the token secrets. |

#### Cross-Device Portability

The `keyDerivationSalt` embedded in the envelope allows the encryption key to be re-derived from the password on any device via PBKDF2 — no device-bound state (Android Keystore) is used in backup encryption. The `passwordVerificationHash` allows the importer to detect a wrong password before attempting decryption.

### 2. AEGIS Format (`.json`)

Industry-standard JSON vault format compatible with Aegis Authenticator, andOTP, and 2FAS.

- **File extension**: `.json`
- **Encryption**: None (plaintext JSON)
- **Data preserved**: `name`, `issuer`, `secret`, `protocol`, `hashAlgo`, `digits`, `period`, `counter`
- **Data NOT preserved**: `group`, `note`, `lastUsedAt`
- **Use case**: Migration to/from other authenticator apps

### 3. OTP Auth URI Format (`.txt`)

Plain text file containing `otpauth://` URIs, one per line.

- **File extension**: `.txt`
- **Encryption**: None (plaintext)
- **Data preserved**: `name`, `issuer`, `secret`, `algorithm`, `digits`, `period`/`counter`
- **Use case**: Maximum compatibility — any authenticator that supports URI import

## Format Comparison

| Feature | TOPTY | AEGIS | OTP_AUTH_URI |
|---------|-------|-------|--------------|
| File extension | `.topty` | `.json` | `.txt` |
| Encrypted | Configurable | No | No |
| Groups | ✅ | ✅ | ❌ |
| Notes | ✅ | ❌ | ❌ |
| Last used | ✅ | ❌ | ❌ |
| Cross-device | ✅ | ✅ | ✅ |
| Interoperable | Topty only | Aegis/andOTP/2FAS | Any authenticator |

## Key Classes

### `BackupFormat` (Sealed Interface)

```kotlin
sealed interface BackupFormat {
    data class Topty(val encryptionOption: ToptyEncryptionOption = ToptyEncryptionOption.AppPassword) : BackupFormat
    data object Aegis : BackupFormat
    data object OtpAuthUri : BackupFormat
}
```

The `Topty` variant carries the encryption intent, so `BackupExporter.exportToFile` needs no additional parameters.

### `BackupExporter`

Serializes tokens and writes to a user-selected file URI.

```kotlin
suspend fun exportToFile(tokens: List<Token>, format: BackupFormat, uri: Uri): Result<Unit, String>
```

For `BackupFormat.Topty`:
- `AppPassword` — calls `PasswordManager.getEncryptionKey()` for the key; reads `keyDerivationSalt`, `passwordHash`, `passwordHashSalt` from DataStore for the envelope
- `CustomPassword` — calls `PasswordManager.derivePasswordData(password)` to derive a fresh key + salts
- `None` — writes plaintext payload directly

### `BackupImporter`

Reads a backup file and imports tokens. All outcomes are communicated via an `eventHandler` lambda — the method returns `Unit`.

```kotlin
suspend fun importFromFile(
    uri: Uri,
    format: BackupFormat,
    eventHandler: suspend (BackupImportEvent) -> Unit,
)
```

### `BackupImportEvent` (Sealed Interface)

The single channel for all import state — both mid-flight interactions and terminal outcomes.

```kotlin
sealed interface BackupImportEvent {
    // Mid-flight — show password dialog; call onReceived to continue or onCancel to abort
    data class PasswordRequired(val onReceived: suspend (String) -> Unit, val onCancel: suspend () -> Unit) : BackupImportEvent
    data class PasswordError(val message: String, val onReceived: suspend (String) -> Unit, val onCancel: suspend () -> Unit) : BackupImportEvent

    // Terminal
    data class Success(val tokens: List<Token>) : BackupImportEvent
    data class Failure(val message: String) : BackupImportEvent
    data object Cancelled : BackupImportEvent
}
```

## Export Flow

```
User selects BackupFormat.Topty(encryptionOption) + destination URI
    ↓
BackupExporter.exportToFile(tokens, format, uri)
    ↓
Serialize tokens → ToptyBackupPayload JSON

AppPassword:
    key = PasswordManager.getEncryptionKey()
    keyDerivationSalt / verificationHash / verificationSalt ← DataStore
    encrypt payload → ToptyBackupFile(ENCRYPTED, ...)

CustomPassword:
    derived = PasswordManager.derivePasswordData(password)
    encrypt payload with derived.key → ToptyBackupFile(ENCRYPTED, ...)

None:
    ToptyBackupFile(NONE, payload = plaintextJson)
    ↓
Serialize ToptyBackupFile → write to file
```

## Import Flow

```
User selects backup file URI
    ↓
BackupImporter.importFromFile(uri, format, eventHandler)
    ↓
Read file → deserialize as ToptyBackupFile
    ↓
Check version — emit Failure if unsupported
    ↓
encryptionType == NONE
    → decode payload → emit Success(tokens)

encryptionType == ENCRYPTED
    → try PasswordManager.getEncryptionKey() (same-device fast path)
        → success: decode payload → emit Success(tokens)
        → failure:
            → emit PasswordRequired
            → user enters password
            → PasswordManager.verifyPasswordAgainstHash(password, hash, salt)
                → fail: emit PasswordError (retry loop)
                → pass: PasswordManager.deriveKeyFromSalt(password, keyDerivationSalt)
                        → decrypt → decode → emit Success(tokens)
```

### Password Verification

Before attempting AES-GCM decryption on a user-entered password, the importer calls:
```kotlin
PasswordManager.verifyPasswordAgainstHash(password, hashBase64, saltBase64)
```
This avoids relying on `AEADBadTagException` as control flow and gives the user an immediate "Incorrect password" error.

## UI Integration

### BackupRestore Screen

- **Route**: `Route.BackupRestore`
- **ViewModel**: `BackupRestoreVM`
- **State**: `BackupRestoreUiState` (with `ExportUiState` and `ImportUiState` sub-states for the section composables)
- **Events**: `BackupRestoreUiEvent`

**Export section** (`ExportSection`):
- Format selector (Topty / Aegis / OTP Auth URI)
- For Topty: encryption selector (App Password / Custom Password / Unencrypted)
- For Custom Password: inline password + confirm fields with match validation
- Owns the export success dialog

**Import section** (`ImportSection`):
- Single "Import Backup" button — format auto-detected from file
- Owns the import success, confirmation, and password dialogs
- VM passes `eventHandler = ::handleImportEvent` to the importer; `PasswordRequired`/`PasswordError` events store the event in `activeImportEvent` state so the dialog can invoke the callbacks directly

**Error dialog** stays at the root screen level — can be triggered by both sections.

### Navigation

Accessed from Settings screen → Data section → "Backup & Restore"

## Extension

`BackupFormatExt.kt` in `util/` package provides extensions on `BackupFormat` and `ToptyEncryptionOption`:
- `displayName`, `description`, `fileExtension`, `mimeType` on `BackupFormat`
- `displayName`, `description` on `ToptyEncryptionOption`
- `generateSuggestedFileName()` on `BackupFormat`

## Related Context

- [Topty Context](./context.md) — Module overview, architecture, all features
- [Topty Security](./security.md) — PBKDF2, AES-256-GCM, PasswordManager, TokenEncryption
- [Commons Module](../../core/commons/context.md) — Shared utilities, serialization setup
- [Main Context](../../context.md) — Project overview, tech stack