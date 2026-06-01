# Cardy Implementation Plan

## Status: ✅ Complete

---

## Phase 1: Project Foundation
> Build system, manifest, application class, DI skeleton, theme

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1.1 | Update `build.gradle.kts` with all required dependencies | ✅ Done | Room, KSP, Koin, Navigation, Biometric, Serialization, Coroutines, tlv lib |
| 1.2 | Update `AndroidManifest.xml` (NFC permission, Application class, singleTop, tech filter) | ✅ Done | |
| 1.3 | Create `res/xml/nfc_tech_filter.xml` | ✅ Done | |
| 1.4 | Create `CardyApplication.kt` with Koin init | ✅ Done | |
| 1.5 | Create `di/CardyModule.kt` (skeleton) | ✅ Done | |
| 1.6 | Create custom dark theme (`ui/theme/`) | ✅ Done | Dark-forward with network accent colors |
| 1.7 | Create `ui/Route.kt` with navigation routes | ✅ Done | |
| 1.8 | Create `ui/CardyApp.kt` NavHost skeleton | ✅ Done | |
| 1.9 | Update `CardyActivity.kt` (splash, edge-to-edge, NFC dispatch) | ✅ Done | |

## Phase 2: NFC & EMV Core
> NFC communication, APDU commands, EMV parsing, card type detection

| # | Task | Status | Notes |
|---|------|--------|-------|
| 2.1 | Create `util/HexExt.kt` (ByteArray ↔ hex extensions) | ✅ Done | |
| 2.2 | Create `nfc/ApduResponse.kt` (SW1/SW2, success check) | ✅ Done | |
| 2.3 | Create `nfc/EmvCommands.kt` (SELECT PPSE, SELECT AID, GPO, READ RECORD) | ✅ Done | |
| 2.4 | Create `emv/EmvTags.kt` (known tag registry) | ✅ Done | |
| 2.5 | Create `emv/CardNetwork.kt` (AID → network detection) | ✅ Done | |
| 2.6 | Create `emv/EmvDataExtractor.kt` (TLV → structured data using libraries:tlv) | ✅ Done | |
| 2.7 | Create `model/` data classes (CardInfo, EmvData, EmvApplication) | ✅ Done | CardReadResult, EmvApplication, ParsedTlvNode |
| 2.8 | Create `nfc/CardReader.kt` (full APDU sequence, multi-AID) | ✅ Done | |
| 2.9 | Create `nfc/NfcManager.kt` (foreground dispatch lifecycle) | ✅ Done | |

## Phase 3: Security & Persistence
> Encryption, biometric auth, Room database, repository

| # | Task | Status | Notes |
|---|------|--------|-------|
| 3.1 | Create `security/KeystoreManager.kt` (Android Keystore key gen/retrieval) | ✅ Done | |
| 3.2 | Create `security/CardEncryption.kt` (AES-256-GCM encrypt/decrypt) | ✅ Done | |
| 3.3 | Create `security/BiometricAuthManager.kt` | ✅ Done | |
| 3.4 | Create `database/entity/CardEntity.kt` | ✅ Done | |
| 3.5 | Create `database/dao/CardDao.kt` | ✅ Done | |
| 3.6 | Create `database/CardyDatabase.kt` | ✅ Done | |
| 3.7 | Create `repository/CardRepository.kt` | ✅ Done | + SerializableCardData + CardReadResultExt |
| 3.8 | Wire all into `CardyModule.kt` | ✅ Done | |

## Phase 4: UI — Home Screen
> NFC tap prompt with animations, NFC status, recent reads

| # | Task | Status | Notes |
|---|------|--------|-------|
| 4.1 | Create `ui/home/HomeVM.kt` | ✅ Done | |
| 4.2 | Create `ui/home/HomeUiState.kt` + events | ✅ Done | Inlined in HomeVM.kt |
| 4.3 | Create `ui/home/HomeScreen.kt` (tap prompt, animations, NFC status) | ✅ Done | |

## Phase 5: UI — Reader Screen
> Post-tap EMV data display, collapsible sections, multi-AID tabs

| # | Task | Status | Notes |
|---|------|--------|-------|
| 5.1 | Create `ui/reader/ReaderVM.kt` | ✅ Done | |
| 5.2 | Create `ui/reader/ReaderUiState.kt` + events | ✅ Done | Inlined in ReaderVM.kt |
| 5.3 | Create `ui/common/` shared composables (CardVisual, NetworkLogo, etc.) | ✅ Done | CardVisual with network gradients |
| 5.4 | Create `ui/reader/ReaderScreen.kt` (card header, collapsible sections, TLV tree) | ✅ Done | |

## Phase 6: UI — Vault & Detail
> Biometric-gated saved cards grid, detail view

| # | Task | Status | Notes |
|---|------|--------|-------|
| 6.1 | Create `ui/vault/VaultVM.kt` | ✅ Done | |
| 6.2 | Create `ui/vault/VaultScreen.kt` (biometric gate, card grid, empty state) | ✅ Done | |
| 6.3 | Create `ui/detail/DetailVM.kt` | ✅ Done | |
| 6.4 | Create `ui/detail/DetailScreen.kt` | ✅ Done | |

## Phase 7: UI — Settings & Polish
> Settings screen, final wiring, build verification

| # | Task | Status | Notes |
|---|------|--------|-------|
| 7.1 | Create `ui/settings/SettingsScreen.kt` | ✅ Done | |
| 7.2 | Final wiring: NavHost, Activity NFC intent handling → VM | ✅ Done | CardyActivity → FragmentActivity |
| 7.3 | Build verification (`assembleDebug`) | ✅ Done | BUILD SUCCESSFUL |

---

## Dependencies between phases
```
Phase 1 (Foundation) → Phase 2 (NFC/EMV) → Phase 3 (Security/DB)
                                                      ↓
Phase 4 (Home UI) ← Phase 2                   Phase 5 (Reader UI)
                                                      ↓
                                               Phase 6 (Vault/Detail)
                                                      ↓
                                               Phase 7 (Settings/Polish)
```

## Key Constraints
- All phases are sequential (shared files, DI wiring)
- Each sub-agent gets a self-contained task with explicit file paths and content requirements
- The plan file itself tracks progress and deviations
