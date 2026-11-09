---
title: "Topty — 2FA authenticator"
status: current
summary: "TOTP/HOTP authenticator with QR scanning, password-based encryption, biometric lock, onboarding and multi-format backup."
read_when: "Changing token generation, storage, onboarding, or app-level flows."
---

> Context: [Main](../../context.md) > Topty

# Topty - 2FA Authenticator App

## Overview

Topty is a secure 2FA authenticator within AppBox that generates TOTP/HOTP one-time passwords. Features include QR scanning, password-based encryption, biometric lock, onboarding flow, backup/restore with multiple format support, and encrypted secret storage using password-derived keys.

- **Package**: `com.axondragonscale.topty`
- **Module path**: `:apps:topty`
- **Key Tech**: Room (encrypted storage), CameraX + ML Kit (QR scanning), AES-256-GCM encryption, BiometricPrompt API

## Architecture

- **UI**: Jetpack Compose + Material3
- **Navigation**: Type-safe Navigation Compose with `@Serializable` routes
- **DI**: Koin (`ToptyApplication` initializes `ToptyModule`)
- **Database**: Room with encrypted secrets
- **Security**: Password-derived AES-256-GCM encryption, BiometricPrompt API — see [Security sub-context](./security.md) for details
- **Camera**: CameraX + ML Kit Barcode Scanner
- **Preferences**: DataStore + EncryptedSharedPreferences

## Package Structure

```
com.axondragonscale.topty/
├── ToptyActivity.kt              # Entry point with biometric lock integration
├── ToptyApplication.kt           # Koin initialization (ToptyModule)
├── ToptyConstants.kt             # URI schemes and parameters
├── auth/
│   ├── BiometricAuthManager.kt   # Authentication prompts (requires FragmentActivity)
│   └── BiometricAvailabilityChecker.kt  # Hardware availability checks
├── backup/                       # See backup-restore.md for details
│   ├── AegisVaultData.kt         # Aegis format data models
│   ├── BackupExporter.kt         # Export to Topty/Aegis/URI formats
│   ├── BackupFormat.kt           # Format enum (TOPTY, AEGIS, OTP_AUTH_URI)
│   ├── BackupImporter.kt         # Import from backup files
│   └── ToptyBackupData.kt        # Native backup data models
├── core/
│   ├── HMACGenerator.kt          # HMAC-based code generation
│   ├── HOTPGenerator.kt          # Counter-based OTP (RFC 4226)
│   ├── TOTPGenerator.kt          # Time-based OTP (RFC 6238)
│   ├── TTS.kt                    # Text-to-speech for codes
│   └── TokenUriHandler.kt        # Parse/generate otpauth:// URIs
├── database/
│   ├── ToptyDatabase.kt          # Room database singleton
│   ├── converter/
│   │   ├── HashAlgoConverter.kt  # Room type converter
│   │   └── ProtocolConverter.kt  # Room type converter
│   ├── dao/
│   │   └── TokenDao.kt           # Token CRUD operations
│   └── entity/
│       └── Token.kt              # Token entity with encrypted secrets
├── di/
│   └── ToptyModule.kt            # Koin module (DB, repos, VMs, backup, auth, security)
├── model/
│   ├── Code.kt                   # Sealed class (TOTPCode, HOTPCode)
│   ├── CodeDesign.kt             # Code display styles
│   ├── HashAlgo.kt               # SHA1, SHA256, SHA512
│   ├── PasswordStrength.kt       # WEAK, MEDIUM, STRONG
│   ├── Protocol.kt               # TOTP, HOTP
│   ├── SortOrder.kt              # 9 sorting options with displayName()
│   ├── UIFont.kt                 # Font customization
│   └── UIMode.kt                 # LIGHT, DARK, AUTO
├── repository/
│   ├── TokenRepository.kt        # Token CRUD, bulk operations
│   └── ToptyPrefsRepository.kt   # DataStore preferences
├── security/                     # See security.md for details
│   ├── PasswordManager.kt        # Password validation, strength checking, PBKDF2 key derivation
│   └── TokenEncryption.kt        # AES-256-GCM encryption using password-derived keys
├── ui/
│   ├── BiometricLockScreen.kt    # Lock screen composable
│   ├── Route.kt                  # Type-safe @Serializable routes
│   ├── ToptyApp.kt               # Root NavHost composable
│   ├── backuprestore/            # Backup/restore screen
│   ├── common/                   # Camera, Dropdown, TextInput, etc.
│   ├── devtools/                 # Debug utilities
│   ├── home/                     # Token list with live codes
│   ├── onboarding/               # 3-page first-time setup flow
│   ├── qrscanner/                # QR scanning screen
│   ├── settings/                 # Settings + password change screens
│   ├── sharetoken/               # Token sharing via QR code
│   ├── theme/                    # Color, Shape, Theme, Type
│   ├── token/                    # Add/edit token screen
│   └── util/                     # Debug, ModifierExt, UiModeExt
├── util/
│   ├── BackupFormatExt.kt        # BackupFormat extensions
│   └── TokenExt.kt               # Token.generateCode() extension
└── widget/
    └── multi/                    # (Empty) - Future widget support
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data object Onboarding       // 3-page linear setup: Welcome → Features → Password
    @Serializable data object Home             // Token list with live codes
    @Serializable data object QRScanner        // Camera-based QR import
    @Serializable data object Settings         // Theme, security, preferences
    sealed interface Token {
        @Serializable data class Add(val uri: String?)   // Add new token (optional pre-filled URI)
        @Serializable data class Edit(val id: Long)      // Edit existing token
    }
    @Serializable data object BackupRestore    // Import/export
    @Serializable data class ShareToken(val tokenId: Long)  // Share individual token
    @Serializable data object ChangePassword   // Change master password
    @Serializable data object DevTools         // Debug utilities
}
```

