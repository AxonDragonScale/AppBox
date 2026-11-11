# Design

<!-- impeccable:design-schema 1 -->

Wallmatic is a **wallpaper library**. The user's own wallpapers carry the colour; the app is the
index, the caption and the schedule. The full token and component record lives in
[docs/apps/wallmatic/ui.md](docs/apps/wallmatic/ui.md); this file holds the durable visual contract.

## Direction

- **Ground**: paper `#F2EFE6` with `#E9E4D8` tonal panels; night edition `#14120F`. The night
  palette is defined in Foundations (its swatches carry the real values); no screen is built in it.
- **Ink**: `#141414`; muted `#6B655C`; soft rule `#C9C2B4`.
- **Signal**: indigo `#4B46C0` from the launcher, used for the primary action, the active index
  and the target state. No second accent; the wallpaper supplies the colour.
- **Type**: Chakra Petch for display, labels and numbers; Roboto for body and captions. Scale
  36 / 28 / 22 / 16 / 14 / 11 / 10.
- **Form**: square images with 1 px rules, 0-radius buttons, switches and radios, 0-radius dialogs
  and sheets. No rounded cards, no pills, no drop shadows.
- **Tabs**: one language, two positions. A 1 px track; the bottom bar carries a 24 × 3 px ink rule
  inset 4 px inside the active segment; the Home/Lock switcher and segmented controls carry a
  20 × 2 px ink underline under the active label or value. Labels stay centred; never a filled block.
- **Composition**: a device-aspect preview with the current/next times beside it; rules separate
  sections; the bottom tab bar is a text-only 64 dp segmented bar.

## Refused

- The M3 dashboard of stacked cards and a floating pill navigation bar.
- Two duplicated Home/Lock cards; a target switcher replaces them when mirror is off.
- The scrolling interval chip row; a shared picker replaces it.
- Decorative gradients, glass, and coloured left borders.

## Home and Lock

One screen, two targets. **Mirror Home Screen** is a toggle in Settings. When it is on, the
Home/Lock tabs are hidden and Lock copies Home; the Home screen has no mirror row or Settings
link. When it is off, the `HomeLockTabs` switcher appears at the top of Home. The interval is
shared and sits once, below the target. The current/next times live in the right column; the
picker does not repeat a NEXT row. `CUSTOM` opens a stepper sheet for arbitrary hours and minutes.
`CHANGE ALBUM` is the single album action and opens the Select Album sheet; there is no second
`CHANGE` link. The Library marks the active Home and Lock albums with `HOME` / `LOCK` badges, and
the album top bar states `ACTIVE · HOME` or `ACTIVE · LOCK`. When a scheduled update fails, Home
carries a ruled `LAST UPDATE FAILED` row with the failed time and `RETRY`.

Every wallpaper preview is cropped to the phone's own 412:915 (0.45) aspect: the Home preview is
180 × 400 at this frame width, album cards are 178 × 433, and wallpaper tiles are 178 × 395.

## Naming

The bottom destination is **Library**, not Albums: the library holds albums, and the album detail
screen is an album. The Library and Settings screens have no display title; the masthead in the
top-right carries the screen name.

Album creation has one dialog, **New album**, wherever it is opened from: an empty name field, a
helper line, and `CANCEL` / `CREATE`. Album counts always read `folders · wallpapers` with the noun
`wallpapers`, never `sheets`.

## Artifact

`apps/wallmatic/wallmatic.fig` — Foundations, Components, Screens (17 frames). It is the only design
file kept for the app. Code has not been changed for this direction yet.
