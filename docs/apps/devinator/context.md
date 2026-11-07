> Context: [Main](../../context.md) > Devinator

# Devinator — Developer Settings Toggle

## Overview

One screen plus three Quick Settings tiles that flip **Developer options**, **USB debugging** and
**Wireless debugging**, show the `adb connect` endpoint while wireless debugging is on, and can
turn wireless debugging off on a timer.

- **Package**: `com.axondragonscale.devinator`
- **Module path**: `:apps:devinator`
- **Target device**: iQOO 13, OriginOS — OEM quirks are a first-class concern, see [Risks](#risks--verification)
- **Not** the `devtools` package in [`:core:commons`](../core/commons/context.md); that is the debug
  screen embedded inside other apps. Different thing, similar name.

Scope is deliberately small: three switches, tiles, endpoint display, auto-off timer. Everything
else is deferred — see [design.md](./design.md#out-of-scope-v1).

## How these switches work

| Switch | `Settings.Global` key | Note |
|---|---|---|
| Developer options | `DEVELOPMENT_SETTINGS_ENABLED` | public constant |
| USB debugging | `ADB_ENABLED` | public constant |
| Wireless debugging | `"adb_wifi_enabled"` | `@hide` since API 30 — use the string literal |

Reading needs no permission. Writing needs `WRITE_SECURE_SETTINGS`
(`signature|privileged|development`). The `development` flag is the loophole: the adb shell user may
grant it to any app that declares it, so one `adb shell pm grant` unlocks the app permanently. The
grant survives reboots and app updates; only uninstall revokes it.

No service is needed to make the toggles take effect: the platform's `AdbSettingsObserver` watches
the same keys and starts/stops `adbd` itself.

Two platform behaviours we design around, not against:

- Writing `adb_wifi_enabled = 1` raises SystemUI's **"Allow wireless debugging on this network?"**
  dialog. Until it is accepted the key still reads `0`.
- The wireless port is random per enable (30000–49999).

## Architecture

Single module, standard AppBox stack: Compose M3, Koin, Navigation Compose, Preferences DataStore
for app preferences only. No Room, no `INTERNET` permission.

```
com.axondragonscale.devinator/
├── DevinatorActivity.kt            # edge-to-edge; re-checks the grant on resume
├── DevinatorApplication.kt         # Koin start (CommonsModule + DevinatorModule)
├── di/DevinatorModule.kt
├── model/
│   ├── DevState.kt                 # the three flags, endpoints, write results
│   └── AutoOffDuration.kt          # Off / 5 / 15 / 30 min, the persisted timer choice
├── repository/
│   ├── DevSettingsRepository.kt    # the one writer; state, endpoints, timer, writes
│   ├── GlobalSettingsRepository.kt # the only code that touches Settings.Global
│   ├── AppPrefsRepository.kt       # the only DataStore; auto-off choice + deadline
│   └── WirelessEndpointRepository.kt # local IPv4s + mDNS port lookup
├── background/
│   ├── AutoOffScheduler.kt         # AlarmManager one-shot
│   └── WirelessAutoOffReceiver.kt  # the alarm's only listener
├── permission/SecureSettingsPermission.kt # grant check, grant command, settings deep links
├── tile/                           # three TileServices, same repository
├── ui/
│   ├── Route.kt                    # Setup, Home, Settings
│   ├── components/DevinatorTopBar.kt # the shared top bar: logo + name (+ subtitle, actions)
│   ├── theme/  setup/  home/  settings/
└── util/
```

The suffixes carry one meaning each: `*Repository` owns a source of state (system settings, app
preferences, network), `*Scheduler` owns time, `*Permission` owns a permission. There is exactly
one implementation of each, so none of them is an interface.

### The one invariant

**`Settings.Global` is the only source of truth.** No switch state is ever persisted or cached. The
repository exposes a `StateFlow` fed by a `ContentObserver`, so Home, the Setup gate and the tiles
cannot disagree — including after a change made in the Settings app, or a write the system rejected.

```kotlin
class DevSettingsRepository(
    settings: GlobalSettingsRepository,
    endpoints: WirelessEndpointRepository,
    autoOff: AutoOffScheduler,
    prefs: AppPrefsRepository,
) {
    val state: StateFlow<DevState>
    val endpoints: StateFlow<List<AdbEndpoint>>    // VPN first, then Wi-Fi, then hotspot
    val autoOffDeadline: StateFlow<Instant?>       // null = no timer armed
    suspend fun setDeveloperOptions(enabled: Boolean, alsoDisableDebugging: Boolean): WriteResult
    suspend fun setUsbDebugging(enabled: Boolean): WriteResult
    suspend fun setWirelessDebugging(enabled: Boolean): WriteResult
    suspend fun setAutoOffDuration(duration: AutoOffDuration)
    fun autoOffDuration(): AutoOffDuration
}

sealed interface WriteResult {
    data object Ok : WriteResult
    data object AwaitingConsent : WriteResult      // trust dialog is up, or was declined
    data class Failed(val reason: String) : WriteResult
}
```

Read through one helper. A key written by `adb shell settings put` is stored as text while
`putInt` stores an integer, and `getInt` throws on the text form — `getString` reads both:

```kotlin
fun readFlag(key: String): Boolean =
    (Settings.Global.getString(resolver, key)?.toIntOrNull() ?: 0) != 0
```

### Write path

`setWirelessDebugging(true)`: write `1`, then poll the key 10× at 250 ms. Reads `1` → `Ok`. Still
`0` → `AwaitingConsent`; the observer pushes the real value whenever the user accepts or walks away.
Disabling is silent and immediate. Turning developer options off writes `0` and, when
`alsoDisableDebugging` is set, also zeroes `ADB_ENABLED` and `ADB_WIFI_ENABLED`.

### Endpoint discovery (best effort)

- **Addresses**: IPv4 per interface via `ConnectivityManager` / `NetworkInterface`, labelled
  Wi-Fi / VPN / Hotspot. No permission.
- **Port**: `NsdManager` discovery of `_adb-tls-connect._tcp`, resolving each instance
  (`registerServiceInfoCallback` on API 34+, `resolveService` below).
- **Degradation rule**: unknown port → show the address without one and point at the Developer
  options screen. Discovery never blocks a toggle.

### Auto-off timer

`AlarmManager.setAndAllowWhileIdle` (inexact, no permission) → `WirelessAutoOffReceiver` writes
`adb_wifi_enabled = 0`. Cancelled when wireless debugging is switched off manually, re-armed on each
enable. The choice is an `AutoOffDuration` (`OFF`, 5, 15, 30 min, default 30): "off" is a decision
the user makes, not a missing value, so it is an enum rather than a nullable count. The deadline is a
nullable pref — absent means no timer — persisted so the countdown survives process death.
Known gap: the alarm is not restored automatically after reboot; it comes back on the next app
start.

### Setup / grant

Detection is `SecureSettingsPermission.isGranted(context)` (`checkSelfPermission`), re-checked on
`ON_RESUME` and by a deliberate "Check again". Nothing about setup is persisted: the permission is
itself the source of truth, so a revoked grant shows Setup again. Setup is the NavHost's start
destination; once the grant is read, `SetupScreen` navigates to Home with
`popUpTo<Route.Setup> { inclusive = true }`, which keeps Home the only reachable destination and off
the back stack. The command shown on screen is built from
`context.packageName`, never hardcoded: the convention plugin suffixes debug builds, so a debug
install is `com.axondragonscale.devinator.debug` and a hardcoded grant would target a package that
is not installed.

```sh
adb shell pm grant <context.packageName> android.permission.WRITE_SECURE_SETTINGS
```

Deep link for step 1: `Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS`. Shizuku and root grants
are out of scope for v1.

### Build & manifest

- `libs.plugins.android.app.convention` + `kotlin.serialization`; deps: `:core:commons`, Compose BOM
  and bundle, Koin bundle, `navigation.compose`, coroutines, `kotlinx.serialization.json`.
- `<uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS"
  tools:ignore="ProtectedPermissions" />`
- Three tile services: `exported="true"`, `android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"`,
  each with a label and icon. Auto-off receiver: `exported="false"`.
- Launcher icon: `drawable/ic_launcher_foreground.xml` is the Devinator mark on the 108 dp grid —
  one path with three subpaths (accent capsule plus caret and knob wound as knockouts,
  `fillType="nonZero"`), so the same drawable doubles as the monochrome layer under a system tint.
  Background is `@color/devinator_icon_background` (`#0E1013`). Source art:
  `apps/devinator/devinator.fig` → Foundations › `05 · App icon`. Do not split the glyph into
  stacked shapes; the knockouts are what keep the themed layer working. `drawable/ic_devinator_logo.xml`
  is the same path at 28×16 for the Setup top bar, tinted `primary` at the call site.
- `INTERNET` is **not** declared in this module, but the merged manifest requests it: `:core:commons`
  brings `INTERNET`, `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`
  and `FOREGROUND_SERVICE` (HttpClient + Chucker in debug). Confirmed with
  `aapt2 dump permissions` on the debug APK. The app makes no outbound requests; the permission is
  there for local mDNS discovery. Stripping it with `tools:node="remove"` would break commons'
  client, so the claim in the UI is "no data collected", not "no network permission".

## Risks & verification

Everything here is verified the first time the app is installed on the iQOO.

| Risk | How to check | Fallback if it bites |
|---|---|---|
| OriginOS accepts the write but the platform ignores it | flip each switch, confirm in the Settings app | banner + "Open Developer options" deep link instead of a silent no-op |
| `adbd` keeps running after developer options is turned off | `adb devices` on the computer after switching off | make the cascade dialog mandatory, always zero the debugging keys |
| Trust dialog is skinned or behaves differently | toggle wireless debugging on | the neutral "waiting for confirmation" state, never a hard error |
| OEM battery manager drops the timer alarm | set a 5-minute auto-off, lock the screen | move the timer to WorkManager |
| mDNS discovery finds nothing | check while on Wi-Fi | address without port |
| Several networks at once (Wi-Fi + VPN + hotspot) | connect with a VPN up | list every candidate, VPN first |

## Related Context

- [Design & behaviour](./design.md) — state machine, interaction and edge-case rules
- [UI spec](./ui.md) — screens, components, copy, tokens for Pencil
- [Coding conventions](../../coding-conventions.md)
- [Build system](../../build-system.md)
