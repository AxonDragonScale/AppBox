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
│   ├── AppPrefsRepository.kt       # the only DataStore; auto-off choice, deadline, restore snapshot
│   ├── QuickSettingsTileRepository.kt # reads/edits the shell's tile list
│   └── WirelessEndpointRepository.kt # local IPv4s + mDNS port lookup
├── background/
│   ├── AutoOffScheduler.kt         # AlarmManager one-shot
│   └── WirelessAutoOffReceiver.kt  # the alarm's only listener
├── permission/SecureSettingsPermission.kt # grant check, grant command, settings deep links
├── tile/                           # three TileServices, same repository
├── ui/
│   ├── Route.kt                    # Setup, Home, Settings, QuickSettingsTiles
│   ├── components/DevinatorTopBar.kt # the shared top bar: logo + name (+ subtitle, actions)
│   ├── theme/  setup/  home/  settings/  tiles/
└── util/
```

The suffixes carry one meaning each: `*Repository` owns a source of state (system settings, app
preferences, network), `*Scheduler` owns time, `*Permission` owns a permission. There is exactly
one implementation of each, so none of them is an interface.

### The one invariant

**`Settings.Global` is the only source of truth.** No switch state is ever persisted or cached. The
one exception is a **restore snapshot**: turning developer options off writes the restorable flags to
app prefs so the next enable can put them back; the snapshot is an intent, never read as state. The
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
    val endpoints: StateFlow<List<AdbEndpoint>>    // ranked Wi-Fi, hotspot, VPN, other — first is primary
    val autoOffDeadline: StateFlow<Instant?>       // null = no timer armed
    val autoOffDuration: StateFlow<AutoOffDuration> // the configured timer choice
    suspend fun setDeveloperOptions(enabled: Boolean): WriteResult
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
Disabling is silent and immediate. Turning developer options off first serializes every key in
`RESTORABLE_FLAG_KEYS` (currently USB and wireless) into the prefs snapshot, then writes `0` and zeroes
those keys. Turning it back on writes `1`; the observer sees the off→on transition and replays the
snapshot, so an enable from Android Settings restores too. A new developer toggle joins both sides by
appending its `Settings.Global` key to that list.

### Endpoint discovery (best effort)

- **Addresses**: IPv4 per interface via `ConnectivityManager` / `NetworkInterface`, labelled
  Wi-Fi / VPN / Hotspot. No permission.
- **Ranking**: the list is rank-ordered, not transport-ordered — Wi-Fi first, then hotspot, then VPN
  (a VPN address rarely routes from another machine and mDNS does not cross the tunnel). The UI draws
  only `endpoints.first()`; the network pill names that network. Rationale and the copy live in
  [design.md](./design.md#which-address-is-primary).
- **One port**: adbd runs a single TLS listener and advertises `_adb-tls-connect._tcp` on every
  interface at that port, so `AdbEndpoint.port` is identical for all candidates in a session.
- **Port**: `NsdManager` discovery of `_adb-tls-connect._tcp`, resolving each instance
  (`registerServiceInfoCallback` on API 34+, `resolveService` below).
- **Degradation rule**: unknown port → show the address without one and point at the Developer
  options screen. Discovery never blocks a toggle.

### Quick Settings tiles

Adding goes through `StatusBarManager.requestAddTileService` (API 33+), which raises the system's own
"Add … to Control center" prompt. There is no public API to remove a tile, so removal writes the
shell's tile list back without that entry.

Which setting holds that list depends on the device, and reading it depends on the app's target SDK:

| Store | Device | Readable? |
|---|---|---|
| `sysui_qs_tiles` (comma-separated, `custom(pkg/cls)` entries) | AOSP | Only up to target SDK 33 — `Settings.Secure.QS_TILES` is `@Readable(maxTargetSdk = TIRAMISU)`, and the provider rejects it above that for reads, `query()` and `call()` alike |
| `super_qs_tiles` (JSON grid, `items[].childItems[].spec`) | OriginOS | Yes — it is not a declared `Settings.*` key, so the restriction never applies |

The repository takes the first store it can read, extracts `custom(…)` entries with one regex (both
formats spell tiles the same way), and only ever rewrites the value it just read — so a failed read
means no write, never a list that drops the user's other tiles. A `ContentObserver` on both keys feeds
the same `Flow` the page collects, so a tile added from the system editor shows up too. Where no store
is readable, the page can only add, and the row cannot offer `Remove`.

### Auto-off timer

`AlarmManager.setAndAllowWhileIdle` (inexact, no permission) → `WirelessAutoOffReceiver` writes
`adb_wifi_enabled = 0`. Cancelled when wireless debugging is switched off manually, re-armed on each
enable. The choice is an `AutoOffDuration` (`OFF`, 5, 15, 30 min, default `OFF`): "off" is a decision
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
| OriginOS accepts the write but the platform ignores it | flip each switch, confirm in the Settings app | snackbar naming the failure; the switch stays where the database says |
| `adbd` keeps running after developer options is turned off | `adb devices` on the computer after switching off | always zero the debugging keys in the same write |
| Trust dialog is skinned or behaves differently | toggle wireless debugging on | the neutral "waiting for confirmation" state, never a hard error |
| OEM battery manager drops the timer alarm | set a 5-minute auto-off, lock the screen | move the timer to WorkManager |
| mDNS discovery finds nothing | check while on Wi-Fi | address without port |
| `sysui_qs_tiles` is unreadable above target SDK 33, so tile state and removal vanish on AOSP | open Quick Settings Tiles on a non-OriginOS device | read OriginOS's `super_qs_tiles` where present; otherwise the page is add-only |
| Several networks at once (Wi-Fi + VPN + hotspot) | connect with a VPN up | rank Wi-Fi → hotspot → VPN and show the top one only |

## Related Context

- [Design & behaviour](./design.md) — state machine, interaction and edge-case rules
- [UI spec](./ui.md) — screens, components, copy, tokens for Pencil
- [Coding conventions](../../coding-conventions.md)
- [Build system](../../build-system.md)
