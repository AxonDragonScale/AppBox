---
title: "Wallmatic UI — wallpaper library design system"
status: current
summary: "The Wallmatic .fig: paper/ink/indigo tokens, Chakra Petch + Roboto, the library component set, Home/Lock model, and 17 screens."
read_when: "Changing Wallmatic's UI, its design tokens, or apps/wallmatic/wallmatic.fig."
---

# Wallmatic UI

Design source: [`apps/wallmatic/wallmatic.fig`](../../../apps/wallmatic/wallmatic.fig).
Only `apps/wallmatic/wallmatic.fig` is kept; the earlier design was removed.
Code source of truth: `apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/ui/`.
All seventeen screens and the supporting overlays are implemented against this design; the reusable
kit lives in `ui/component/`. Known implementation gaps are listed at the end of this doc.

The file has three pages: **Foundations**, **Components**, **Screens**.

## The idea

A wallpaper library. The user's own wallpapers carry the colour; the app is the index, the caption
and the schedule. The design refuses the M3 dashboard of stacked cards, a floating pill navigation
bar and a wallpaper reduced to a thumbnail.

- **Paper** ground, **ink** type, one **indigo** signal from the launcher (`#4B46C0`).
- Square images with 1 px rules; no rounded cards.
- Tabs and segmented controls share one language: a 1 px ruled track and a full-width ink rule for
  the active item — a 3 px top rule flush on the bottom bar, a 3 px underline flush on the bottom
  edge of the Home/Lock switcher and segmented controls.
- Chakra Petch for display, labels and numbers; Roboto for body and captions.
- The active target (Home / Lock) is a switcher, not two duplicated cards.
- The interval is a shared picker, not a scrolling chip row.

## Theme

Two palettes, `Light` and `Night`, share the same role names. The screens are the light edition; the
night edition is defined as a palette only — the swatches on **Foundations** carry their real values,
but no screen has been built in it.

| Role | Light | Dark |
|---|---|---|
| Paper | `#F2EFE6` | `#14120F` |
| PaperLow | `#E9E4D8` | `#1C1916` |
| PaperHigh | `#DCD6C8` | `#2A2621` |
| PaperWhite | `#FFFFFF` | `#1C1916` |
| Ink | `#141414` | `#F2EFE6` |
| InkMuted | `#6B655C` | `#A7A29A` |
| RuleSoft | `#C9C2B4` | `#3A352E` |
| Primary | `#4B46C0` | `#C9C5FF` |
| OnPrimary | `#FFFFFF` | `#1A1740` |
| PrimaryContainer | `#DCD9F5` | `#2A2650` |
| OnPrimaryContainer | `#141235` | `#E4E1FF` |
| Error | `#B3261E` | `#F2B8B5` |
| OnError | `#FFFFFF` | `#601410` |
| InverseSurface | `#141414` | `#F2EFE6` |
| InverseOnSurface | `#F2EFE6` | `#141414` |

Numeric tokens: spacing 4 / 8 / 12 / 16 / 24 / 32 / 48; radii 0 / 4 / 8 / 12 / 24;
type 36 / 28 / 22 / 20 / 16 / 14 / 11 / 10.

## Typography

| Style | Size / line | Face | Use |
|---|---|---|---|
| Display | 36 / 44 | Chakra Petch Bold | Screen titles: Albums, Settings |
| Headline | 28 / 34 | Chakra Petch Bold | Card title, dialog title |
| Title | 22 / 28 | Chakra Petch Regular | Top bar, sheet title |
| Title Small | 16 / 24 | Chakra Petch SemiBold | Album row title, interval value |
| Row Title | 14 / 18 | Chakra Petch SemiBold | Album card, folder card, settings row |
| Body | 14 / 20 | Roboto Regular | Supporting copy |
| Label | 11 / 16 | Chakra Petch SemiBold, 1.6 tracking | Section labels, actions |
| Micro | 10 / 12 | Chakra Petch Medium, 1.2 tracking | Masthead, index, picker ticks |

## Shape, spacing and layout

- Screen 412 × 915, padding 24, content width 364.
- Every wallpaper preview uses the phone's own 412:915 (0.45) crop. Home preview 180 × 400;
  album card 178 × 433 (395 image + caption); wallpaper tile 178 × 395; folder strip 108 × 240.
  The folder card's strips scroll horizontally when the folder holds more than the three that fit.
