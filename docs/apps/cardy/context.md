---
title: "Cardy — NFC card reader"
status: current
summary: "NFC EMV reader that parses contactless card data with libraries:tlv and stores cards in a biometric-gated, AES-256-GCM vault."
read_when: "Changing NFC reads, APDU/EMV parsing, the vault, or Cardy's screens."
---

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

## Architecture

```
com.axondragonscale.cardy/
├── CardyActivity.kt              # FragmentActivity: splash, edge-to-edge, NFC reader mode
├── CardyApplication.kt           # Koin initialization
├── di/CardyModule.kt             # Koin module
├── nfc/
│   ├── NfcManager.kt             # NFC availability, enableReaderMode lifecycle
│   ├── CardReader.kt             # IsoDep communication, APDU sequencing
│   ├── EmvCommands.kt            # APDU builders (SELECT PPSE, SELECT AID, GPO, READ RECORD)
│   └── ApduResponse.kt           # SW1/SW2 parsing
├── emv/
│   ├── EmvTag.kt                 # Known EMV tag registry (tag → name + description)
│   ├── EmvDataExtractor.kt       # Structured data from parsed TLV (uses libraries:tlv)
│   ├── CardNetwork.kt            # Network detection from AID prefix
│   ├── Track2EquivalentData.kt   # Track 2 (tag 57) decoding
│   └── interpreter/              # Per-tag value interpretation
├── model/                        # CardReadResult, EmvApplication, ParsedTlvNode
├── database/
│   ├── CardyDatabase.kt
│   ├── dao/CardDao.kt
│   └── entity/CardEntity.kt      # Encrypted card entity
├── security/
│   ├── KeystoreManager.kt        # Android Keystore key generation & retrieval
│   ├── CardEncryption.kt         # AES-256-GCM encrypt/decrypt
│   └── BiometricAuthManager.kt   # BiometricPrompt wrapper
├── repository/CardRepository.kt  # Encrypt-on-save, decrypt-on-read
├── ui/
│   ├── Route.kt, CardyApp.kt     # Routes + NavHost
│   ├── common/PaymentCard.kt     # Shared card visual
│   ├── home/                     # Tap prompt, live read result, recent reads
│   ├── detail/                   # Full card detail + raw TLV tree
│   ├── vault/                    # Saved cards grid (biometric-gated)
│   ├── settings/
│   └── theme/
└── util/HexExt.kt                # ByteArray ↔ hex
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data object Home                       // Tap prompt + live read + recent reads
    @Serializable data class CardDetail(val cardId: Long = -1L)
    @Serializable data object Vault                      // Saved cards (biometric-gated)
    @Serializable data object Settings
}
```

There is no separate reader destination: `CardyApp` keeps the Home entry alive and gates NFC reader
mode on the current route, handing tags to `HomeVM::readTag`.

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

UI sources live under `apps/cardy/src/main/java/com/axondragonscale/cardy/ui/`.

### Home

- Full-screen animated NFC tap prompt (pulsing rings) with `Tap your card` → `Reading card...` status text
- Bottom bar: Vault and Settings
- Read failures surface as a snackbar
- NFC disabled / not supported state with a button to open system NFC settings

### Detail (`ui/detail/DetailScreen.kt`)

One screen serves both a fresh tap and a saved card; `DetailMode` picks which. There is no separate
Reader destination.

- **Card header**: `PaymentCard` with network, PAN or last four digits, expiry
- **Application selector**: filter chips, one per AID found
- **Basic Info**: PAN, expiry, network
- **Application Data**: AID, label, priority, CVM list, AIP, effective and expiry dates, each rendered through `InterpretableHexRow`
- **Raw TLV**: tree and flat views, covering every tag including unknown ones
- **Top-bar actions**: Save to Vault on a fresh read; rename and delete on a saved card

### Vault (`ui/vault/VaultScreen.kt`)

- Grid of `PaymentCard` tiles built from the unencrypted columns only (network, last four digits, expiry, label) — nothing is decrypted until Detail
- Biometric gate on entry, tracked by `isSessionAuthenticated`
- Long-press → delete with confirmation
- Empty state prompting the first tap

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

## Behaviour and limits

What the reader already does — check here before "fixing" something that is not broken:

- **APDU status handling**: `CardReader.transceive()` recovers from `SW_6C` (re-sends with the
  corrected `Le`) and `SW_61` (issues `GET RESPONSE`).
- **AID discovery**: PPSE first, then probes the known AIDs in `CardNetwork` when PPSE is absent or empty.
- **PDOL**: minimal read-only terminal data — TTQ `26000000`, amount `000000000001`, today's date,
  transaction type `00`, random UN; unrequested tags are zero-filled.
- **PAN / expiry**: tags `5A` / `5F24` with a Track 2 (`57`) fallback.
- **Per-application isolation**: an application that fails to read is skipped and the rest of the
  read still returns — no fail-fast.

Not implemented, and each needs protocol work rather than a new tag:

- **GENERATE AC** (`80 AE`). `EmvCommands` has only SELECT PPSE, SELECT AID, GPO and READ RECORD, so
  cards never produce `9F26` / `9F27` / `9F36` / `9F10`. Adding a cryptogram means adding the
  command and its CDOL1 data, not just registering a tag.
- **Offline data authentication** (SDA / DDA / CDA).
- **Cardholder verification** (PIN entry) and **issuer script processing**.
- **IIN-based network detection**: the seven networks (Visa, Mastercard, Rupay, Amex, Discover, JCB,
  UnionPay) are matched by AID prefix, so a co-branded card reports the first prefix match.

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- [TLV Library](../../libraries/tlv/context.md) — BER-TLV parsing library used by Cardy for EMV data decoding
