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
The design now leads the code: no Compose change has been made for this direction yet.

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
type 36 / 28 / 22 / 16 / 14 / 11 / 10.

## Typography

| Style | Size / line | Face | Use |
|---|---|---|---|
| Display | 36 / 44 | Chakra Petch Bold | Screen titles: Albums, Settings |
| Headline | 28 / 34 | Chakra Petch Bold | Card title, dialog title |
| Title | 22 / 28 | Chakra Petch Regular | Top bar, sheet title |
| Title Small | 16 / 24 | Chakra Petch SemiBold | Row title, album name |
| Body | 14 / 20 | Roboto Regular | Supporting copy |
| Label | 11 / 16 | Chakra Petch SemiBold, 1.6 tracking | Section labels, actions |
| Micro | 10 / 12 | Chakra Petch Medium, 1.2 tracking | Masthead, index, picker ticks |

## Shape, spacing and layout

- Screen 412 × 915, padding 24, content width 364.
- Every wallpaper preview uses the phone's own 412:915 (0.45) crop. Home preview 180 × 400;
  album card 178 × 433 (395 image + caption); wallpaper tile 178 × 395; folder strip 108 × 240.
- Bottom tab bar 64 dp; touch target 48 dp; rule 1 dp.
- **Square everywhere**: buttons, images, switches, radios, tab segments, dialogs and sheets.
  No pills and no rounded cards.

## Components

Specimens are 420 wide, three per row. Masters live on **Components**; screens use instances.

| Component | Variants / properties | Notes |
|---|---|---|
| `Button` | `Style=Filled` · `Outline` · `Text`, text `Label` | 48 dp, square, label 12 tracked |
| `Switch` | `On` · `Off` | 52 × 32, square track and thumb |
| `HomeLockTabs` | `Selected=Home` · `Selected=Lock` | 364 × 40, 1 px track, full-width 3 px ink underline flush with the bottom edge under the active label |
| `IntervalPicker` | text `Value` | 364 × 76, 7 ticks, Custom action; no NEXT row |
| `AlbumCard` | text `Title` · `Subtitle`, optional target badge | 178 × 433; square image, caption below; an active card carries a paper `HOME` or `LOCK` badge over the image |
| `WallpaperTile` | text `Index` | 178 × 395; paper index label with ink type on the image |
| `SettingsRow` | `Trailing=Switch` · `Chevron` · `Segmented`, text `Title`/`Subtitle` | 364 × 72 |
| `Segmented` | — | 128 × 32, 2 / 3 / 4 grid size, full-width 3 px ink underline flush with the bottom edge under the active value |
| `ThemeSegmented` | — | 128 × 32, sun / auto / moon, full-width 3 px ink underline flush with the bottom edge under the active icon |
| `TabBar` | `Active=Home` · `Library` · `Settings` | paper bar, 64 dp, full-width 3 px ink rule on the top edge of the active segment |
| `TopBar` | text `Title` | back + screen name, no eyebrow |
| `EmptyState` | text `Title`/`Description` | outlined blank card |
| `Fab` | — | 56 dp, square, one primary action |
| `AlbumRow` | `State=Selected` · `Unselected`, text `Title`/`Subtitle` | 364 × 72, square radio with a centred indicator |
| `AlbumNameDialog` | text `Title` | 280 wide, square corners, paper; one dialog for every entry point — `New album`, empty field, helper, `CANCEL` / `CREATE` |
| `ContextMenu` | `Menu=Album` · `Wallpaper` | 1 px rule, destructive row isolated |

### Reuse contract

Instances are linked to their masters. Editing a master's fill, size, layout or text updates every
instance on every page. Text is exposed through component properties (`Label`, `Value`, `Index`,
`Title`, `Subtitle`, `Description`), so instances carry their own copy without detaching.

Variant selection uses the full variant child name: `<Instance of="TabBar" Variant="Active=Library" />`.
Boolean properties take a string: `properties={{"Show subtitle":"false"}}`.

## Home and Lock

Home and Lock are two targets of one screen, not two stacked cards.

