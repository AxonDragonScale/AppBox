> Context: [Main](../../context.md) > [Topty](./context.md) > Security

# Topty — Security Architecture

This document covers Topty's encryption, password management, biometric authentication, and key storage systems. For general Topty context, see [Topty context](./context.md).

## Overview

Topty uses a **password-based encryption** model where all token secrets are encrypted at rest using a key derived from the user's master password. The system is built on:

- **PBKDF2** for key derivation (100,000 iterations)
- **AES-256-GCM** for token secret encryption
- **EncryptedSharedPreferences** for encryption key storage (hardware-backed when available)
- **BiometricPrompt API** for optional app-level authentication

## Encryption Flow

```
User Password
    ↓ PBKDF2 (100k iterations) + Random Salt
Encryption Key (256-bit)
    ↓ Stored in EncryptedSharedPreferences
TokenEncryption.encrypt(secret)
    ↓ AES-256-GCM + Random IV
Encrypted Secret → Room Database
```

## Password-Based Encryption

### How It Works

1. User sets a master password during onboarding (non-skippable)
2. `PasswordManager.setPassword()` is called:
   - Generates a random salt for key derivation
   - Derives a 256-bit encryption key via PBKDF2 with 100,000 iterations
   - Stores the encryption key in `EncryptedSharedPreferences`
   - Hashes the password separately for verification
   - Stores password hash and both salts in DataStore
3. All token secrets are encrypted/decrypted using this derived key

### Key Classes

#### `PasswordManager` (`security/PasswordManager.kt`)

Responsibilities:
- Password validation (minimum 2 characters)
- Password strength checking (`WEAK`, `MEDIUM`, `STRONG` — based on length and character variety)
- PBKDF2 key derivation with random salt
- Secure password verification via hash comparison
- Stores/retrieves encryption key from EncryptedSharedPreferences

Key methods:
- `setPassword(password)` — Derives key via `derivePasswordData()`, stores key + hash + salts
- `verifyPassword(password)` — Compares against stored hash (used for app unlock / password change)
- `getEncryptionKey()` — Retrieves derived key from EncryptedSharedPreferences
- `derivePasswordData(password)` — Derives key + salts + verification hash from a password; returns `DerivedPasswordData`. Used by `setPassword` and `BackupExporter` for custom-password backups
- `deriveKeyFromSalt(password, saltBase64)` — Re-derives the AES key from a password and a Base64-encoded salt. Used by `BackupImporter` to decrypt backups on a new device
- `verifyPasswordAgainstHash(password, hashBase64, saltBase64)` — Verifies a password against an arbitrary hash + salt pair. Used by `BackupImporter` for fast pre-decryption password checks

#### `TokenEncryption` (`security/TokenEncryption.kt`)

Object singleton for encrypting/decrypting token secrets.

- **Algorithm**: AES-256-GCM
- **IV handling**: Random IV generated per encryption, prepended to ciphertext
- **Storage format**: `"IV_BASE64]CIPHERTEXT_BASE64"` (delimiter is `]`)
- Integrates with `PasswordManager` for key retrieval

Key methods:
- `encrypt(plaintext)` → encrypted string in `IV]CIPHERTEXT` format
- `decrypt(encrypted)` → original plaintext secret

### Password Strength Levels

| Level | Criteria |
|-------|----------|
| `WEAK` | Short length, limited character variety |
| `MEDIUM` | Moderate length, some character variety |
| `STRONG` | Longer length, mixed character types (upper, lower, digits, symbols) |

## Token Encryption Integration

### Token Entity (`database/entity/Token.kt`)

The `Token` entity stores `encryptedSecret` in the database. It provides transparent encryption/decryption:

- **`secret` property getter**: Auto-decrypts `encryptedSecret` via `TokenEncryption.decrypt()`
- **`Token.fromPlaintext()`**: Factory method that auto-encrypts the secret during creation
- **`Token.withSecret()`**: Creates a copy with a new encrypted secret

This means:
- Reading `token.secret` always returns the plaintext — decryption happens transparently
- Creating a token via `fromPlaintext()` always stores encrypted — encryption happens transparently
- The database never contains plaintext secrets

### OTP Code Generation

When generating codes (`Token.generateCode()` extension):
1. `token.secret` property is accessed → triggers `TokenEncryption.decrypt()`
2. Plaintext secret is passed to `TOTPGenerator` or `HOTPGenerator`
3. HMAC is computed and code is generated
4. Plaintext secret is never persisted — only held in memory during generation

