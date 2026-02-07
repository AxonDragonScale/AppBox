# Topty - 2FA Authenticator Module

## Overview
Topty is a secure 2FA authenticator within AppBox that generates TOTP/HOTP one-time passwords. Features include QR scanning, biometric lock, backup/restore with multiple format support, and encrypted secret storage using Android Keystore.

## Architecture
- **UI**: Jetpack Compose + Material3
- **Navigation**: Type-safe Navigation Compose with `@Serializable` routes
- **DI**: Koin (`ToptyApplication` initializes `ToptyModule`)
- **Database**: Room with encrypted secrets
- **Security**: Android Keystore (AES-256-GCM), BiometricPrompt API
- **Camera**: CameraX + ML Kit Barcode Scanner
- **Preferences**: DataStore

## Package Structure
```
com.axondragonscale.topty/
├── ToptyActivity.kt              # Entry point with biometric lock integration
├── ToptyApplication.kt           # Koin initialization (CommonsModule + ToptyModule)
├── ToptyConstants.kt             # URI schemes and parameters
├── auth/
│   ├── BiometricAuthManager.kt   # Authentication prompts (requires FragmentActivity)
│   └── BiometricAvailabilityChecker.kt  # Hardware availability checks
├── backup/
│   ├── AegisVaultData.kt         # Aegis format data models
│   ├── BackupExporter.kt         # Export to Topty/Aegis/URI formats
│   ├── BackupFormat.kt           # Format enum (TOPTY, AEGIS, OTP_AUTH_URI)
│   ├── BackupImporter.kt         # Import from backup files
│   └── ToptyBackupData.kt        # Native backup data models
├── core/
│   ├── HMACGenerator.kt          # HMAC-based code generation
│   ├── HOTPGenerator.kt          # Counter-based OTP (RFC 4226)
│   ├── QRScanner.kt              # CameraX QR scanning (deprecated, use ui/common)
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
│   └── ToptyModule.kt            # Koin module (DB, repos, VMs, backup, auth)
├── model/
│   ├── Code.kt                   # Sealed class (TOTPCode, HOTPCode)
│   ├── CodeDesign.kt             # Code display styles
│   ├── HashAlgo.kt               # SHA1, SHA256, SHA512
│   ├── Protocol.kt               # TOTP, HOTP
│   ├── SortOrder.kt              # 9 sorting options with displayName()
│   ├── UIFont.kt                 # Font customization
│   └── UIMode.kt                 # LIGHT, DARK, AUTO
├── repository/
│   ├── TokenRepository.kt        # Token CRUD, bulk operations
│   └── ToptyPrefsRepository.kt   # DataStore preferences
├── security/
│   └── SecretEncryption.kt       # AES-256-GCM with Android Keystore
├── ui/
│   ├── BiometricLockScreen.kt    # Lock screen composable
│   ├── Route.kt                  # Type-safe @Serializable routes
│   ├── ToptyApp.kt               # Root NavHost composable
│   ├── backuprestore/
│   │   ├── BackupRestore.kt      # Backup/restore screen
│   │   ├── BackupRestoreUiEvent.kt
│   │   ├── BackupRestoreUiState.kt
│   │   └── BackupRestoreVM.kt
│   ├── common/                   # Camera, Dropdown, TextInput, etc.
│   │   ├── Camera.kt
│   │   ├── Dropdown.kt
│   │   ├── ExpandingFabButton.kt
│   │   ├── QRCodeAnalyzer.kt
│   │   ├── ScannerOverlay.kt
│   │   ├── Switch.kt
│   │   └── TextInput.kt
│   ├── devtools/
│   │   └── ToptyDevTools.kt      # Debug utilities
│   ├── home/
│   │   ├── Home.kt               # Token list with live codes
│   │   ├── HomeUiEffect.kt
│   │   ├── HomeUiEvent.kt
│   │   ├── HomeUiState.kt
│   │   └── HomeVM.kt
│   ├── qrscanner/
│   │   ├── QRScanner.kt          # QR scanning screen
│   │   ├── QRScannerUiEffect.kt
│   │   ├── QRScannerUiEvent.kt
│   │   ├── QRScannerUiState.kt
│   │   └── QRScannerVM.kt
│   ├── settings/
│   │   ├── Settings.kt           # Settings screen
│   │   ├── SettingsUiEvent.kt
│   │   ├── SettingsUiState.kt
│   │   └── SettingsVM.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Shape.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── token/
│   │   ├── Token.kt              # Add/edit token screen
│   │   ├── TokenUiEffect.kt
│   │   ├── TokenUiEvent.kt
│   │   ├── TokenUiState.kt
│   │   └── TokenVM.kt
│   └── util/
│       ├── Debug.kt
│       ├── ModifierExt.kt
│       └── UiModeExt.kt
└── util/
    ├── BackupFormatExt.kt        # BackupFormat extensions
    └── TokenExt.kt               # Token.generateCode() extension
```