## Token Management

### Token Entity (`database/entity/Token.kt`)

- Stores `encryptedSecret` (auto-decrypted via `secret` property getter using `TokenEncryption`)
- Factory methods: `fromPlaintext()` auto-encrypts, `withSecret()` creates encrypted copy
- Fields: `id`, `name`, `issuer`, `group`, `note`, `protocol`, `hashAlgo`, `period`, `counter`, `digits`, `lastUsedAt`, `isFavorite`

### Protocols & Algorithms

- **TOTP** (time-based) / **HOTP** (counter-based)
- **Hash Algorithms**: SHA1, SHA256, SHA512
- **Sort Options**: Added first/last, name/issuer/group A-Z/Z-A, recently used
- **Favorites**: Tokens can be marked as favorites

### OTP Generation

- `TOTPGenerator` / `HOTPGenerator` use `HMACGenerator` (all object singletons)
- Code types: `TOTPCode` (with period tracking), `HOTPCode`
- Extension: `Token.generateCode()` delegates to appropriate generator
- Secrets auto-decrypt during generation via `token.secret` property

### Token URI Format

Follows Google Authenticator spec:
```
otpauth://totp/ACME:user@email.com?secret=BASE32&issuer=ACME&algorithm=SHA1&digits=6&period=30
```
Handled by: `TokenUriHandler.parseTokenUri()` / `generateTokenUri()`

## Preferences (ToptyPrefsRepository)

**Appearance:**
- `uiMode`: Light/Dark/Auto
- `dynamicTheme`: Material You colors
- `uiFont`: Font customization
- `codeDesign`: Code display style

**Behavior:**
- `speakOutOnTap`: TTS accessibility
- `sortOrder`: Token list ordering

**Security:** (see [security.md](./security.md))
- `biometricLock`: App authentication
- `preventScreenshots`: Security flag
- `passwordHash`, `passwordHashSalt`, `keyDerivationSalt`

**Other:**
- `hasCompletedOnboarding`: Onboarding completion status

## UI Screens

