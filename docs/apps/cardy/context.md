> Context: [Main](../../context.md) > Cardy

# Cardy - NFC Card Reader & Vault

## Overview

Cardy is an NFC-based smart card reader and secure vault within AppBox. Users tap contactless payment cards (Visa, Mastercard, Rupay, Amex, etc.) against their device to read EMV chip data — including card number, expiration, AID, AIP, CVM List, and other low-level tag data. Read cards can be saved to an encrypted local vault, accessible only after biometric authentication.

- **Package**: `com.axondragonscale.cardy`
- **Module path**: `:apps:cardy`
- **Key Tech**: Android NFC (IsoDep / NfcA), `libraries:tlv` (EMV TLV parsing), Room (encrypted storage), AES-256-GCM encryption, BiometricPrompt API

## Core Features

### 1. NFC Card Reading

- Tap any contactless EMV payment card to the device
- Automatically detect card type (Visa, Mastercard, Rupay, Amex, Discover, JCB, etc.)
- Read basic card info: PAN (masked/full), expiration date, cardholder name (if available)
- Communicate via ISO 7816-4 APDU commands over IsoDep
- Read ALL available applications on a card (multi-AID support)

### 2. Advanced EMV Data Viewer

- Display full parsed EMV response data in a structured, browsable format
- Key data elements:
  - **AID** (Application Identifier) — identifies the payment application
  - **Application Label** — human-readable app name (e.g. "VISA DEBIT")
  - **AIP** (Application Interchange Profile) — card capabilities bitfield
  - **AFL** (Application File Locator) — points to records containing card data
  - **CVM List** (Cardholder Verification Methods) — supported auth methods & priority
  - **CDOL1/CDOL2** (Card Risk Management Data Object Lists)
  - **Track 2 Equivalent Data** — magnetic stripe equivalent
  - **Application Expiration Date**
  - **Application PAN** (Primary Account Number)
  - **PAN Sequence Number**
  - **Issuer Country Code**
  - **Application Currency Code**
- Raw TLV (Tag-Length-Value) tree view for all response data
- Tag lookup: display EMV tag name + description alongside raw hex

### 3. Encrypted Card Vault

- Save any successfully read card to local encrypted storage
- Encryption: AES-256-GCM with a key derived from device-backed Android Keystore
- Stored data: full EMV response, parsed card info, card type, timestamp, auto-generated label
- Label auto-generated as "Network •••• Last4" (e.g. "Visa •••• 4821"), user-editable later
- User can delete saved cards

### 4. Biometric-Gated Access

- Viewing saved cards requires biometric authentication (fingerprint / face)
- Lock engages on every app launch (no configurable timeout — always locks)
- Graceful fallback to device credential (PIN/pattern/password) when biometric hardware is unavailable

### 5. UI / UX

