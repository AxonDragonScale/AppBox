> Context: [Main](../../context.md) > [Devinator](./context.md) > UI

# Devinator — UI Spec

Visual spec for building the screens in Pencil. Behaviour rules live in [design.md](./design.md);
keys and permissions live in [context.md](./context.md). Copy below is literal.

## Frame setup

- Phone frame **412 × 915**, 4-pt grid, screen side padding **16**.
- Everything below is dp at 1×.
- Two themes: **dark** (primary target) and **light**. Draw both for Home; either for the rest.
- Corner radii: card `20`, inner item `12`, button `16`, pill `999`.

## Tokens

| Token | Dark | Light |
|---|---|---|
| `bg` | `#0E1013` | `#F7F9FB` |
| `surface` (card) | `#171A1F` | `#FFFFFF` |
| `surfaceHigh` (inset rows, code) | `#20242B` | `#EEF1F5` |
| `outline` (dividers, muted icons) | `#313740` | `#D3D9E1` |
| `text` | `#E6E9EE` | `#131720` |
| `textMuted` | `#98A0AC` | `#5A6472` |
| `primary` / `onPrimary` | `#5FD3A6` / `#05231A` | `#0E6E52` / `#FFFFFF` |
| `on` (status dot) | `#5FD3A6` | `#0E6E52` |
| `error` / `errorContainer` | `#F2B8B5` / `#3A1414` | `#B3261E` / `#FCE4E2` |

Runtime uses Material You dynamic colour on top of this fallback palette; design against the
fallback so contrast is verified.

| Type role | Size / weight | Use |
|---|---|---|
| Screen title | 22 / 600 | top bar |
| Card title | 16 / 600 | switch titles, card headers |
| Row label | 14 / 500 | secondary rows |
| Caption | 12 / 400 `textMuted` | explanations, hints |
| Mono | 13 / 400 mono, letterSpacing 0 | commands, `ip:port` |
| Button | 15 / 600 | primary actions |
| Chip | 11 / 500 | network chip, timer chip |

Icons: Material Symbols `developer_mode`, `usb`, `wifi_tethering`, `content_copy`, `add`, `timer`,
`settings`, `arrow_back`, `key`, `error`, `open_in_new`, `grid_view`.

## Components

Define these once and reuse by name in the trees below.

- **Card** — vertical frame, `fill_container`, `padding 16`, `gap 8`, `cornerRadius 20`, fill `surface`.
- **SwitchRow** — horizontal, `fill_container`, `height 56`, `gap 12`, `justifyContent space_between`,
  `alignItems center`. Children: 40-wide icon slot (24 icon), text column (title + caption, gap 2),
  M3 switch 52×32. Off track `surfaceHigh`, on track `primary`.
- **IconBadge** — 40×40, `cornerRadius 12`, `surfaceHigh`, centred 20 icon.
- **CodeBlock** — horizontal, `fill_container`, `padding 12`, `cornerRadius 12`, `surfaceHigh`,
  mono text `fill_container` with `textGrowth "fixed-width"`, plus a 32 copy icon.
- **StatusChip** — pill, `padding 8`, `cornerRadius 999`, `surfaceHigh`, 8 dot + `Chip` label.
- **Button** — `fill_container`, `height 52`, `cornerRadius 16`, fill `primary`, `Button` label.
- **Divider** — 1 high, `fill_container`, `outline` at 60% opacity.

## Home

```
Screen 412×915 · fill bg · vertical
├─ TopBar · h64 · padding 8 · horizontal · space_between · alignItems center
│  ├─ column gap2: "Devinator" 22/600 · "Developer options & ADB" 12 textMuted
│  └─ icon button 48 · settings
└─ column · fill_container · padding 16 · gap 12
   ├─ Banner            (failure state only — see Deltas)
   ├─ Card
   │  ├─ SwitchRow  title "Developer options"  caption "System-wide switch apps check"  icon badge developer_mode
   │  ├─ Divider
   │  ├─ SwitchRow  title "USB debugging"      caption "ADB over cable"                icon usb
   │  └─ SwitchRow  title "Wireless debugging" caption "ADB over Wi-Fi"                icon wifi_tethering
   ├─ Card  (visible while wireless debugging is on)
   │  ├─ row space_between: "Connect from your computer" 14/600 · copy icon 32
   │  ├─ EndpointRow ×N  chip "Wi-Fi" · "192.168.1.42:37129" mono
   │  ├─ "Port changes every time wireless debugging is turned back on." 12 textMuted
   │  ├─ Divider
   │  └─ row space_between h44: "Auto-off" 14 · StatusChip "12:04 left"
   ├─ Card · horizontal · gap 12 · alignItems center
   │  ├─ IconBadge grid_view
   │  ├─ column gap2 fill: "Quick Settings tiles" 16/600 · "One tap from the shade." 12 textMuted
   │  └─ text button "Add" 15/600 primary
   └─ spacer fill_container
```

**EndpointRow** — horizontal, `height 40`, `padding 8`, `cornerRadius 12`, `surfaceHigh`, `gap 8`:
network pill (`Chip`) + mono `ip:port` (`fill_container`, fixed-width). Order: VPN, Wi-Fi, Hotspot.
The row displays the address only; tapping it copies `adb connect <ip>:<port>`. Visible and copied
text differ on purpose: the command is for the terminal, the address is for reading.

## Home states (deltas only)

