# Topty - 2FA Authenticator Module

## Overview
Topty is a secure 2FA authenticator within AppBox that generates TOTP/HOTP one-time passwords. Features include QR scanning, password-based encryption, biometric lock, onboarding flow, backup/restore with multiple format support, and encrypted secret storage using password-derived keys.

## Architecture
- **UI**: Jetpack Compose + Material3
- **Navigation**: Type-safe Navigation Compose with `@Serializable` routes
- **DI**: Koin (`ToptyApplication` initializes `ToptyModule`)
- **Database**: Room with encrypted secrets
- **Security**: Password-derived AES-256-GCM encryption, BiometricPrompt API
- **Camera**: CameraX + ML Kit Barcode Scanner
- **Preferences**: DataStore + EncryptedSharedPreferences

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
├── security/
│   ├── PasswordManager.kt        # Password validation, strength checking, PBKDF2 key derivation
│   └── TokenEncryption.kt        # AES-256-GCM encryption using password-derived keys
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
│   │   ├── TestData.kt           # Test token generation
│   │   └── ToptyDevTools.kt      # Debug utilities
│   ├── home/
│   │   ├── Home.kt               # Token list with live codes
│   │   ├── HomeUiEffect.kt
│   │   ├── HomeUiEvent.kt
│   │   ├── HomeUiState.kt
│   │   ├── HomeVM.kt
│   │   └── TokenContextMenu.kt   # Context menu for tokens
│   ├── onboarding/
│   │   ├── Onboarding.kt         # Onboarding flow screen (3-page linear flow)
│   │   ├── OnboardingUiEffect.kt
│   │   ├── OnboardingUiEvent.kt
│   │   ├── OnboardingUiState.kt
│   │   ├── OnboardingVM.kt
│   │   └── pages/                # Individual onboarding pages
│   │       ├── FeaturesPage.kt
│   │       ├── SetPasswordPage.kt
│   │       └── WelcomePage.kt
│   ├── qrscanner/
│   │   ├── QRScanner.kt          # QR scanning screen
│   │   ├── QRScannerUiEffect.kt
│   │   ├── QRScannerUiEvent.kt
│   │   ├── QRScannerUiState.kt
│   │   └── QRScannerVM.kt
│   ├── settings/
│   │   ├── ChangePassword.kt     # Password change screen
│   │   ├── ChangePasswordUiEvent.kt
│   │   ├── ChangePasswordUiState.kt
│   │   ├── ChangePasswordVM.kt
│   │   ├── Settings.kt           # Settings screen
│   │   ├── SettingsUiEvent.kt
│   │   ├── SettingsUiState.kt
│   │   └── SettingsVM.kt
│   ├── sharetoken/
│   │   ├── ShareToken.kt         # Token sharing screen
│   │   ├── ShareTokenUiEffect.kt
│   │   ├── ShareTokenUiEvent.kt
│   │   ├── ShareTokenUiState.kt
│   │   └── ShareTokenVM.kt
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
├── util/
│   ├── BackupFormatExt.kt        # BackupFormat extensions
│   └── TokenExt.kt               # Token.generateCode() extension
└── widget/
    └── multi/                    # (Empty) - Future widget support
```

## Key Features

### Security & Authentication
- **Password-Based Encryption**: 
  - User sets master password during onboarding
  - PBKDF2 (100,000 iterations) derives 256-bit encryption key from password
  - Encryption key stored in EncryptedSharedPreferences (hardware-backed on supported devices)
  - Password hash stored separately for verification
  - All token secrets encrypted with AES-256-GCM using password-derived key
- **TokenEncryption**: 
  - AES-256-GCM with IV prepended to ciphertext
  - Format: `"IV_BASE64]CIPHERTEXT_BASE64"`
  - Integrates with PasswordManager for key retrieval
- **PasswordManager**:
  - Password validation (min 2 chars)
  - Strength checking (WEAK/MEDIUM/STRONG based on length and character variety)
  - PBKDF2 key derivation with random salt
  - Secure password verification via hash comparison
- **Biometric Lock**: Optional app-level authentication using fingerprint/face unlock
- **Screenshot Prevention**: Configurable security option (default: enabled)
- **Onboarding Flow**: Non-skippable first-time setup with password creation (3 pages: Welcome → Features → Password)

### Token Management
- **Token Entity** (`database/entity/Token.kt`):
  - Stores `encryptedSecret` (auto-decrypted via `secret` property getter)
  - Factory methods: `fromPlaintext()` auto-encrypts, `withSecret()` creates encrypted copy
  - Fields: `id`, `name`, `issuer`, `group`, `note`, `protocol`, `hashAlgo`, `period`, `counter`, `digits`, `lastUsedAt`, `isFavorite`
- **Protocols**: TOTP (time-based), HOTP (counter-based)
- **Hash Algorithms**: SHA1, SHA256, SHA512
- **Sort Options**: Added first/last, name/issuer/group A-Z/Z-A, recently used
- **Favorites**: Tokens can be marked as favorites

### OTP Generation
- `TOTPGenerator` / `HOTPGenerator` use `HMACGenerator`
- Code types: `TOTPCode` (with period tracking), `HOTPCode`
- Extension: `Token.generateCode()` delegates to appropriate generator
- Secrets auto-decrypt during generation via `token.secret` property

### Backup & Restore
**Three Formats:**
1. **TOPTY** (`.topty`) - Encrypted JSON with all data (group, note, lastUsedAt, isFavorite)
2. **AEGIS** (`.json`) - Industry standard, compatible with Aegis/andOTP/2FAS (no notes/lastUsedAt)
3. **OTP_AUTH_URI** (`.txt`) - Plain text `otpauth://` URIs (basic compatibility)

