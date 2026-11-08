> Context: [Main](../../context.md) > Devinator

# Devinator — Developer Settings Toggle

## Overview

One screen plus five Quick Settings tiles that flip **Developer options**, **USB debugging** and
**Wireless debugging**, show the `adb connect` endpoint while wireless debugging is on, turn
wireless debugging off on a timer, and can temporarily pause or resume **Developer options** for a
chosen duration.

- **Package**: `com.axondragonscale.devinator`
- **Module path**: `:apps:devinator`
- **Target device**: iQOO 13, OriginOS — OEM quirks are a first-class concern, see [Risks](#risks--verification)
- **Not** the `devtools` package in [`:core:commons`](../core/commons/context.md); that is the debug
  screen embedded inside other apps. Different thing, similar name.

Scope is deliberately small: three switches, tiles, endpoint display, the timed toggle and the
wireless auto-off timer. Everything else is deferred — see [design.md](./design.md#out-of-scope-v1).

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
├── DevinatorActivity.kt            # edge-to-edge; host for DevinatorApp and theme prefs
├── DevinatorApplication.kt         # Koin start (CommonsModule + DevinatorModule)
├── di/DevinatorModule.kt
├── model/
│   ├── DevState.kt                 # the three flags, endpoints, write results
│   ├── DurationOption.kt           # Off / 5 / 15 / 30 min for both timers
│   ├── Countdown.kt                # one `m:ss` formatter and expiry check
│   └── TimedToggleSession.kt       # target, restoreTo, deadline
├── repository/
│   ├── DevSettingsRepository.kt    # the one writer; state, endpoints, timer, writes
│   ├── TimedToggleRepository.kt    # temporary on/off policy, duration, recovery
│   ├── GlobalSettingsRepository.kt # the only code that touches Settings.Global
│   ├── AppPrefsRepository.kt       # the only DataStore; auto-off choice, deadline, restore snapshot, UI mode, dynamic colour
│   ├── QuickSettingsTileRepository.kt # reads and removes entries from the shell's tile list
│   └── WirelessEndpointRepository.kt # local IPv4s + mDNS port lookup + connectivity refresh
├── background/
│   ├── DeadlineAlarm.kt            # one one-shot AlarmManager alarm
│   ├── DevAlarms.kt                # the auto-off and timed-toggle alarms
│   ├── KoinReceiver.kt             # the goAsync window every receiver shares
│   ├── WirelessAutoOffReceiver.kt  # the wireless alarm's only listener
│   ├── TimedToggleReceiver.kt      # the timed alarm's only listener
│   └── DevBootReceiver.kt          # reconciles both timers after a reboot
├── permission/
│   ├── SecureSettingsPermission.kt # grant check, grant/revoke commands, settings deep link
│   ├── GrantRepository.kt          # the app's one grant source (application context)
│   └── LocalNetworkAccess.kt       # the API-37 local-network gate, shared by Home and discovery
├── tile/
│   ├── DevTile.kt                  # one catalogue: component, label, icon, description
│   ├── ListeningTileService.kt     # shared tile lifecycle
│   ├── DevTileServices.kt          # developer options, USB, wireless
│   └── TimedDevOptionsTileServices.kt # pause and resume
├── ui/
│   ├── Route.kt                    # Setup, Home, Settings, Permission, QuickSettingsTiles
│   ├── components/                 # card, divider, top bars, badge, chip, code block, switch, clipboard
│   ├── theme/  setup/  home/  settings/  tiles/
```

The suffixes carry one meaning each: `*Repository` owns a source of state (system settings, app
preferences, network), `*Alarm` owns a one-shot alarm, `*Permission` owns a permission. There is
exactly one implementation of each, so none of them is an interface.

### The one invariant

**`Settings.Global` is the only source of truth.** No switch state is ever persisted or cached. The
exceptions are **intents**: the developer-flags restore snapshot, and the timed toggle's
`restoreTo`/`deadline`. They describe what to do next, never what the switches are. The repository
exposes a `StateFlow` fed by a `ContentObserver`, so Home, the Setup gate and the tiles cannot
disagree — including after a change made in the Settings app, or a write the system rejected.

```kotlin
class DevSettingsRepository(
    settings: GlobalSettingsRepository,
    endpoints: WirelessEndpointRepository,
    alarms: DevAlarms,
    prefs: AppPrefsRepository,
) {
    val state: StateFlow<DevState>
    val endpoints: StateFlow<List<AdbEndpoint>>    // ranked Wi-Fi, hotspot, VPN, other — first is primary
    val autoOffDeadline: StateFlow<Instant?>       // null = no timer armed
    val autoOffDuration: StateFlow<DurationOption> // the configured timer choice
    suspend fun setDeveloperOptions(enabled: Boolean): WriteResult
    suspend fun setUsbDebugging(enabled: Boolean): WriteResult
    suspend fun setWirelessDebugging(enabled: Boolean): WriteResult
    suspend fun setAutoOffDuration(duration: DurationOption)
}

sealed interface WriteResult {
    data object Ok : WriteResult
    data object AwaitingConsent : WriteResult      // trust dialog is up, or was declined
    data object Failed : WriteResult
}
```

Read through one helper. A key written by `adb shell settings put` is stored as text while
`putInt` stores an integer, and `getInt` throws on the text form — `getString` reads both:

```kotlin
fun readFlag(key: String): Boolean =
    (Settings.Global.getString(resolver, key)?.toIntOrNull() ?: 0) != 0
```

### Write path

`setWirelessDebugging(true)`: write `1`, then wait up to 3 s for the observer to report the key as
`1`. Reads `1` → `Ok`. Still `0` → `AwaitingConsent`; the observer pushes the real value whenever
the user accepts or walks away.
Every ordinary write is read back, because a provider can accept the call and ignore it (the OriginOS
risk); a value that did not land is a `Failed`, not a silent success. A wireless enable is the one
exception: its key legitimately stays `0` until consent, so the `putInt` Boolean is checked and only
an accepted write enters the consent wait. Restoring a snapshot writes wireless through the same
unverified path; USB and every disable are verified. Disabling is silent and
immediate. Turning developer options off first serializes every key in `RESTORABLE_FLAG_KEYS`
(currently USB and wireless) into the prefs snapshot, then writes `0` and zeroes
those keys. The live key makes a duplicate off idempotent, so a second tap cannot overwrite the first
snapshot. Turning it back on writes `1` and restores the snapshot directly; the observer path stays
for enables made in Android Settings.

### Endpoint discovery (best effort)

- **Addresses**: IPv4 per interface via `ConnectivityManager` / `NetworkInterface`, labelled
  Wi-Fi / VPN / Hotspot. No permission.
- **Refresh**: a `ConnectivityManager.NetworkCallback` re-reads the addresses on every network
  change, so airplane mode empties the card without waiting for mDNS. The local-network grant only
  gates discovery; the addresses still track the network without it.
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

### Theme

`DevinatorTheme` takes `darkTheme` and `dynamicColor`, and `DevinatorActivity` feeds both from
`AppPrefsRepository`: `uiMode` (`UIMode`, default `AUTO`) and `dynamicColor` (default `false`). Both are
read in `setContent`, so a change in Settings repaints the whole app.

The fixed palette is the shipped look. Under dynamic colour Material You sets `surface` equal to
`background`, which flattens every card into the page, so the theme steps the tones
(`surfaceContainer` in dark, `surfaceContainerLowest` in light, `surfaceContainerHigh` for insets).
`DevinatorColors` (the hero card's green and the off-state red) stay fixed either way.

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
is readable, removal cannot be offered. On API < 33 there is no public add API either, so the page
points at the Quick Settings editor instead of pretending to write the shell's list. The USB and
Wireless tiles render `STATE_UNAVAILABLE` with "Dev options off" while the master switch is off,
and the tile tick calls `reconcileDeadline()` so a dropped alarm heals while the shade is open. The
repository also rejects an enable of USB or wireless while developer options is off; the tile state is
only the visible half of that rule.

### Auto-off timer

`DevAlarms.autoOff` arms `AlarmManager.setAndAllowWhileIdle` (inexact, no permission) →
`WirelessAutoOffReceiver` writes `adb_wifi_enabled = 0`. The repository owns the persisted deadline; the
alarm only knows when to fire. Cancelled when wireless debugging is switched off manually, re-armed
on each enable. The choice is a `DurationOption` (`OFF`, 5, 15, 30 min, default `OFF`): "off" is a
decision the user makes, not a missing value, so it is an enum rather than a nullable count. The
deadline is a nullable pref — absent means no timer — persisted so the countdown survives process
death. An expired deadline is acted on, not replaced with a fresh duration: the next observation, the
visible countdown reaching zero, or `DevBootReceiver` turns wireless debugging off. A reboot therefore
no longer leaves the timer lost. If the deadline pref is missing when the app first observes wireless
on, it arms one fresh duration from that moment; a real off→on transition always arms one.

### Timed toggle

`Pause Dev options` and `Resume Dev options` are two fixed-direction tiles; the Home card exposes the
same actions. A session stores the state before it started, writes the target through
`DevSettingsRepository` (so USB and wireless follow the snapshot path), arms `DevAlarms.timedToggle`
(`AlarmManager.setAndAllowWhileIdle`, request code 1), and restores the pre-session state at expiry.
One session at a time: tapping the owning tile ends it early; tapping the opposite tile switches
direction; any manual Developer options change cancels it, including a change that races the session
write. The repository owns the deadline; the alarm is a pure trigger. The visible countdown also
reconciles when it reaches zero, so a dropped alarm does not leave the session stuck on screen.

The duration is a `DurationOption` (5 / 15 / 30 min, default `MIN_5`), persisted in
`AppPrefsRepository` along with `timedToggleDeadline` and `timedToggleRestoreTo`. The alarm receiver
restores through the repository, so the result arrives like every other write.
`DevBootReceiver` restores on boot; `TimedToggleRepository.reconcile()` handles a force-stop
(expired → restore, future → re-arm). No new permission, no foreground service, no notification.

### Setup / grant

Detection is `SecureSettingsPermission.isGranted(context)` (`checkSelfPermission`), re-checked on
`ON_RESUME` at the NavHost level, so every screen returns to Setup when the grant disappears. Nothing
about setup is persisted: the permission is itself the source of truth, so a revoked grant shows
Setup again. Setup is the NavHost's start destination; once the grant is read, `SetupScreen`
navigates to Home with `popUpTo<Route.Setup> { inclusive = true }`, which keeps Home the only
reachable destination and off the back stack. The command shown on screen is built from
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
- `<uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" />` — the platform's
  local-network protection (API 37+), needed only for the mDNS port lookup. It is `dangerous`, so Home
  requests it when wireless debugging is switched on and a refusal costs the port, never the toggle.
  Declaring it is not enough on its own: the connectivity module's "choose a device" picker keys off
  the **grant**, so `WirelessEndpointRepository.start()` skips discovery until it is granted rather
  than letting the platform substitute that picker. `LocalNetworkAccess` is the single gate used by
  both Home and discovery.
- Five tile services: `exported="true"`, `android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"`,
  each with a label and icon. Auto-off, timed-toggle and boot receivers: `exported="false"`. The
  page's catalogue (`tile/DevTile.kt`) is the single source for each tile's component, label and icon.
- `RECEIVE_BOOT_COMPLETED` is declared for `DevBootReceiver`; the rest of the permission list is
  unchanged.
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
| Timed toggle alarm is late or lost after a force-stop | start a timed pause, force-stop, reopen the app | `TimedToggleRepository.reconcile()` restores an expired session or re-arms a live one; `DevBootReceiver` covers a normal reboot |
| Wireless auto-off alarm is lost after a reboot or force-stop | set a 5-minute auto-off, reboot, check the card and `adb devices` | `DevBootReceiver` and the next app start both act on an expired deadline; a future deadline is re-armed |
| mDNS discovery finds nothing | check while on Wi-Fi | address without port |
| Local-network grant is missing or revoked | launch on a device that never granted it | discovery is skipped, so no picker and no port; the caption points at Developer options |
| `sysui_qs_tiles` is unreadable above target SDK 33, so tile state and removal vanish on AOSP | open Quick Settings Tiles on a non-OriginOS device | read OriginOS's `super_qs_tiles` where present; otherwise the page is read-only and points at the Quick Settings editor |
| Several networks at once (Wi-Fi + VPN + hotspot) | connect with a VPN up | rank Wi-Fi → hotspot → VPN and show the top one only |

## Related Context

- [Design & behaviour](./design.md) — state machine, interaction and edge-case rules
- [UI spec](./ui.md) — screens, components, copy, tokens for Pencil
- [Coding conventions](../../coding-conventions.md)
- [Build system](../../build-system.md)
