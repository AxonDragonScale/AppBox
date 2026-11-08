> Context: [Main](../../context.md) > [Devinator](./context.md) > UI

# Devinator — UI Spec

Visual spec for building the screens in Pencil. Behaviour rules live in [design.md](./design.md);
keys and permissions live in [context.md](./context.md). Copy below is literal.

## Frame setup

- Phone frame **412 × 915**, 4-pt grid, screen side padding **16**.
- Everything below is dp at 1×.
- **Both palettes ship.** Every frame is drawn in the dark palette; the light palette is the same
  layout with the token values in the next table. Settings can opt into dynamic colour, but the
  fallback palette is the reviewed look.
- Corner radii: card `20`, inner item `12`, button `16`, pill `999`. Switch `52×32`; segmented
  control `36` high with `32` segments; the duration chip is `36` inside a `48` tap target.

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
| `primarySurface` (hero card) | `#15241E` | `#E8F3EE` |
| `primaryContainer` (hero badge, state chip) | `#1F3A30` | `#D3EBE1` |
| `primaryOutline` (hero card border) | `#2B4C3F` | `#A4CFBD` |
| `on` (status dot) | `#5FD3A6` | `#0E6E52` |
| `error` / `errorContainer` | `#F2B8B5` / `#3A1414` | `#B3261E` / `#FCE4E2` |
| `errorSurfaceHigh` (off-state badge, chip, switch track) | `#4E2323` | `#F6CFCB` |
| `errorOutline` (off-state card border) | `#774A49` | `#E5A5A1` |

The app ships this palette by default. Dynamic colour (Material You) is off until Settings turns it
on; the fallback above is what every device without a wallpaper theme renders. Verify contrast
against the values above.

| Type role | Size / weight | Use |
|---|---|---|
| Screen title | 22 / 600 + logo | top bar |
| Hero title | 20 / 600 | Developer options card |
| Card title | 16 / 600 | switch titles, card headers, page rows |
| Tweak row title | 15 / 600 | Tweaks rows — the same role as `Button` |
| Group label | 13 / 600 | list headings, beside a hairline rule |
| Row label | 14 / 500 | secondary rows |
| Caption | 12 / 400 `textMuted` | explanations, hints |
| Mono (endpoint) | 18 / 400 mono, letterSpacing 0 | `ip:port` in the wireless card |
| Mono (command) | 12 / 400 mono, letterSpacing 0 | commands to copy |
| Button | 15 / 600 | primary actions |
| Chip | 12 / 500 | network pill, status chip, timer value |

Nothing in the app's own content is smaller than 12. The two exceptions are system fidelity: Quick Settings
tile labels (11 / 10) and the annotation tags drawn onto reference frames.

Icons: Material Symbols `developer_mode`, `usb`, `wifi_tethering`, `content_copy`, `add`, `timer`,
`expand_more`, `settings`, `arrow_back`, `key`, `error`, `open_in_new`, `grid_view`, `check`, `close`.

## Components

Define these once and reuse by name in the trees below.

- **TopBar** — component `TopBar`, instanced into every screen: `fill_container`, `height 64`,
  `padding 16`, `gap 10`, logo 28×16 tinted `primary`, then a column `gap 2` with "Devinator" 22/600
  and the tagline "Developer options, minus the hassle." 12 `textMuted`. Home places a trailing
  settings button (48 tap target) beside the instance.
- **Card** — vertical frame, `fill_container`, `padding 16`, `gap 8`, `cornerRadius 20`, fill `surface`.
- **SwitchRow** — horizontal, `fill_container`, `height 56`, `gap 12`, `justifyContent space_between`,
  `alignItems center`. The whole row is the toggle; the switch is a non-interactive Material 3
  `Switch` `52×32` with a 48 dp touch target. Children: 40-wide icon slot (24 icon), text column
  (title + caption, gap 2), switch. Off track `surfaceHigh`, on track `primary`. The thumb carries a
  14 glyph in the track's colour either way — `check` in `primary` when on, `close` in `surfaceHigh`
  when off — so no switch renders as an empty circle.
- **HeroCard** — the master-switch card: `fill_container`, `padding 16`, `cornerRadius 20`, fill
  `primarySurface` with a 1 px `primaryOutline` stroke. One row `gap 12` centred: 56 IconBadge, a
  text column `gap 4` (hero title 20/600, then a row `gap 8` with StatusChip and the 12 `textMuted`
  caption), and a 52×32 HeroSwitch. Off state: fill `errorContainer`, stroke `errorOutline`.