- **Mirror Home Screen** is a toggle in **Settings**, not on Home. When it is on, the Home/Lock
  target tabs are hidden: Lock is a copy of Home, so there is nothing to switch to. The Home
  screen has no mirror row or Settings link; the toggle is the only place to change it.
- When mirror is off, the `HomeLockTabs` switcher appears at the top of Home; the preview, caption
  and album follow the selected target.
- The Home preview uses the phone's own 0.45 aspect ratio (180 × 400 at this width). The right
  column carries the current and next update times; the old index rail is gone.
- `CHANGE ALBUM` is the single album affordance: the filled button below the picker. The old
  `CHANGE` text beside the `ALBUM` eyebrow is gone, so there is one verb for one job. It opens
  `07 · Select Album`, a square bottom sheet of album rows with square radios. Choosing a row
  closes the sheet and swaps the preview, caption and schedule for the selected target — on Lock
  as well as Home.
- The interval is **shared** by both targets, as in the product. It sits once, below the target,
  so changing it changes the schedule for both. The current/next times live in the right column;
  the picker does not repeat a NEXT row.
- `CUSTOM` on the picker opens a stepper sheet for arbitrary hours and minutes.


## Screens

Seventeen 412 × 915 frames on **Screens** — sixteen in a 4 × 4 grid, gap 60, plus the update-failure
state in the fifth column of the first row:

1. `01 · Home · mirrored` — no target tabs; device-aspect preview, current/next, picker, change
   action, text tab bar.
2. `02 · Home · Lock` — mirror off; Home/Lock tabs with Lock selected; own album.
3. `03 · Library` — two-column album cards at the device aspect (one full row, the next peeking
   under the bar), FAB, Library tab. Active albums carry a `HOME` or `LOCK` badge; no page title,
   the masthead already says LIBRARY.
4. `04 · Album` — top bar with the target status (`ACTIVE · HOME`), bordered folder card with a
   header and three device-aspect strips, wallpaper grid, FAB.
5. `05 · Folder` — top bar and a two-column wallpaper grid.
6. `06 · Settings` — Appearance, Home / Lock, Preferences and About ledgers. No page title; the
   masthead already says SETTINGS.
7. `07 · Select Album` — scrim plus a square paper bottom sheet of album rows with square radios.
8. `08 · New album` — scrim plus the square `New album` dialog, the same construction as `14`.
9. `09 · Wallpaper` — full-bleed image, no chrome.
10. `10 · Context menu` — scrim, album preview and context menu (`Rename…` / `Delete…`).
11. `11 · Custom interval` — square stepper sheet for arbitrary hours and minutes; the HOURS and
    MINUTES minus and plus buttons are aligned in a fixed 160 dp stepper group with a 48 dp value cell.
12. `12 · Home · no album` — framed empty-preview mark, side guide (`01` create, `02` add, `03` set
    active), `CREATE ALBUM`; schedule controls hidden.
13. `13 · Library · no albums` — the same empty-preview mark, side guide and bottom `CREATE ALBUM`,
    Library tab active.
14. `14 · Name album` — `New album` dialog over the Library empty state.
15. `15 · Album · empty` — device-aspect empty-preview mark on the left, album copy on the right,
    `ADD WALLPAPERS`; no step label.
16. `16 · Album · ready` — folder card and wallpaper grid; `READY TO ROTATE` banner with
    `SET AS ACTIVE`.
17. `17 · Home · update failed` — Home with a ruled status row under the preview:
    `LAST UPDATE FAILED`, the failed time and a `RETRY` action; the picker and primary action stay
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
   banner carries `SET AS ACTIVE`.
6. **Home · update failed** — a ruled row under the preview reports `LAST UPDATE FAILED` with the
   failed time and a `RETRY` action. It is the visible half of the reliability promise; the
   schedule controls stay usable.

The guide numbers are first-run only; the per-album screens carry no step labels, because creating
a second album is not onboarding.

Frames live on the **Screens** page, `12`–`17` at the end of the grid.