- Bottom tab bar 64 dp; touch target 48 dp; rule 1 dp. The bar's paper surface and its two vertical
  dividers are edge-to-edge through the navigation-bar inset; the label row stays above the gesture
  area.
- **Square everywhere**: buttons, images, switches, radios, tab segments, dialogs and sheets.
  No pills and no rounded cards.

## Components

Specimens are 420 wide, three per row; the Components page also carries the mirror-off
`READY TO ROTATE` banner with its split `HOME` / `LOCK` buttons. Masters live on **Components**;
screens use instances.

| Component | Variants / properties | Notes |
|---|---|---|
| `Button` | `Style=Filled` · `Outline` · `Text`, text `Label` | 48 dp, square, label 12 tracked |
| `Switch` | `On` · `Off` | 52 × 32, square track and thumb |
| `HomeLockTabs` | `Selected=Home` · `Selected=Lock` | 364 × 40, 1 px track, full-width 3 px ink underline flush with the bottom edge under the active label |
| `IntervalPicker` | text `Value` | 364 × 76, 7 ticks, Custom action; tap or drag snaps to the nearest tick; the value reads `HR` for hours and `MIN` for minutes while the ticks stay `15m` … `24h` |
| `AlbumCard` | text `Title` · `Subtitle`, optional target badge | 178 × 433; square image, caption below on a Paper plate; an active card carries a paper `HOME`, `LOCK` or `BOTH` badge over the image; a card with no cover shows the `EmptyPreview` mark centred on a Paper (`#F2EFE6`, not PaperHigh) 178 × 395 image area (48 dp paper square, 1 px RuleSoft border, 16 dp Primary dot) |
| `WallpaperTile` | text `Index` | 178 × 395; paper index label with ink type on the image |
| `SettingsRow` | `Trailing=Switch` · `Chevron` · `Segmented`, text `Title`/`Subtitle` | 364 × 72 |
| `Segmented` | — | 128 × 32, 2 / 3 / 4 grid size, full-width 3 px ink underline flush with the bottom edge under the active value |
| `ThemeSegmented` | — | 128 × 32, sun / auto / moon, full-width 3 px ink underline flush with the bottom edge under the active icon |
| `TabBar` | `Active=Home` · `Library` · `Settings` | paper bar, 64 dp, full-width 3 px ink rule on the top edge of the active segment; its two vertical dividers are ink, like the switcher and segmented controls, and they continue through the navigation-bar inset |
| `TopBar` | text `Title` | back + screen name, no eyebrow |
| `EmptyState` | text `Title`/`Description` | outlined blank card |
| `Fab` | — | 56 dp, square, one primary action |
| `AlbumRow` | `State=Selected` · `Unselected`, text `Title`/`Subtitle` | 364 × 72, square radio with a centred indicator |
| `AlbumNameDialog` | text `Title` | 280 wide, square corners, paper; one dialog for every entry point — `New album`, empty field, helper, `CANCEL` / `CREATE` |
| `ContextMenu` | `Menu=Album` · `Wallpaper`, album items vary with Mirror | 1 px row rules inset 16 dp from both edges, destructive row isolated; the album menu is `Rename` / `Set active` / `Delete` with Mirror on, and `Rename` / `Set Home` / `Set Lock` / `Delete` with it off, offering only the targets the album is not already active on; no item carries an ellipsis; the album card above the menu sits its title and count on an 8 dp-inset Paper plate so they read over the scrim |

Bottom sheets: the drag handle sits 16 dp below the sheet's top edge, and the sheet title starts 44 dp
from the top (24 dp below the handle).

### Copy

Copy: count labels title-case their nouns — `1 Folder · 1 Wallpaper`, `3 Folders · 12 Wallpapers`,
and `8 Wallpapers` in a folder-card header. Any other short count-like label follows the same rule.

### Reuse contract

Instances are linked to their masters. Editing a master's fill, size, layout or text updates every
instance on every page. Text is exposed through component properties (`Label`, `Value`, `Index`,
`Title`, `Subtitle`, `Description`), so instances carry their own copy without detaching.

Variant selection uses the full variant child name: `<Instance of="TabBar" Variant="Active=Library" />`.
Boolean properties take a string: `properties={{"Show subtitle":"false"}}`.

## Home and Lock