- **HeroSwitch** — 52×32, `cornerRadius 16`, 4 padding, 24 thumb, 14 glyph. On: `primary` track,
  `onPrimary` thumb, `primary` check. Off: `errorSurfaceHigh` track, `error` thumb, `errorContainer`
  close glyph.
- **IconBadge** — `surfaceHigh`, centred icon. Hero size 56×56, `cornerRadius 16`, 28 icon, fill
  `primaryContainer` with a `primary` icon (off state: `errorSurfaceHigh` with an `error` icon);
  row size 40×40, `cornerRadius 12`, 20 icon.
- **StepRow** — row, `gap 12`: 24 marker circle (`surfaceHigh`, 12/600 `primary` digit), text column
  `gap 6` holding title 16/600, caption 12 muted and the optional 48-high text action
  (13/600 `primary`, text top-aligned so the visual position matches the drawing).
- **CodeBlock** — horizontal, `fill_container`, `padding 12`, `cornerRadius 12`, `surfaceHigh`,
  mono command text at 12 `fill_container`, then a 16 copy glyph in `textMuted`. The whole block is
  the copy target; the glyph marks the action. A one-line endpoint is centred in the 48 dp block; a
  command that wraps keeps its text and glyph at the top, so the glyph never floats beside a middle
  line. The component decides from the rendered line count.
- **StatusChip** — pill, `height 24`, `padding 8`, `cornerRadius 999`, 6 dot + `Chip` label at 12/500
  (default fill `surfaceHigh`, `on` dot for a live state, `textMuted` for neutral). On the hero card
  the fill is `primaryContainer` with dot and label both `primary`; the off state is
  `errorSurfaceHigh` with both `error`.
- **EndpointRow** — `fill_container`, `padding 12`, `cornerRadius 12`, `surfaceHigh`, row `gap 12`
  centred: info column `gap 4` (meta row `gap 6`: network pill + “Must be on this network” 12
  `textMuted`; then the mono 18 address), plus a 16 copy glyph in `textMuted`. One row only; the
  glyph copies `adb connect <ip>:<port>`.
- **AutoOffRow** — `fill_container`, `height 48`, `gap 12`, centred: `timer` 18 `textMuted`, label
  14/500 `fill_container`, then a trailing **DurationChip** (36) centred in the row. Opens the duration
  menu; it is a control, not a readout. It is a standard Material `DropdownMenu`: content-width,
  anchored to the trailing control, with a 16 dp container and the menu's 8 dp content inset applied
  horizontally as well; the 8 dp selected row uses `primaryContainer` with a `primary` check, so
  16 − 8 = 8 keeps the corners concentric. `Off` turns the chip's value and caret `error`
  and the caption reads “Stays on until you turn it off.”; an armed timer replaces the caption with
  “Wireless debugging turns off in 12:04.”, the time in `primary` 600.
- **DurationChip** — the collapsed duration picker, used twice: at the timed-toggle header and as the
  AutoOffRow's trailing control. `height 36`, `padding horizontal 12`, `gap 6`, `cornerRadius 12`,
  fill `surfaceHigh`, 1 px `outline` stroke; value 14/600 and `expand_more` 18 both in `primary`, or
  both in `error` when the value is `Off`. The chip is centred in a 48 dp tap target and opens the
  shared `DropdownMenu`.
- **NoticeCard** — `fill_container`, `padding 12`, `cornerRadius 16`, fill `errorContainer`, row
  `gap 10` aligned top: 18 lock icon in `error`, then a column `gap 4` with a 13/600 `error` line and
  a 12 `error` line at 75% opacity. Used for the one permission explanation.
- **ErrorCard** — same fill at `cornerRadius 12`, row `gap 10` centred: 18 `error` icon + 13/600
  `error` label. Used for a state message above the primary action.
- **Button** — `fill_container`, `height 52`, `cornerRadius 16`, fill `primary`, `Button` label.
- **Divider** — 1 high, inset **16 on both sides**, `outline` at 60% opacity. It never runs the full
  card width: rows are full-bleed and their ripple reaches the corners, the divider stays inside them.

## Home