## Key Features

### Security & Authentication
- **SecretEncryption**: Token secrets encrypted with AES-256-GCM (Android Keystore)
- **Biometric Lock**: Optional app-level authentication using fingerprint/face unlock
- **Screenshot Prevention**: Configurable security option (default: enabled)

### Token Management
- **Token Entity** (`database/entity/Token.kt`):
  - Stores `encryptedSecret` (auto-decrypted via `secret` property getter)
  - Factory methods: `fromPlaintext()` auto-encrypts, `withSecret()` creates encrypted copy
  - Fields: `id`, `name`, `issuer`, `group`, `note`, `protocol`, `hashAlgo`, `period`, `counter`, `digits`, `lastUsedAt`
- **Protocols**: TOTP (time-based), HOTP (counter-based)
- **Hash Algorithms**: SHA1, SHA256, SHA512
- **Sort Options**: Added first/last, name/issuer/group A-Z/Z-A, recently used

### OTP Generation
- `TOTPGenerator` / `HOTPGenerator` use `HMACGenerator`
- Code types: `TOTPCode` (with period tracking), `HOTPCode`
- Extension: `Token.generateCode()` delegates to appropriate generator
- Secrets auto-decrypt during generation via `token.secret` property

### Backup & Restore
**Three Formats:**
1. **TOPTY** (`.topty`) - Encrypted JSON with all data (group, note, lastUsedAt)
2. **AEGIS** (`.json`) - Industry standard, compatible with Aegis/andOTP/2FAS (no notes/lastUsedAt)
3. **OTP_AUTH_URI** (`.txt`) - Plain text `otpauth://` URIs (basic compatibility)

**Classes:**
- `BackupExporter`: Serializes and exports tokens
- `BackupImporter`: Parses and imports tokens (auto-generates new IDs)

### UI Screens & Routes
```kotlin
object Route {
    data object Home                    // Token list with live codes
    data object QRScanner               // Camera-based QR import
    data object Settings                // Theme, security, preferences
    sealed interface Token {
        data class Add(val uri: String?)
        data class Edit(val id: Long)
    }
    data object BackupRestore          // Import/export
    data object DevTools               // Debug utilities
}
```

### Preferences (ToptyPrefsRepository)
- `uiMode`: Light/Dark/Auto
- `dynamicTheme`: Material You colors
- `codeDesign`: Display style
- `uiFont`: Font customization
- `speakOutOnTap`: TTS accessibility
- `biometricLock`: App authentication
- `preventScreenshots`: Security flag
- `sortOrder`: Token list ordering

## Token URI Format
Follows Google Authenticator spec:
```
otpauth://totp/ACME:user@email.com?secret=BASE32&issuer=ACME&algorithm=SHA1&digits=6&period=30
```
**Handled by**: `TokenUriHandler.parseTokenUri()` / `generateTokenUri()`

## Data Flow Examples

### QR Code Import
1. QRScanner → ML Kit detects → Validate URI
2. Navigate to `Token.Add(uri)` → Parse URI
3. User confirms → `Token.fromPlaintext()` encrypts secret
4. Save to Room via `TokenRepository`

### Code Generation
1. `HomeVM` observes `TokenRepository.getTokens()`
2. Auto-decrypt: `token.secret` property
3. Generate: `token.generateCode()` → TOTP/HOTP
4. Display with countdown, update `lastUsedAt` on tap

### Biometric Lock
1. Check `biometricLock` pref → Show `BiometricLockScreen`
2. `BiometricAuthManager.authenticate()` prompts
3. Success → Navigate to Home | Fail → Retry | Unavailable → Auto-disable

### Backup
1. Select format → `BackupExporter.exportToFile(tokens, format, uri)`
2. TOPTY: Encrypt JSON | AEGIS: JSON vault | URI: Plain text
3. Save to user-selected location

## Dependencies
- AndroidX: Compose BOM, Navigation, Room, DataStore, Biometric, CameraX, Splash Screen
- ML Kit: Barcode Scanner
- Koin: DI
- kotlinx: serialization, coroutines, datetime
- Coil: Image loading
- Accompanist: Permissions

## Build Info
- **Namespace**: `com.axondragonscale.topty`
- **Min SDK**: 31 (Android 12)
- **Version**: 1.0.0

## Code Conventions
- ViewModels: `HomeVM`, `QRScannerVM`, `TokenVM`, `SettingsVM`, `BackupRestoreVM`
- Generators: Object singletons (`TOTPGenerator`, `HOTPGenerator`, `HMACGenerator`, `SecretEncryption`)
- Koin DSL: `singleOf`, `factoryOf`, `viewModelOf`
- Extensions: `Token.generateCode()`, `Protocol.code`, `SortOrder.displayName()`