Home and Lock are two targets of one screen, not two stacked cards.

- **Mirror Home Screen** is a toggle in the **Preferences** section of Settings, not on Home. When it
  is on, the Home/Lock target tabs are hidden: Lock is a copy of Home, so there is nothing to switch
  to. The Home screen has no mirror row or Settings link; the toggle is the only place to change it.
- When mirror is off, the `HomeLockTabs` switcher appears at the top of Home; the preview, caption
  and album follow the selected target.
- The Home preview uses the phone's own 0.45 aspect ratio (180 × 400 at this width). The right
  column carries the current and next update times; the old index rail is gone.
- The album name in the right column is the album affordance: a 1 px ink boxed control (48 dp tall,
  10 dp side padding) with the name at 20 dp in one line, ellipsized when long, and a 20 dp
  `KeyboardArrowDown` in Ink. The whole control — text and chevron — is the tap target and
  opens `07 · Select Album`, a square bottom sheet of album rows with square radios. Choosing a row
  closes the sheet and swaps the preview, caption and schedule for the selected target — on Lock as
  well as Home. The filled `CHANGE ALBUM` button at the bottom of Home is gone, and so is the old
  `CHANGE` text beside the `ALBUM` eyebrow: one dropdown for one job.
- The interval is **shared** by both targets, as in the product. It sits once, below the target,
  so changing it changes the schedule for both. The current/next times live in the right column;
  the picker does not repeat a NEXT row. An outline `CHANGE NOW` button (364 × 48, 12 dp below the
  picker, the `Button` `Style=Outline` treatment) applies the next wallpaper immediately; it is
  grouped with the picker, not pinned to the bottom of the screen.
- `CUSTOM` on the picker opens a stepper sheet for arbitrary hours and minutes.


## Screens

Seventeen 412 × 915 frames on **Screens** — sixteen in a 4 × 4 grid, gap 60, plus the update-failure
state in the fifth column of the first row:

1. `01 · Home · mirrored` — no target tabs; device-aspect preview, current/next with the album
   dropdown, picker and outline `CHANGE NOW`, text tab bar.
2. `02 · Home · Lock` — mirror off; Home/Lock tabs with Lock selected; own album via the
   right-column dropdown.
3. `03 · Library` — two-column album cards at the device aspect (one full row, the next peeking
   under the bar), FAB, Library tab. Active albums carry a `HOME`, `LOCK` or `BOTH` badge; no page title,
   the masthead already says LIBRARY.
4. `04 · Album` — top bar with the target status (`ACTIVE · HOME`, `ACTIVE · LOCK` or `ACTIVE · BOTH`), bordered folder card with a
   header and three device-aspect strips that scroll horizontally, wallpaper grid, FAB.
5. `05 · Folder` — top bar and a two-column wallpaper grid.
6. `06 · Settings` — Appearance, Preferences (Mirror Home Screen, then Grid Size) and About ledgers.
   No page title; the masthead already says SETTINGS.
7. `07 · Select Album` — scrim plus a square paper bottom sheet of album rows with square radios,
   opened from the right-column album dropdown.
8. `08 · New album` — scrim plus the square `New album` dialog, the same construction as `14`.
9. `09 · Wallpaper` — full-bleed image, no chrome.
10. `10 · Context menu` — scrim, album preview and album menu: `Rename` / `Set active` / `Delete`
    with Mirror on, `Rename` / `Set Home` / `Set Lock` / `Delete` with it off, listing only the
    targets the album is not already active on.
11. `11 · Custom interval` — square stepper sheet for arbitrary hours and minutes; the HOURS and
    MINUTES minus and plus buttons are aligned in a fixed 160 dp stepper group with a 48 dp value cell.
12. `12 · Home · no album` — framed empty-preview mark, side guide (`01` create, `02` add, `03` set
    active), `CREATE ALBUM`; schedule controls hidden.
13. `13 · Library · no albums` — the same empty-preview mark, side guide and bottom `CREATE ALBUM`,
    Library tab active.
14. `14 · Name album` — `New album` dialog over the Library empty state.
15. `15 · Album · empty` — device-aspect empty-preview mark on the left, album copy on the right,
    `ADD WALLPAPERS`; no step label.
16. `16 · Album · ready` — folder card and wallpaper grid; `READY TO ROTATE` banner with one
    `SET AS ACTIVE` action when Mirror is on, and two 56 × 28 Paper buttons, `HOME` and `LOCK`, when
    it is off.