- **Dark-mode-forward** premium wallet aesthetic
- Card network accent colors: Visa blue (#1A1F71), Mastercard red/orange (#EB001B/#F79E1B), Rupay teal (#097A6A), Amex blue (#006FCF)
- Clean, card-centric design with visual card representations
- Animated NFC tap prompt (pulsing/ripple animation encouraging card tap)
- Card type detected → show network logo + brand color
- EMV data presented in collapsible sections (summary → detail)
- Saved cards shown as visual card tiles with full PAN visible (post biometric auth)
- No Material You / dynamic theme — uses a custom dark palette with network-derived accents

## Planned Architecture

```
com.axondragonscale.cardy/
├── CardyActivity.kt              # Entry point, NFC foreground dispatch
├── CardyApplication.kt           # Koin initialization
├── di/
│   └── CardyModule.kt            # Koin module
├── nfc/
│   ├── NfcManager.kt             # NFC availability, foreground dispatch lifecycle
│   ├── CardReader.kt             # IsoDep communication, APDU command sequencing
│   ├── EmvCommands.kt            # APDU command builders (SELECT, READ RECORD, GPO)
│   └── ApduResponse.kt           # Response parsing, SW1/SW2 status handling
├── emv/
│   ├── EmvTags.kt                # Known EMV tag registry (tag → name + description)
│   ├── EmvDataExtractor.kt       # Extract structured data from parsed TLV (uses libraries:tlv)
│   └── CardType.kt               # Card network detection from AID
├── model/
│   ├── CardInfo.kt               # Parsed card summary (PAN, expiry, name, type)
│   ├── EmvData.kt                # Full structured EMV data
│   ├── CardNetwork.kt            # Enum: Visa, Mastercard, Rupay, Amex, etc.
│   └── SavedCard.kt              # UI model for vault display
├── database/
│   ├── CardyDatabase.kt          # Room database
│   ├── dao/
│   │   └── CardDao.kt            # Card CRUD
│   └── entity/
│       └── CardEntity.kt         # Encrypted card entity
├── security/
│   ├── CardEncryption.kt         # AES-256-GCM encrypt/decrypt using Keystore-backed key
│   ├── KeystoreManager.kt        # Android Keystore key generation & retrieval
│   └── BiometricAuthManager.kt   # BiometricPrompt wrapper
├── repository/
│   └── CardRepository.kt         # Card persistence, encrypt-on-save, decrypt-on-read
├── ui/
│   ├── Route.kt                  # @Serializable routes
│   ├── CardyApp.kt               # NavHost
│   ├── theme/                    # Color, Shape, Theme, Type
│   ├── common/                   # Shared composables (CardTile, NetworkLogo, etc.)
│   ├── home/                     # Tap prompt + recent reads
│   ├── reader/                   # Live read results + EMV data viewer
│   ├── vault/                    # Saved cards grid (biometric-gated)
│   ├── detail/                   # Full card detail + raw TLV explorer
│   └── settings/                 # NFC settings, security preferences
└── util/
    ├── HexExt.kt                 # ByteArray ↔ hex string extensions
    └── PanUtils.kt               # PAN formatting, Luhn validation, label generation
```

## Navigation Routes (Planned)

```kotlin
object Route {
    @Serializable data object Home           // NFC tap prompt + recent reads
    @Serializable data class Reader(         // Live EMV data from current tap
        val sessionId: String                // Identifies the current read session
    )
    @Serializable data object Vault          // Saved cards (biometric-gated)
    @Serializable data class Detail(         // Full card detail + TLV tree
        val cardId: Long
    )
    @Serializable data object Settings       // App preferences
}
```

## NFC Read Flow

```
1. User opens app → Home screen shows animated "Tap your card" prompt
2. NFC foreground dispatch enabled in onResume
3. Card detected → IsoDep.connect()
4. SELECT PPSE (2PAY.SYS.DDF01) → get available AIDs
5. For EACH available AID:
   a. SELECT AID → Get Processing Options (GPO)
   b. Parse AFL from GPO response → READ RECORD for each entry
   c. Parse all TLV data via `libraries:tlv` → Extract card info
6. Navigate to Reader screen with full EMV data (all applications)
7. User can optionally save → encrypt → store in vault
```

## APDU Command Sequence

```
┌─────────────────────────────────────────────────────┐
│ SELECT PPSE                                         │
│ 00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44   │
│ 46 30 31 00                                        │
├─────────────────────────────────────────────────────┤
│ Response: FCI Template with AID list                │
├─────────────────────────────────────────────────────┤
│ SELECT AID (e.g. A0000000041010 for Mastercard)     │
│ 00 A4 04 00 07 A0 00 00 00 04 10 10 00             │
├─────────────────────────────────────────────────────┤
│ Response: FCI with PDOL                             │
├─────────────────────────────────────────────────────┤
│ GET PROCESSING OPTIONS (with PDOL data)             │
│ 80 A8 00 00 [Lc] [PDOL data] 00                    │
├─────────────────────────────────────────────────────┤
│ Response: AIP + AFL                                 │
├─────────────────────────────────────────────────────┤
│ READ RECORD (for each AFL entry)                    │
│ 00 B2 [record] [SFI] 00                            │
├─────────────────────────────────────────────────────┤
│ Response: Card data records (PAN, expiry, etc.)     │
└─────────────────────────────────────────────────────┘
```

## Security Model

### Card Data Encryption

- **Algorithm**: AES-256-GCM (authenticated encryption)
- **Key Management**: Android Keystore-backed key (hardware-backed where available)
  - Key generated on first save, stored in Keystore (never leaves secure hardware)
  - No user password needed — device security IS the protection
- **What's encrypted**: Full EMV response blob + parsed sensitive fields (PAN, Track 2)
- **What's NOT encrypted**: Card network type, user label, timestamp (for display without auth)

### Biometric Authentication

- Required before decrypting / displaying saved card data
- Uses `BiometricPrompt` with `BIOMETRIC_STRONG` authenticators
- Lock triggers: every app launch (always locks, no timeout configuration)
- If biometrics unavailable: fallback to device credential (PIN/pattern/password)

## UI Screens

### Home

- Full-screen animated NFC tap prompt (centered card icon with pulsing rings)
- Bottom section: horizontally scrollable recent reads (last 5, not saved — ephemeral)
- FAB or bottom nav to access Vault
- Status indicator: NFC enabled/disabled with prompt to enable
- Graceful "NFC not available" state on devices without NFC hardware

### Reader (Post-Tap)

- **Card Summary Header**: Visual card representation with detected network logo, full PAN, expiry
- **Application Tabs/Selector**: If multiple AIDs found, user can switch between them
- **Collapsible Sections** (per application):
  - Basic Info (PAN, expiry, cardholder name, card type)
  - Application Data (AID, label, priority, AIP decoded)
  - Verification Methods (CVM List decoded into human-readable list)
  - Risk Management (CDOL1/CDOL2 decoded)
  - Raw TLV Tree (expandable tag-value pairs with tag descriptions)
- **Actions**: Save to Vault, Re-read

### Vault

- Grid of saved card tiles (2 columns)
- Each tile: network logo, full PAN, expiry, auto-generated label
- Biometric gate on entry (every launch)
- Long-press → delete option
- Empty state with prompt to tap first card

### Detail

- Full saved card data viewer (same layout as Reader but from stored data)
- Edit auto-generated label
- Delete card option

### Settings

- NFC status & system settings shortcut
- Clear all saved cards
- About / version

## Tech Stack

| Category | Libraries | Notes |
|----------|-----------|-------|
| NFC | Android NFC API (`android.nfc`) | IsoDep for ISO 14443-4 communication |
| EMV Parsing | `libraries:tlv` | BER-TLV recursive decoder from monorepo |
| Database | Room | Encrypted card storage |
| Encryption | Android Keystore + AES-256-GCM | Hardware-backed key, no user password |
| Biometrics | `androidx.biometric` | BiometricPrompt with BIOMETRIC_STRONG |
| Navigation | Navigation Compose | Type-safe @Serializable routes |
| DI | Koin | singleOf, viewModelOf |
| UI | Jetpack Compose + Material 3 | Card-centric, animation-heavy |
| Coroutines | kotlinx.coroutines + Flow | Async NFC reads, reactive UI |

## Manifest Requirements

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="false" />

<application android:name=".CardyApplication" ... >
    <activity android:name=".CardyActivity"
        android:launchMode="singleTop"> <!-- Required for NFC foreground dispatch -->
        <intent-filter>
            <action android:name="android.nfc.action.TECH_DISCOVERED" />
        </intent-filter>
        <meta-data
            android:name="android.nfc.action.TECH_DISCOVERED"
            android:resource="@xml/nfc_tech_filter" />
    </activity>
</application>
```

```xml
<!-- res/xml/nfc_tech_filter.xml -->
<resources>
    <tech-list>
        <tech>android.nfc.tech.IsoDep</tech>
    </tech-list>
</resources>
```

## Build Info

- **Namespace**: `com.axondragonscale.cardy`
- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36
- **Java/Kotlin**: JVM 21
- **Convention Plugin**: `alias(libs.plugins.android.app.convention)`

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| TLV parsing | Use `libraries:tlv` | Already in monorepo, avoid duplication |
| Multiple AIDs | Read all, browsable | More informative for power users |
| Card label | Auto-generate ("Visa •••• 4821"), editable | Frictionless save flow |
| PAN display | Full PAN visible after biometric auth | Convenience — biometric IS the gate |
| Export/backup | Not in v1 | Keep scope tight |
| NFC requirement | Graceful error (not required) | App installs everywhere, shows helpful state |
| Theme | Custom dark palette + network accents | Premium wallet feel, not generic Material You |
| Biometric config | Always locks on launch, no toggle/timeout | Simplicity, always-secure |

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- [TLV Library](../../libraries/tlv/) — BER-TLV parsing library used by Cardy for EMV data decoding
