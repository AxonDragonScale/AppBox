# Topty - Module Context

## Overview

Topty is a lightweight 2FA (Two-Factor Authentication) authenticator sub-app within AppBox. It generates time-based (TOTP) and counter-based (HOTP) one-time passwords compatible with Google Authenticator and other standard 2FA systems. Features QR code scanning, manual token entry, and text-to-speech code readout.

## Package Structure

```
com.axondragonscale.topty/
├── ToptyActivity.kt              # Entry point activity
├── ToptyConstants.kt             # URI schemes, parameters, links
├── core/
│   ├── HMACGenerator.kt          # HMAC-based code generation
│   ├── HOTPGenerator.kt          # Counter-based OTP generation
│   ├── QRScanner.kt              # CameraX QR code scanning
│   ├── TOTPGenerator.kt          # Time-based OTP generation
│   ├── TTS.kt                    # Text-to-speech for codes
│   └── TokenUriHandler.kt        # otpauth:// URI parsing
├── database/
│   ├── ToptyDatabase.kt          # Room database definition
│   ├── converter/                # Room type converters
│   ├── dao/                      # Data Access Objects
│   └── entity/
│       └── Token.kt              # Token entity
├── di/
│   └── ToptyModule.kt            # Koin dependency injection module
├── model/
│   ├── Code.kt                   # Code sealed class (HOTPCode, TOTPCode)
│   ├── CodeDesign.kt             # Code display style options
│   ├── HashAlgo.kt               # Hash algorithms (SHA1, SHA256, SHA512)
│   ├── Protocol.kt               # OTP protocols (TOTP, HOTP)
│   ├── UIFont.kt                 # Font customization options
│   └── UIMode.kt                 # Light/Dark/Auto theme mode
├── repository/
│   ├── AppPrefsRepository.kt     # User preferences (DataStore)
│   └── TokenRepository.kt        # Token data operations
├── ui/
│   ├── Route.kt                  # Navigation routes (type-safe)
│   ├── ToptyApp.kt               # Root composable with NavHost
│   ├── common/                   # Shared UI components
│   ├── home/                     # Home screen (token list + codes)
│   ├── qrscanner/                # QR code scanner screen
│   ├── settings/                 # Settings screen
│   ├── theme/                    # Topty-specific theming
│   ├── token/                    # Add/Edit token screen
│   └── util/                     # UI utilities
└── util/
    └── DevTools.kt               # Development utilities
```

## Architecture

- **UI Framework**: Jetpack Compose with Material3
- **Navigation**: Jetpack Navigation Compose (type-safe routes)
- **Dependency Injection**: Koin
- **State Management**: ViewModel + StateFlow
- **Persistence**: Room database + DataStore Preferences
- **Camera**: CameraX for QR scanning
- **Barcode**: ML Kit Barcode Scanner
- **Image Loading**: Coil

## Key Components

### ToptyActivity
Entry point activity with splash screen support, edge-to-edge display, and theme configuration.

```kotlin
companion object {
    fun start(context: Context) {
        val intent = Intent(context, ToptyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
```

### Navigation Routes
Type-safe navigation using kotlinx.serialization:
```kotlin
object Route {
    data object Home
    data object QRScanner
    data object Settings
    sealed interface Token {
        data class Add(val uri: String?): Token
        data class Edit(val id: Long): Token
    }
}
```

### Main Screens
- **Home**: List of tokens with live-updating OTP codes
- **QRScanner**: Camera-based QR code scanning for token import
- **Token (Add/Edit)**: Manual token entry or editing
- **Settings**: App configuration (theme, font, code design, TTS)

### OTP Generation

**Supported Protocols:**
- `TOTP` - Time-based One-Time Password (RFC 6238)
- `HOTP` - HMAC-based One-Time Password (RFC 4226)

**Supported Hash Algorithms:**
- SHA1 (default)
- SHA256
- SHA512

**TOTPGenerator:**
```kotlin
object TOTPGenerator {
    fun generateCode(token: Token): TOTPCode {
        val counter = Math.floorDiv(System.currentTimeMillis(), token.period * 1000)
        // ... generates code using HMACGenerator
    }
}
```