```
Screen 412×915 · fill bg · vertical
├─ TopBar · component `TopBar` · trailing icon button 48 · settings
└─ column · fill_container · padding 16 · gap 12
   ├─ HeroCard · developer options
   │  └─ row · gap 12 · items centre
   │     ├─ IconBadge 56 · primaryContainer · 28 developer_mode primary
   │     ├─ column · grow · gap 4
   │     │  ├─ "Developer options" 20/600
   │     │  └─ row · gap 8 · items centre: StatusChip "On" · "Master switch for ADB" 12 textMuted
   │     └─ HeroSwitch · 52×32
   ├─ Card · timed toggle · gap 12
   │  ├─ Header row · gap 12 · items centre
   │  │  ├─ IconBadge 40 · surfaceHigh · 20 timer
   │  │  ├─ column · grow · gap 2: "Timed toggle" 16/600 · caption 12 textMuted
   │  │  └─ Duration control · 48 tap target ▸ DurationChip “5 min”
   │  └─ Button · "Pause for 5 min" 52
   ├─ Card · USB debugging
   │  └─ SwitchRow  title "USB debugging"  caption "ADB over USB"  icon usb
   ├─ Card · wireless debugging · gap 12
   │  ├─ SwitchRow  title "Wireless debugging"  caption "ADB over Wi-Fi or hotspot"  icon wifi_tethering
   │  ├─ EndpointRow   pill "Wi-Fi" · "Must be on this network" · "192.168.1.42:37129" mono 18 · copy 16
   │  ├─ CodeBlock   "adb connect 192.168.1.42:37129" · copy 16
   │  ├─ Divider
   │  └─ column · gap 4
   │     ├─ AutoOffRow   timer 18 · “Auto-off” 14/500 · DurationChip “Off”
   │     └─ "Stays on until you turn it off." 12 textMuted
   └─ Card · pages   (normal flow — never pinned to the bottom)
      ├─ PageRow   h 64 · IconBadge tune · "Tweaks" 16/600 · "Animation and app behaviour" 12 textMuted · chevron_right 20
      ├─ Divider
      └─ PageRow   h 64 · IconBadge grid_view · "Quick Settings Tiles" 16/600 · "Add tiles to Quick Settings" 12 textMuted · chevron_right 20
```

**Pages card.** The two pages Home can reach share one card of 64 dp rows. A sixth 80 dp card does not fit: the last card ends at `y=763` of `851`, and one more card plus its gap needs 92. The rows use the Tiles page's row anatomy rather than Home's card anatomy, and the whole row is the target.

**Timed toggle.** The card sits directly under the hero and stays enabled when Developer options is off — Resume is the way back on. Its caption and button follow the current state:

| State | Caption | Button |
|---|---|---|
| Developer options on, no timer | "Off for a few minutes, then back on." | "Pause for 5 min" |
| Developer options off, no timer | "On for a few minutes, then back off." | "Resume for 5 min" |
| Pause timer active | "Turns back on in 4:32." — time in `primary` 600 | "Resume now" |
| Resume timer active | "Turns back off in 4:32." — time in `primary` 600 | "Pause now" |

The duration control opens a content-width `5 / 15 / 30 min` menu, default 5; the 16 dp container, 8 dp all-round inset and 8 dp selected row match the auto-off menu, with the selected row in `primaryContainer` and a `primary` check. The button is the primary 52-high button; the card never gets a second action.

**Duration chip.** Both duration controls use the same `DurationChip` component: a 36-high outlined
field (radius 12, `surfaceHigh`, 1 px `outline`) with the value 14/600 and `expand_more` 18 in
`primary`, centred in the 48 dp tap target. Auto-off's `Off` value switches the value and caret to
`error` while the container stays neutral; the timed toggle has no `Off` option, so it is always
primary. The chip opens the content-width menu described above.