| State | Change to the tree above |
|---|---|
| Developer options off | USB and Wireless rows at 38% opacity, switches disabled, captions replaced by "Turn on Developer options first". Endpoint card hidden |
| Wireless pending consent | Wireless row: switch replaced by a 20 spinner; caption "Confirm the system dialog" |
| Wireless on, port unknown | EndpointRow shows `192.168.1.42` only and a caption "Port is on the Developer options screen" |
| No network | Endpoint card kept, one row "No network" 14 textMuted, no EndpointRows |
| Write failed | Banner above the switch card: `errorContainer`, `cornerRadius 12`, `padding 12`, row [error icon, "Couldn't change this setting" 14, text button "Open Developer options"] |

## Setup

Shown instead of Home while `WRITE_SECURE_SETTINGS` is missing.

```
Screen 412×915 · fill bg · vertical
├─ TopBar · h64 · padding 8: "Devinator" 22/600
└─ column · fill_container · padding 20 · gap 20
   ├─ column gap 8
   │  ├─ IconBadge 56 · cornerRadius 16 · primary tint · key icon
   │  ├─ "One-time setup" 24/600
   │  └─ "Devinator needs one system permission to flip these switches. Android only grants it from
   │     a computer over ADB — once, and it survives reboots and updates." 14 textMuted
   ├─ Card · gap 16
   │  ├─ Step 1 · row gap 12
   │  │  ├─ step marker "1" 12/600 primary, 24 circle surfaceHigh
   │  │  └─ column gap 6: "Enable USB debugging" 16/600 · "Settings → System → Developer options →
   │  │     USB debugging" 12 textMuted · [text button "Open Developer options"]
   │  ├─ Divider
   │  ├─ Step 2 · row gap 12
   │  │  └─ column gap 6: "Run this command" 16/600 · CodeBlock mono
   │  │     "adb shell pm grant com.axondragonscale.devinator android.permission.WRITE_SECURE_SETTINGS"
   │  │     · "Paste it in a terminal on the computer that has adb installed." 12 textMuted
   │  ├─ Divider
   │  └─ Step 3 · row gap 12
   │     └─ column gap 6: "Come back here" 16/600 · "Devinator checks automatically when you return."
   │        12 textMuted
   ├─ spacer fill_container
   ├─ Button "Check again"
   └─ "Setup stays done until you uninstall the app." 12 textMuted · centred
```

Setup states: after a failed check, add "Permission not granted yet." 12 in `error` under the
button. No spinner state — the check is instant.

The command text is composed from `context.packageName` at runtime, so a debug install reads
`adb shell pm grant com.axondragonscale.devinator.debug …`. Design against the release name; the
shape of the line, not the exact package, is what matters.

## Settings

```
TopBar h64: arrow_back + "Settings"
column · padding 16 · gap 12
├─ Card · row h56 space_between: "Appearance" 16/600 · segmented System / Light / Dark
├─ Card
│  ├─ row h56 space_between: "Write secure settings" 16/600 · StatusChip "Granted" (on dot)
│  ├─ Divider
│  └─ row h56 space_between: "How to re-grant" 14 · chevron
├─ Card · row h56 space_between: "Open Developer options" 16/600 · open_in_new
└─ Card: "Devinator" 16/600 · "Version 1.0.0" 12 textMuted · "No network permission. No data
   collected. Nothing leaves this device." 12 textMuted
```

## Overlays

**Turn off developer options** — dialog, `cornerRadius 28`, `padding 24`, max width 320:

```
Title   "Turn off Developer options?"   20/600
Body    "USB and wireless debugging stop working immediately. A connected adb session drops."  14 textMuted
Check   [x] "Also turn off USB and wireless debugging"  14
Actions [Cancel] text · [Turn off] filled, error colour
```

**Auto-off menu** — bottom sheet or popup menu: `5 min` · `15 min` · `30 min` · `Off`, current item
checkmarked, caption "Wireless debugging turns itself off when the timer ends."

**Snackbars** — "Copied" · "Opened Developer options" · "Wireless debugging not confirmed"
(neutral, not error). One line, `cornerRadius 12`; no Undo action here.

## Quick Settings tiles

Three 2×1 tiles, standard shade layout: icon 32 top-left of a 24-inset block, label at the bottom.
Active = `primary` container, inactive = `surfaceHigh` with `textMuted` content.

| Tile | Icon | Label | Secondary label |
|---|---|---|---|
| Developer options | `developer_mode` | "Dev options" | "On" / "Off" |
| USB debugging | `usb` | "USB debugging" | "On" / "Off" |
| Wireless debugging | `wifi_tethering` | "Wireless debugging" | "On" / "Off" (+ " · 12:04" while the timer runs) |

Also draw the **Add tiles** system prompt frame: title "Add Devinator to Quick Settings?", body
naming the three tiles, Cancel / Add.

## Frames to draw

1. Home — dark, developer options and wireless on, endpoint card visible
2. Home — light, same content
3. Home — developer options off (disabled rows)
4. Home — wireless pending consent (spinner, dialog impression)
5. Home — write-failed banner
6. Setup — default, dark
7. Setup — permission still missing (error line under the button)
8. Settings — dark
9. Turn-off dialog over Home
10. QS tiles — three tiles, on and off
11. Add-tiles system prompt

## Related Context

- [Design & behaviour](./design.md) — states, interactions, edge cases
- [Module context](./context.md) — keys, permission, risks