## Key Storage

| Data | Storage Location | Purpose |
|------|-----------------|---------|
| Encryption Key (256-bit) | `EncryptedSharedPreferences` | Encrypting/decrypting token secrets |
| Password Hash | DataStore | Verifying password on re-entry and embedded in `AppPassword` backup exports |
| Password Hash Salt | DataStore | Salt for password hashing; embedded in `AppPassword` backup exports |
| Key Derivation Salt | DataStore | Salt for PBKDF2 key derivation; embedded in `AppPassword` backup exports |

`EncryptedSharedPreferences` is backed by Android Keystore hardware when available, providing an additional layer of protection for the encryption key.

## Re-encryption (Password Change)

When the user changes their master password (`ui/settings/ChangePasswordVM.kt`):

1. **Verify** old password via `PasswordManager.verifyPassword()`
2. **Load** all tokens from the database
3. **Decrypt** each token's secret using the old encryption key (`token.secret`)
4. **Generate** new encryption key from the new password (`PasswordManager.setPassword()`)
5. **Re-encrypt** all secrets with the new key (`Token.fromPlaintext()` or `Token.withSecret()`)
6. **Update** all tokens in the database with new encrypted secrets

This is an atomic operation — if any step fails, the password change is rolled back.

### Data Flow

```
User enters new password
    ↓
PasswordManager.verifyPassword(oldPassword)
    ↓ (success)
Load all tokens from Room
    ↓
For each token: read token.secret (decrypts with old key)
    ↓
PasswordManager.setPassword(newPassword)
    ↓ (new key now in EncryptedSharedPreferences)
For each token: Token.withSecret(plaintextSecret) (encrypts with new key)
    ↓
Update all tokens in Room
```

## Biometric Authentication

### Components

#### `BiometricAuthManager` (`auth/BiometricAuthManager.kt`)

Manages biometric authentication prompts. Requires a `FragmentActivity` context.

Key methods:
- `authenticate()` — Shows BiometricPrompt dialog
- Handles success, failure, and error callbacks

#### `BiometricAvailabilityChecker` (`auth/BiometricAvailabilityChecker.kt`)

Checks hardware availability for biometric authentication before enabling the feature.

### Biometric Lock Flow

1. App start → Check `biometricLock` preference in `ToptyPrefsRepository`
2. If enabled → Show `BiometricLockScreen` composable
3. `BiometricAuthManager.authenticate()` shows system prompt
4. **Success** → Navigate to Home screen
5. **Failure** → Allow retry
6. **Unavailable** (hardware not present or not enrolled) → Auto-disable the setting

### Important Notes

- Biometric lock is **optional** — configured in Settings under Security section
- It protects app access only — it does NOT replace the master password
- The encryption key is still derived from the password, not biometrics
- If biometric hardware becomes unavailable (e.g., all fingerprints removed), the setting is automatically disabled

## Screenshot Prevention

- Configurable security option in Settings (`preventScreenshots` preference)
- **Default**: Enabled (screenshots blocked)
- Implementation: Sets `FLAG_SECURE` on the Activity window
- Prevents screenshots and screen recording of the app

## Onboarding Security Flow

The onboarding flow is **linear and non-skippable** — 3 pages:

1. **Welcome** → Introduction screen → Continue
2. **Features** → Display app features → Continue
3. **Set Password** → User creates master password → `PasswordManager.setPassword()`

Key design decisions:
- No page indicators, no swiping, no skip option
- Consistent bottom Continue button (using `DefaultShape`) on all pages
- Password creation is mandatory before the app can be used
- After onboarding: `hasCompletedOnboarding = true` is set in preferences → Navigate to Home

## Security-Related Preferences

Stored in `ToptyPrefsRepository`:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `biometricLock` | Boolean | `false` | App-level biometric authentication |
| `preventScreenshots` | Boolean | `true` | Block screenshots/screen recording |
| `passwordHash` | String | — | Hashed master password for verification |
| `passwordHashSalt` | String | — | Salt for password hash |
| `keyDerivationSalt` | String | — | Salt for PBKDF2 key derivation |
| `hasCompletedOnboarding` | Boolean | `false` | Whether onboarding (incl. password setup) is done |

## Related Context

- [Topty Context](./context.md) — Module overview, architecture, all features
- [Topty Backup & Restore](./backup-restore.md) — Backup formats, encryption options, cross-device restore flow
- [Commons Module](../../core/commons/context.md) — Shared utilities
- [Main Context](../../context.md) — Project overview, tech stack