**One address and one port.** The card shows the top-ranked address only, the same thing Android's
own wireless debugging screen does. Addresses rank Wi-Fi (the phone's active network) → Hotspot →
VPN → other; the network pill names the network the computer must be on. Every address shares one
adbd port, so a second row was never a choice of transport. If a fallback is ever needed, it belongs
behind the pill, not on the card.

Both copy glyphs are icon-only. The one on the endpoint copies `adb connect <ip>:<port>`; the one on
the command block copies the command. Visible and copied text differ on purpose: the address is for
reading, the command for the terminal. Android 13+ draws its own clipboard confirmation; Android
12/12L get a toast.

## Home states (deltas only)

| State | Change to the tree above |
|---|---|
| Developer options off | HeroCard becomes `errorContainer` with a 1 px `errorOutline` border; the badge, status chip and switch track become `errorSurfaceHigh`; the badge icon, dot, label and switch thumb become `error` (the thumb's close glyph is `errorContainer`). The title stays `text` and the caption stays `textMuted`. USB and Wireless cards collapse to one 38%-opacity row each, captions "Turn on Developer options first"; the endpoint, command and auto-off are hidden. This is the only place red appears — a state signal, not a failed write. Turning it back on restores the USB and wireless values captured when it went off |
| Wireless off | The wireless card is a single switch row; endpoint, command, divider and auto-off are hidden |
| Wireless pending consent | Wireless header switch replaced by a 20 spinner; caption "Confirm the system dialog"; endpoint, command and auto-off hidden |
| Wireless on, port unknown | Endpoint address shown without a port; the note becomes "Port is on the Developer options screen" |
| No network | Endpoint row shows "No network" 14 `textMuted`; pill and copy glyph hidden; command block hidden; auto-off stays |
| Multiple networks | Only the top-ranked address is shown; the pill names its network |
| Auto-off is `Off` | AutoOffRow value reads "Off" in `textMuted`; caption reads "Stays on until you turn it off." |
| Auto-off armed | Value shows the choice ("5 min"…); caption becomes "Wireless debugging turns off in 12:04." with the time in `primary` 600 |
| Timed pause active | Developer options is off and USB/wireless are disabled as in the off state; the timed card caption reads "Turns back on in 4:32." and the button reads "Resume now" |
| Timed resume active | Developer options is on; USB and wireless may be off (the restored snapshot), shown as enabled switch rows; the timed card caption reads "Turns back off in 4:32." and the button reads "Pause now" |
| Duration menu open | A standard content-width `surfaceHigh` DropdownMenu under the duration control (148 wide in the design frame): 5 / 15 / 30 min; 16 dp container, 8 dp all-round inset, 8 dp selected row in `primaryContainer` with a check |
| Write failed | Snackbar "Couldn't change this setting."; the switch stays where the settings database says |

The pages card is identical in every Home state: it is a navigation, not a control, so the
Developer-options-off and timed screens show it unchanged.

## Tweaks

Reached from the pages card. One scrolling page of grouped rows; every row is a write, so there is
no Save button and no confirmation.

```
TopBar h64 · back arrow + "Tweaks"
column · padding 16
├─ GroupLabel "Animation"   13/600 text · 1 px outline rule filling the rest of the 380 row
├─ gap 8
├─ Card · gap 0, rows 60 high, Divider between them
│  ├─ TweakRow  badge 40 animation · "Window animation" 15/600 · "Windows opening and closing" 12 textMuted · chip "1×"
│  ├─ Divider
│  ├─ TweakRow  … · chip "0.5×"
│  ├─ Divider
│  └─ TweakRow  … · chip "Off"  — value and caret in error
├─ gap 24 · GroupLabel "Behaviour" · gap 8 · Card (4 rows, one of them a chip)
├─ gap 20
├─ ResetRow   h 48 · cornerRadius 16 · surface · "Reset to defaults" 15/600 primary, centred
├─ gap 12
└─ "Reset restores the stock value for every tweak." 12 textMuted
```

**Row.** `height 60`, `padding horizontal 16`, `gap 12`, items centred: 40 IconBadge, a column
`weight 1` with `gap 4` (title 15/600, caption 12 at 16 line-height), then either the switch or the
chip. A toggle row is `toggleable` as a whole with `role = Switch`, and the switch itself is
non-interactive — the row is the target, the switch is a readout.

**Chip.** Choice rows reuse `DurationChip`, centred in a 48 dp tap target, and open the shared
content-width menu. Value 14/600 and `expand_more` 18 in `primary`; an animation scale set to `Off`
turns both `error`; the process limit prints `Standard` for `-1`.

| Group | Rows | Control |
|---|---|---|
| Animation | Window / Transition / Animator animation | chip: Off, 0.5×, 1×, 1.5×, 2×, 5×, 10×; stock 1× |
| Behaviour | Don't keep activities, Background processes, Show all ANRs, Stay awake while charging | switch, chip (`Standard`, 0–4), switch, switch |

**Nothing here needs the input overlays.** Show taps, Pointer location and Layout bounds are not on
this page, and cannot be: the first two are `Settings.System` *private* keys the platform only lets
root/shell/system write (an app with `WRITE_SECURE_SETTINGS` gets `IllegalArgumentException: You
cannot change private secure settings`), and Layout bounds is the `debug.layout` **system property**,
which an app UID cannot set. All three work through Android's own Developer options, which runs as
system. See [context.md](./context.md#tweaks-page).

**Reset.** One tap writes the stock value for every row on the page — the switches off, the scales
1×, the limit `Standard`, stay-awake off. It reports a failure as a snackbar and changes nothing if
the grant is gone. A row that fails does not stop the rest.

**Honesty.** Values are read back from the settings provider after every write and observed live, so
a change made in Android's own Developer options screen lands on this page without a refresh, and a
rejected write leaves the row showing what the database says. Nothing here is persisted, and none of
these keys join the developer-flags snapshot: stock Android keeps them when Developer options goes
off, so zeroing them would be a surprise.

| State | Change to the tree above |
|---|---|
| Value menu open | A standard content-width `surfaceHigh` DropdownMenu under the chip: 16 dp container, 8 dp all-round inset, 8 dp selected row in `primaryContainer` with a `primary` check |
| Write failed | Snackbar "Couldn't change this setting."; the row keeps the database value |
| A key the OEM ignores | The row shows what the database reports. There is no optimistic flip to unwind |

## Setup

Shown instead of Home while `WRITE_SECURE_SETTINGS` is missing.

```
Screen 412×915 · fill bg · vertical
├─ TopBar · component `TopBar`
└─ column · fill_container · padding 20 horizontal / 16 vertical · gap 16
   ├─ Header · row gap 12 · items centre
   │  ├─ IconBadge 40 · cornerRadius 12 · surfaceHigh · 20 key icon
   │  └─ column gap 2: "One-time setup" 20/600 · "One permission, granted once over ADB." 12 textMuted
   ├─ NoticeCard
   │  ├─ "Devinator needs the Write Secure Settings permission to control Developer options." 13/600
   │  └─ "It is safe to grant and is used only to adjust Developer options." 12 at 75% opacity
   ├─ Card · gap 16
   │  ├─ Step 1 · row gap 12
   │  │  ├─ step marker "1" 12/600 primary, 24 circle surfaceHigh
   │  │  └─ column gap 6: "Enable USB debugging" 16/600 · "Settings → System → Developer options →
   │  │     USB debugging" 12 textMuted · [text action "Open Developer options" 48 high, 13/600 primary]
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
   ├─ column gap 16: ErrorCard (state only) · Button "Check again" · "Setup stays done until you
   │  uninstall the app." 12 textMuted centred
```

Setup states: after a failed check, show an ErrorCard above the button reading "Permission not
 granted yet." No spinner state — the check is instant.

The command text is composed from `context.packageName` at runtime, so a debug install reads
`adb shell pm grant com.axondragonscale.devinator.debug …`. Design against the release name; the
shape of the line, not the exact package, is what matters.

## Settings

```
TopBar h64: arrow_back + "Settings"
column · padding 16 · gap 12
├─ Card
│  ├─ row h56 space_between: "Appearance" 16/600 · segmented System / Light / Dark (36 high,
│  │  32 segments, selected `primaryContainer` with `primary` text)
│  ├─ Divider · inset 16 both sides
│  └─ row h56 space_between: "Dynamic colour" 16/600 · M3 switch 52×32 (default off)
├─ Card
│  ├─ row h56 space_between: "Write secure settings" 16/600 · StatusChip "Granted" (`primaryContainer` fill, `primary` dot and label)
│  ├─ Divider · inset 16 both sides
│  └─ row h56 space_between: "Grant or revoke" 16/600 · chevron
├─ Card · row h56 space_between: "Open Developer options" 16/600 · open_in_new
└─ Card · row h56 space_between: "Devinator" 16/600 · "Version 1.0.0" 14/500 textMuted
```

Every navigation label is 16/600: two rows of the same kind must not be two different sizes. The
status row carries a chip instead of a chevron, which is what keeps it from reading as tappable.

Appearance is the only card with two controls: the segmented control picks System / Light / Dark, and
`Dynamic colour` layers Material You over that choice. Dynamic colour defaults off, because the app
ships its own palette and only the fallback palette has been contrast-reviewed.

Auto-off is not repeated here: it belongs on Home, beside the wireless switch it applies to, and it is
meaningless while wireless debugging is off.

The chevron and `open_in_new` are the two navigation idioms: a chevron stays inside the app, an
external-link glyph leaves it. Never use one for the other.

## Permission

Reached from Settings › Grant or revoke. Both directions of the one permission, because neither
command can run from inside the app: this page is reference, which is why it is a page and not a
sheet. Each block copies its own command.

```
TopBar h64: arrow_back + "Permission"
column · padding 16 · gap 12
├─ "Devinator needs the Write Secure Settings permission to change Developer options." 14 text
├─ "Grant" 16/600
├─ "Run this on a computer that has adb installed:" 12 textMuted
├─ CodeBlock · mono 12 · copy 16
├─ Divider · full content width, 60% outline
├─ "Revoke" 16/600
├─ "To take the permission back — before uninstalling, for example:" 12 textMuted
├─ CodeBlock · mono 12 · copy 16
└─ "The grant survives reboots and app updates. Revoking it leaves Devinator unable to change
   anything until it is granted again." 12 textMuted
```

The intro stays at 14 so the two section titles are the loudest thing on the page.
Both commands are composed from `context.packageName`, so a debug install reads `.debug`.

## Overlays

**Auto-off menu** — a content-width popup menu anchored to the AutoOffRow's trailing value: `Off` ·
`5 min` · `15 min` · `30 min`, 16 dp container, 8 dp all-round inset, 8 dp selected row in
`primaryContainer` with a `primary` check. The row's
trailing value always mirrors the current choice, including `Off` — the control must look like a
control, not a readout. The explanatory line is not in the menu; the row's caption carries it
("Wireless debugging turns off in 12:04.") while a timer is armed.

**Snackbars** — "Opened Developer options" · "Wireless debugging not confirmed" (neutral) ·
"Couldn't change this setting." (failure). One line, `cornerRadius 12`; no Undo action here. There is
no confirmation dialog for any toggle.

## Quick Settings Tiles

Reached from the Home tiles card. One row per tile, and the row itself is the control.

```
TopBar h64: arrow_back + "Quick Settings Tiles"
column · padding 16 · gap 12
├─ "Choose which Devinator tiles appear in the shade." 12 textMuted
├─ Card · gap 0 · no padding — rows run edge to edge
│  ├─ TileRow · title "Developer options"  caption "Master switch for ADB"  icon developer_mode
│  ├─ Divider · inset 16 both sides
│  ├─ TileRow · title "Pause Dev options"  caption "Off for 5 min, then back on."  icon timer_pause
│  ├─ Divider · inset 16 both sides
│  ├─ TileRow · title "Resume Dev options"  caption "On for 5 min, then back off."  icon timer_play
│  ├─ Divider · inset 16 both sides
│  ├─ TileRow · title "USB debugging"  caption "ADB over USB"  icon usb
│  ├─ Divider · inset 16 both sides
│  └─ TileRow · title "Wireless debugging"  caption "ADB over Wi-Fi or hotspot"  icon wifi_tethering
└─ "New tiles land at the end — rearrange them in the Quick Settings editor." 12 textMuted
```

**Timed rows follow the setting.** The Pause and Resume captions use the current timed-toggle duration, so a 15-minute choice reads "Off for 15 min, then back on."

**TileRow** — `fill_container`, `height 64`, `padding 4/16`, `gap 12`, items centre, no corner radius:
full-bleed like a list item, so the press ripple reaches the card's edges, and the 4 vertical padding
belongs to the row rather than the parent, so the tap target and ripple still meet the dividers. The
whole row is the tap target: 40 IconBadge, a text column `gap 2` (title 16/600, caption 12
`textMuted`), then the state label 15/600. There is no per-row button — the label is a readout, not a
control.

| State | Change |
|---|---|
| Tile not added | Label reads `Add` in `primary`; tapping asks the system to add it |
| Tile added | Label reads `Remove` in `error`; tapping removes it immediately. Tiles added from the system's own editor land here too, since the page reads the shell's list |
| Add refused or already present | Snackbar "Couldn't add this tile." / "Already in Quick Settings."; the row only flips once the shell reports the tile. On API < 33 there is no public add API, so the snackbar points at the Quick Settings editor |
| Remove failed | Snackbar "Couldn't remove this tile." |

The system's Add prompt is OS chrome and is not drawn.

## Quick Settings tiles in the shade

Five 2×1 tiles, standard shade layout: icon 32 top-left of a 24-inset block, label at the bottom.
Active = `primary` container, inactive = `surfaceHigh` with `textMuted` content.

| Tile | Icon | Label | Secondary label |
|---|---|---|---|
| Developer options | `developer_mode` | "Dev options" | "On" / "Off" |
| Pause Dev options | `timer_pause` | "Pause Dev options" | "Off for 5 min" · "Off · 4:32" while paused |
| Resume Dev options | `timer_play` | "Resume Dev options" | "On for 5 min" · "On · 4:32" while resumed |
| USB debugging | `usb` | "USB debugging" | "On" / "Off" |
| Wireless debugging | `wifi_tethering` | "Wireless debugging" | "On" / "Off" (+ " · 12:04" while the timer runs) |

The platform's Add prompt is one per tile and is OS chrome, so it is not drawn; tiles are added one
at a time from the Quick Settings Tiles page. With Developer options off, the USB and Wireless tiles
are unavailable and read "Dev options off".

## Screens page layout

`devinator.fig` → `Screens` is a **flow diagram**, not a gallery: columns are steps, and every arrow is
a CTA. A screen with several CTAs gets one arrow per exit, pointing at either the next step or the
state variant it produces. Connectors are 1.5 vectors in `textMuted` with an open head; the label sits
beside the line at 12 `textMuted`. Frames keep 160 between them so a label fits in the gutter.

| From | To | CTA |
|---|---|---|
| 01 Setup | 03 Home | permission granted |
| 01 Setup | 02 Setup · not granted | `Check again` fails |
| 02 Setup · not granted | 03 Home | permission granted |
| 03 Home · dark | 04 Home · developer options off | developer options off |
| 04 Home · developer options off | 03 Home · dark | developer options on |
| 03 Home · dark | 05 Settings | settings gear |
| 03 Home · dark | 07 Quick Settings Tiles | tiles card |
| 05 Settings | 08 Permission | `Grant or revoke` |
| 09 Home · timed pause | 10 Home · timed resume | `Resume` tile — switches direction |
| 10 Home · timed resume | 09 Home · timed pause | `Pause` tile — switches direction |
| 10 Home · timed resume | 11 Home · timed duration menu | duration control |

**Timed toggle strip.** 09–11 sit in a separate strip below the main flow, under the `TIMED TOGGLE` label. The strip caption names the entry points that are not drawn as cross-row arrows: 03 `Pause for 5 min` → 09, and 04 `Resume for 5 min` → 10. 09 and 10 return to 03 and 04 when the timer ends or `Resume now` / `Pause now` is tapped; those returns are named in the card copy, not drawn as cross-row arrows.

`Open Developer options` leaves the app for Android's own screen, so it has no target frame.

## Frames

One file, dark frames (the light palette is the same layout and ships in code):

1. Setup — default
2. Setup — permission still missing (the error card above the button)
3. Home — all on (hero developer options, USB card, wireless card with endpoint, command and auto-off)
4. Home — developer options off (red hero, disabled USB and wireless)
5. Settings
6. Quick Settings Tiles — the page, one tile already added (mixed `Remove` / `Add` rows)
7. Permission — grant and revoke commands, each with a copy control
8. Home — timed pause active (Developer options off, countdown, `Resume now`)
9. Home — timed resume active (Developer options on, USB/wireless off, `Pause now`)
10. Home — duration menu open (5 / 15 / 30 min)

The Quick Settings tiles themselves are specified below but have no frame: the system draws them.

The OS trust and Add-tile prompts are not drawn, and neither is the write-failure state: the system
owns the prompts, and a failed write is a snackbar.

A light-theme pass is no longer pending: the app ships the light palette and Settings offers
Light/Dark. When a screen adds a `Style=Light` variant, copy the dark frame and swap the tokens
rather than redrawing the layout.

## Related Context

- [Design & behaviour](./design.md) — states, interactions, edge cases
- [Module context](./context.md) — keys, permission, risks