17. `17 · Home · update failed` — Home with a ruled status row under the preview:
    `LAST UPDATE FAILED`, the failed time and a `RETRY` action; the picker and `CHANGE NOW` stay
    available.

Overlays are siblings of the scrim, both absolute at `0, 0`.

## First album / empty states

Onboarding is the product screen with no albums, not a splash sequence. Home and Library explain the
next action in place, then the album flow carries the user through naming, adding wallpapers and
setting the album active.

1. **Home · no album** — the preview slot becomes a ruled frame with a centred indigo square; the
   right column carries the guide (`01` create, `02` add, `03` set active); the schedule picker and
   `CHANGE ALBUM` are hidden; the only primary action is `CREATE ALBUM`.
2. **Library · no albums** — the same empty-preview mark, side guide and bottom `CREATE ALBUM` as
   Home, with the Library tab active. No ghost tile and no FAB.
3. **New album** — the square `New album` dialog over the Library, with an empty name field,
   `You can rename it later.` and `CANCEL` / `CREATE`.
4. **Album · empty** — device-aspect empty-preview mark on the left, album copy on the right, and
   `ADD WALLPAPERS`; no step label.
5. **Album · ready** — the folder card and wallpaper grid appear; an indigo `READY TO ROTATE`
   banner carries `SET AS ACTIVE`, or the `HOME` / `LOCK` pair when Mirror is off.
6. **Home · update failed** — a ruled row under the preview reports `LAST UPDATE FAILED` with the
   failed time and a `RETRY` action. It is the visible half of the reliability promise; the
   schedule controls stay usable.

The guide numbers are first-run only; the per-album screens carry no step labels, because creating
a second album is not onboarding. Home and Library do not scroll when the content fits — the primary
bottom action is anchored above the tab bar — and Album's `ADD WALLPAPERS` sits above the navigation
bar.

Frames live on the **Screens** page, `12`–`17` at the end of the grid.

## Implementation notes

The code follows the frames; these are the places where the design is silent or deliberately
differs from what shipped.

- Auto-cycling has no pause control in the design. Selecting an album enables it; an existing paused
  config resumes by re-selecting the album.
- Mirroring preserves the Lock album configuration while it is on and restores it when turned off; the
  frames do not show that hidden state.
- The active-target actions are target-aware: `SET AS ACTIVE` writes both targets only while Mirror
  is on; with Mirror off the album banner splits into `HOME` and `LOCK` buttons that each write their
  own target, and the long-press menu offers only the targets the album is not already active on.
  The design has no both-screens status.
- `04` and `16` disagree on the FAB, grid indices and folder fill. The code follows `04`: FAB present,
  indices from `01`, `PaperLow` folder card, and no index badges on the strips.
- Painted targets for the switchers, segmented controls and interval ticks are 32–40 dp. The 48 dp
  touch-target rule still needs the frames updated.
- The interval rail stops at 24 h; the custom sheet accepts up to 168 h and the thumb snaps to 24 h
  for longer values. `Interval` owns the bounds and labels in one place. The rail snaps on tap and
  drag alike — either gesture lands on the nearest tick. The press ripple is centred on the snapped
  tick under the thumb, at the ends as well as the middle, and is tinted with Ink so it reads on both
  the Paper and Night palettes. The value label reads `HR` for hours (`1 HR`, `1 HR 30 MIN`) with
  minutes kept as `MIN`.

- The folder card shows three 108 × 240 strips and scrolls horizontally through the rest of the
  folder; the strip row is a `LazyRow` inside the card, so a horizontal drag moves the strips while
  a vertical drag still scrolls the album.
- The Album FAB opens a square `Add wallpapers` sheet (folder or images); the design draws only `+`.
- The album badge and the album page top bar tell the truth about targets: an album active on both
  screens carries `BOTH` / `ACTIVE · BOTH`, and with Mirror on an album active on Home also reads
  `BOTH`, because Lock follows Home.
- Settings adds a `Dev Tools` row after About in debug builds only; the design has no debug entry.
- `CREATE` in the New album dialog is disabled and dimmed until the field has text; the frames have no
  disabled state.
- Folder has no empty frame; it uses the `EmptyState` master as an outlined card rather than a
  sentence.