### Token URI Format
Follows Google Authenticator Key URI Format:
```
otpauth://totp/ACME%20Co:john.doe@email.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30
```

**Supported Parameters:**
- `secret` (required) - Base32 encoded secret key
- `issuer` - Service provider name
- `algorithm` - SHA1, SHA256, SHA512
- `digits` - Code length (6-8)
- `period` - TOTP refresh interval (30-3600 seconds)
- `counter` - HOTP counter (required for HOTP)

### Database Entity

**Token:**
```kotlin
@Entity(tableName = "token")
data class Token(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val name: String,
    val issuer: String,
    val group: String,
    val note: String,
    val secret: String,
    val protocol: Protocol,
    val hashAlgo: HashAlgo,
    val period: Int,
    val counter: Long,
    val digits: Int,
)
```

### Code Models
```kotlin
sealed class Code {
    abstract val code: String
    abstract val token: Token
}

data class TOTPCode(
    override val code: String,
    override val token: Token,
    val periodStart: Long,
    val periodEnd: Long,
) : Code()

data class HOTPCode(
    override val code: String,
    override val token: Token,
) : Code()
```

### AppPrefsRepository (DataStore)
Persisted preferences:
- `uiMode`: UIMode enum (LIGHT, DARK, AUTO)
- `dynamicTheme`: Boolean for Material You colors
- `codeDesign`: CodeDesign enum for display style
- `uiFont`: UIFont enum for font customization
- `speakOutOnTap`: Boolean for TTS feature

### Text-to-Speech (TTS)
Accessibility feature that reads out OTP codes when tapped.

## Dependencies

### Local Modules
- `projects.commons` - Shared utilities

### Key Libraries
- AndroidX Compose BOM + Material3
- Navigation Compose
- Splash Screen API
- CameraX (camera2, lifecycle, view)
- ML Kit Barcode Scanner
- Coil (image loading)
- Koin (DI)
- Room (database)
- DataStore Preferences
- Accompanist Permissions
- Guava (utilities)
- kotlinx.serialization

## Build Configuration

- **Namespace**: `com.axondragonscale.topty`
- **Min SDK**: 31 (Android 12)
- **Java/Kotlin**: 21

## Resources

### Launcher Icon
- Accent Color: `#2196F3` (Blue)
- Icon files in `mipmap-*/topty_icon*`

### Constants
```kotlin
object Scheme {
    const val OTPAUTH = "otpauth"
}

object UriParam {
    const val SECRET = "secret"
    const val ISSUER = "issuer"
    const val ALGORITHM = "algorithm"
    const val DIGITS = "digits"
    const val PERIOD = "period"
    const val COUNTER = "counter"
}
```

## Data Flow

### Adding Token via QR Code
1. User opens QRScanner → CameraX preview starts
2. ML Kit scans frames for QR codes
3. QR detected → `TokenUriHandler.isValidTokenUri()` validates
4. Valid URI → Navigate to `Route.Token.Add(uri)`
5. `TokenUriHandler.parseTokenUri()` extracts token data
6. User confirms → `TokenRepository.saveToken()` persists to Room

### Adding Token Manually
1. User navigates to `Route.Token.Add(null)`
2. Manual entry form displayed
3. User fills fields → Token object created
4. Save → `TokenRepository.saveToken()` persists to Room

### Generating Codes
1. `HomeVM` observes `TokenRepository.getTokens()`
2. For each token, `TOTPGenerator.generateCode()` called
3. Codes refresh based on `period` (typically 30 seconds)
4. UI displays codes with countdown indicator

### Code Display with TTS
1. User taps code card
2. Code copied to clipboard
3. If `speakOutOnTap` enabled → `TTS.speak(code)`

## Code Style

- ViewModels: `HomeVM`, `QRScannerVM`, `TokenVM`, `SettingsVM`
- Type-safe navigation with `@Serializable` route classes
- Sealed classes for code types (TOTPCode, HOTPCode)
- Object singletons for generators (TOTPGenerator, HOTPGenerator, HMACGenerator)
- Extension properties for Protocol (`code` returns lowercase name)