### Onboarding (First Launch)
- 3-page linear, non-skippable flow: Welcome → Features → Password
- Consistent bottom Continue button using DefaultShape on all pages
- No page indicators, no swiping, no skip option
- Completes with password creation → see [security.md](./security.md) for encryption details

### Home
- Token list with live countdown codes
- Context menu for token actions (edit, delete, favorite, share)
- Auto-refreshing TOTP codes with period tracking

### QR Scanner
- CameraX preview with ML Kit barcode detection
- Validates `otpauth://` URIs before navigation to Token.Add

### Settings
- **Appearance**: UI Mode, Dynamic Theme, Font, Code UI
- **Behavior**: Speak Out
- **Security**: Biometric Lock, Change Password, Prevent Screenshots
- **Data**: Backup & Restore
- **About**: App info and developer details

### Token (Add/Edit)
- Manual entry or pre-filled from QR scan URI
- Fields: name, issuer, secret, protocol, hash algo, period, digits, group, note

### Share Token
- Displays QR code with `otpauth://` URI for sharing with other devices/apps

### Change Password
- Verify current → enter new → re-encrypts all tokens with new key
- See [security.md](./security.md) for re-encryption flow

## Data Flow Summaries

### QR Code Import
1. QRScanner → ML Kit detects → Validate URI
2. Navigate to `Token.Add(uri)` → Parse URI
3. User confirms → `Token.fromPlaintext()` encrypts secret via `TokenEncryption`
4. Save to Room via `TokenRepository`

### Code Generation
1. `HomeVM` observes `TokenRepository.getTokens()`
2. Auto-decrypt: `token.secret` property calls `TokenEncryption.decrypt()`
3. Generate: `token.generateCode()` → TOTP/HOTP
4. Display with countdown, update `lastUsedAt` on tap

### Token Sharing
1. Navigate to `ShareToken(tokenId)` → Load token details
2. Generate QR code with `otpauth://` URI
3. Display QR + URI for sharing

### Biometric Lock
1. Check `biometricLock` pref → Show `BiometricLockScreen`
2. `BiometricAuthManager.authenticate()` prompts
3. Success → Navigate to Home | Fail → Retry | Unavailable → Auto-disable

For security-specific flows (encryption, password change, key derivation), see [security.md](./security.md).
For backup/restore flows, see [backup-restore.md](./backup-restore.md).

## Dependencies

- **Local Modules**: `:core:common`, `:core:ui`, `:core:devtools`, `:core:datastore`, `:libraries:qr`
- **AndroidX**: Compose BOM, Navigation, Room, DataStore, Biometric, CameraX, Splash Screen, Security Crypto
- **ML Kit**: Barcode Scanner
- **Koin**: DI
- **kotlinx**: serialization, coroutines, datetime
- **Coil**: Image loading
- **Accompanist**: Permissions

## Build Info

- **Namespace**: `com.axondragonscale.topty`
- **Min SDK**: 31 (Android 12)
- **Version**: 1.0.0

## Code Conventions

- ViewModels: `HomeVM`, `OnboardingVM`, `QRScannerVM`, `TokenVM`, `SettingsVM`, `ChangePasswordVM`, `BackupRestoreVM`, `ShareTokenVM`
- Generators: Object singletons (`TOTPGenerator`, `HOTPGenerator`, `HMACGenerator`, `TokenEncryption`)
- Managers: Class singletons via Koin (`PasswordManager`, `BiometricAuthManager`)
- Koin DSL: `singleOf`, `factoryOf`, `viewModelOf`
- Extensions: `Token.generateCode()`, `Protocol.code`, `SortOrder.displayName()`

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Core Modules](../../core/context.md) — Shared utilities, networking, DI module
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- **Sub-contexts:**
  - [Security & Encryption](./security.md) — Password-based encryption, PBKDF2, biometrics, key storage, re-encryption
  - [Backup & Restore](./backup-restore.md) — Backup formats (Topty/Aegis/URI), import/export flows