**Classes:**
- `BackupExporter`: Serializes and exports tokens
- `BackupImporter`: Parses and imports tokens (auto-generates new IDs)

**Note**: Backup files use password-derived encryption for TOPTY format

### UI Screens & Routes
```kotlin
object Route {
    data object Onboarding              // 3-page linear setup: Welcome → Features → Password
    data object Home                    // Token list with live codes
    data object QRScanner               // Camera-based QR import
    data object Settings                // Theme, security, preferences
    sealed interface Token {
        data class Add(val uri: String?)
        data class Edit(val id: Long)
    }
    data object BackupRestore          // Import/export
    data class ShareToken(val tokenId: Long)  // Share individual token
    data object ChangePassword         // Change master password
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
- `hasCompletedOnboarding`: Onboarding completion status
- `passwordHash`: Hashed master password for verification
- `passwordHashSalt`: Salt for password hash
- `keyDerivationSalt`: Salt for encryption key derivation

## Token URI Format
Follows Google Authenticator spec:
```
otpauth://totp/ACME:user@email.com?secret=BASE32&issuer=ACME&algorithm=SHA1&digits=6&period=30
```
**Handled by**: `TokenUriHandler.parseTokenUri()` / `generateTokenUri()`

## Data Flow Examples

### First Launch (Onboarding)
1. Check `hasCompletedOnboarding` → Navigate to Onboarding
2. Page 1 (Welcome): Introduction screen → Continue
3. Page 2 (Features): Display app features → Continue
4. Page 3 (Password): User creates master password → `PasswordManager.setPassword()`
   - Derives encryption key (PBKDF2) → Stores in EncryptedSharedPreferences
   - Hashes password → Stores in DataStore for verification
5. Set `hasCompletedOnboarding = true` → Navigate to Home
6. **Flow is linear and non-skippable** with consistent bottom Continue button on all pages

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
3. Display QR + URI for sharing with other apps/devices

### Password Change
1. Navigate to `ChangePassword` → Verify current password
2. Enter new password → `PasswordManager.verifyPassword()` current
3. Load all tokens → Decrypt with old key → Re-encrypt with new key
4. `PasswordManager.setPassword()` with new password → Update all tokens

### Biometric Lock
1. Check `biometricLock` pref → Show `BiometricLockScreen`
2. `BiometricAuthManager.authenticate()` prompts
3. Success → Navigate to Home | Fail → Retry | Unavailable → Auto-disable

### Backup
1. Select format → `BackupExporter.exportToFile(tokens, format, uri)`
2. TOPTY: Encrypt JSON with password | AEGIS: JSON vault | URI: Plain text
3. Save to user-selected location

## Security Architecture

### Encryption Flow
```
User Password
    ↓ PBKDF2 (100k iterations) + Salt
Encryption Key (256-bit)
    ↓ Stored in EncryptedSharedPreferences
TokenEncryption.encrypt(secret)
    ↓ AES-256-GCM + IV
Encrypted Secret → Database
```

### Key Storage
- **Encryption Key**: EncryptedSharedPreferences (hardware-backed when available)
- **Password Hash**: DataStore (for password verification)
- **Salts**: DataStore (both password hash salt and key derivation salt)

### Re-encryption
When password changes, all token secrets must be re-encrypted:
1. Verify old password
2. Load all tokens from database
3. Decrypt each secret with old key
4. Generate new encryption key from new password
5. Re-encrypt all secrets with new key
6. Update all tokens in database

## Dependencies
- AndroidX: Compose BOM, Navigation, Room, DataStore, Biometric, CameraX, Splash Screen, Security Crypto
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
- ViewModels: `HomeVM`, `OnboardingVM`, `QRScannerVM`, `TokenVM`, `SettingsVM`, `ChangePasswordVM`, `BackupRestoreVM`, `ShareTokenVM`
- Generators: Object singletons (`TOTPGenerator`, `HOTPGenerator`, `HMACGenerator`, `TokenEncryption`)
- Managers: Class singletons via Koin (`PasswordManager`, `BiometricAuthManager`)
- Koin DSL: `singleOf`, `factoryOf`, `viewModelOf`
- Extensions: `Token.generateCode()`, `Protocol.code`, `SortOrder.displayName()`

## Notable Changes from Previous Version
1. **Security Overhaul**: Replaced Android Keystore with password-based encryption using PBKDF2
2. **Onboarding**: Added 3-page linear, non-skippable setup flow (Welcome → Features → Password)
   - Consistent bottom Continue button using DefaultShape on all pages
   - No page indicators, no swiping, no skip option
3. **Password Management**: New `PasswordManager` for password validation, strength checking, and key derivation
4. **Token Sharing**: New screen for sharing individual tokens via QR code
5. **Password Change**: New screen for changing master password with full re-encryption
6. **Favorites**: Tokens can now be marked as favorites
7. **Context Menu**: Added context menu for token actions in home screen
8. **Test Data**: Added `TestData.kt` with 10 diverse test tokens for development