---
title: "Storage viewer design"
status: in-progress
summary: "The board behind `:libraries:storage-viewer`: three-segment home, 29 screens and 5 sheet overlays, state coverage, component and token vocabulary, copy rules, and the files surface."
read_when: "Building or reviewing the storage viewer UI, or changing libraries/storage-viewer/storage-viewer.fig."
---

> Context: [Main](../../context.md) > [Storage Viewer](./context.md) > Design

# Storage Viewer design

Design source: `libraries/storage-viewer/storage-viewer.fig` (Foundations, Components, Screens).
Module, module path and open work live in [context.md](./context.md); this doc is the board.

## 1. Product and boundary

A read-only, in-app viewer for the host app's own storage: databases, Preferences DataStore,
SharedPreferences, and internal files and cache. No copies, no export, no writes in v1, and reads run
against the live files. Values are verbatim, secrets included: a debug surface for the developer
holding the device, opened from `:core:devtools`, not a consumer screen, so it carries one title and
no onboarding, settings or branding.

## 2. Information architecture

Home is a three-segment tab row, `Databases · Preferences · Files`, with the count inside each
segment. Each segment lists stores; a store opens its own surface.

- Database: store → tables → (schema) → rows → row sheet.
- Preferences: store → keys → value sheet.
- Files: roots → folders → file preview.

One store switcher concept: the tabs stay reachable from every root surface, including its loading,
empty, no-match and error states. Deep surfaces go back instead of switching stores, so nobody
loses the store they were in; depth is three below the list, plus a sheet.

## 3. Screen inventory

29 screens and 5 sheet overlays. Every screen is 412 × 915; sheets are 396 wide.

| # | Screen | Shows | State |
|---|---|---|---|
| 01 | `01 · Stores · databases · dark` | database rows with table count, version, size | content |
| 02 | `02 · Stores · preferences · dark` | DataStore and SharedPreferences sections in one list | content |
| 03 | `03 · Stores · files · dark` | five roots, each with a size and item count | content |
| 04 | `04 · Stores · empty · dark` | no store of the active kind; the copy names that tab | empty |
| 05 | `05 · Stores · no matches · dark` | query with no store-name match, one clear action | no results |
| 06 | `06 · Stores · loading · dark` | scanning pill, ellipsis counts, skeleton rows | loading |
| 07 | `07 · Database · tables · dark` | table rows, each with row and column counts | content |
| 08 | `08 · Database · tables empty · dark` | a database that holds no tables | empty |
| 09 | `09 · Database · busy · dark` | locked database, banner with retry, skeleton grid | busy |
| 10 | `10 · Database · schema · dark` | DDL card, column rows, index rows | content |
| 11 | `11 · Database · rows · dark` | sticky column header, two-line data rows, row count | content |
| 12 | `12 · Database · rows · no matches · dark` | rows loaded, zero matches, clear action | no results |
| 13 | `13 · Table empty · dark` | schema intact, no records | empty |
| 14 | `14 · Database · unreadable · dark` | failed open, plain-language banner, retry | error |
| 15 | `15 · Store · keys · dark` | key rows with type chip and value preview | content |
| 16 | `16 · Store · keys empty · dark` | store exists, no entries | empty |
| 17 | `17 · Store · keys · no matches · dark` | zero matching keys or values | no results |
| 18 | `18 · Preferences · search · dark` | cross-store results, each naming its store | content |
| 19 | `19 · Store · unreadable · dark` | failed read, plain-language banner, retry | error |
| 20 | `20 · Files · folder · dark` | path line, folder rows, file rows with sizes | content |
| 21 | `21 · Files · folder empty · dark` | a folder with no entries | empty |
| 22 | `22 · Files · unreadable · dark` | read failure, plain-language banner, retry | error |
| 23 | `23 · File · text · dark` | capped text preview with byte count | content |
| 24 | `24 · File · binary · dark` | hex dump preview with byte count | content |
| 25 | `25 · File · truncated · dark` | preview stopped at the cap, truncation marked | partial |
| 26 | `26 · Stores · databases · light` | 01 in light | light |
| 27 | `27 · Database · rows · light` | 11 in light | light |
| 28 | `28 · Store · keys · light` | 15 in light | light |
| 29 | `29 · Files · folder · light` | 20 in light | light |

Sheet overlays composite over their parent and are not counted as screens:
`Sheet · Row detail` (over 11), `Sheet · Value · long text` (over 11), `Sheet · Value · binary`
(over 11), `Sheet · Value · preferences` (over 15), `Sheet · Row detail · light` (over 27).

### State coverage

| Surface | Content | Loading | Empty | No results | Error | Busy | Light |
|---|---|---|---|---|---|---|---|
| Stores per tab | 01 / 02 / 03 | 06 | 04 | 05 | n/a: discovery is a directory scan; a store that fails to open reports on its own surface | n/a: the scan never blocks | 26 |
| Table list | 07 | n/a: 09 covers a slow open | 08 | n/a: no search field here | 14 | 09 | n/a: no frame; reuses the card surface proved by 26 |
| Schema | 10 | n/a: read with the table open | n/a: a table always has at least one column | n/a: no search | 14 | 09 | n/a: same card surface as 27 |
| Rows | 11 | n/a: a slow read is 09 | 13 | 12 | 14 | 09 | 27 |
| Keys | 15 (18 for search) | n/a: the list arrives with the store open; 06 covers the scan | 16 | 17 | 19 | n/a: preference files are read as bytes, so there is no writer lock | 28 |
| Row sheet | over 11 | n/a: the row is already loaded | n/a: opens only from a row | n/a: no search inside | n/a: the row loaded or the sheet never opens | n/a | sheet over 27 |
| Value sheet | over 11 and 15 | n/a | n/a | n/a | n/a: the value came with the row | n/a | n/a: shares the row-sheet light scaffold |
| Files | 20 / 23 / 24 / 25 | n/a: a folder listing is synchronous; 06 covers the root scan | 21 | 05 pattern with the file noun | 22 | n/a: files are read, never locked by a writer | 29 |

A named frame is reused with that surface's nouns; every n/a is a state the surface cannot enter.

## 4. Flows

Entry: DevTools opens the viewer and 06 runs the scan → 01 (databases is the default tab), 02 or 03
by tab, or 04 when nothing was written.

- **Database:** 01 → store → 07 → table → 11. The schema action on a table row opens 10 and back
  returns to 07. A locked open is 09, an unreadable one 14; 08, 13 and 12 cover no tables, no
  records and no match.
- **Preferences:** 02 → store → 15 → row → value sheet. Typing in the store search stays on 15; 18
  is the cross-store search from the home search; 16, 17 and 19 sit on the same surface.
- **Files:** 03 → root → 20. A folder opens 20 one level deeper and the path line grows; a file
  opens 23, 24 or 25; 21 and 22 replace the list only.

Back: a sheet dismisses first (back, scrim tap, close), a second back leaves the screen; every step
between a store and its rows is a screen, never an in-place swap. A sheet never resets the list
behind it: search text, the rows filter and the scroll offset survive dismiss on 11 and 15, and
scroll is restored under the light variants. Leaving a surface clears its query; each stores tab
keeps its own query while the viewer is open.

## 5. Layout and density

- Screen 412 wide, 16 side padding, 380 content, 4-pt grid. Top bar 64: title 22/500, then one 12
  muted line carrying identity and the read-only statement (`3 databases · 5 preference stores`,
  `Database · v1 · read-only`, `cache · read-only`). One line, never two.
- Lists scroll under a sticky 40 dp column header on 11 and a sticky section header elsewhere; the
  row-count line (`24 rows`) sits with that header. No paging, no footer, no "showing 1 to 10".
- Row grammar: store 56 single-line, table 48 two-line, database data row 64 two-line, keys 48
  single-line, file row 52 single-line, schema row 40 single-line.
- A database data row is two lines: `#id · label` at 14/500, then the value preview at mono 13 plus
  one type chip. `label` is the row's first readable text column.
- Column grid on rows and the row sheet: key 150, type 64, value flex; numeric columns and ids
  end-align with tabular figures. Density target 16 to 20 visible data rows, with no caption strip
  at the bottom of any screen, no prose card under a working list, and at most one info affordance
  per screen, opening a sheet.
- Screens with a metadata chip row (`07`) or a leading code/DDL card (`10`) place the filter bar or
  first section lower; their content follows the same rhythm, and the canonical 88/148 values apply
  to screens without those blocks.

On the board the Screens page groups the 29 screens into labelled bands in flow order — Stores,
Databases, Preferences, Files, Sheets, Light theme — with arrows between steps.

## 6. Components

`component` is one Compose composable shared by screens; `inline` is a composition inside its screen.

| Component | Anatomy and size | Kind |
|---|---|---|
| Top bar | root: 64, 22/500 title, 12 muted subtitle, 40 slots, no back. Detail: same, with back and one trailing slot | component |
| Segmented tabs | 40 track inside a 44 hit row, 4 inset, pill 999, 3-up at 14/500 with a 12 muted count; active segment `primaryContainer` | component |
| Search field | 48, radius 16, `surfaceHigh`, 20 leading icon; active state swaps the icon for a 40 clear action and shows the query at 14 with a muted result count | component |
| Filter bar | 44, one 40 trailing slot; carries the rows filter in v1 and reserves the sort slot for v2 | component |
| Section header | 40; label 16/500, count 12 muted, optional text action 13/500 | component |
| Store row | 56; 36 icon tile, name 16/500, meta 12 muted, size mono 12 end, chevron 20 | component |
| Table row | 48, two lines; 18 icon, mono 12 name, mono 11 muted `24 rows · 6 columns`, chevron 16 | component |
| Database data row | 64, two lines; `#id · label` 14/500, preview mono 13 + type chip 20 | component |
| Key-value row | 48; key mono 12 in 150, type chip 20 in 64, value mono 13 flex | component |
| Schema row | 40; name mono 12 in 120, type chip 20, flag 12 muted | component |
| File row | 52; 20 kind icon, name 14/500, size mono 12 end; the folder variant adds a chevron | component |
| Path line | 24; mono 12 muted, one line, middle-ellipsized from the left, scrolls with the list | component |
| Meta chip, type chip | 20, radius 6, padding 8, mono 11, `chipFill` + 1 dp `outline`; the type chip names the kind in that kind's colour | component |
| Status pill, status banner | pill 20, radius 999, mono 11, `SCANNING` only; banner 72 with 20 icon, title 14/500, body 12 muted, an optional muted Details line and a 44 action below; the busy variant is 131 with that action inline | component |
| Empty state, skeleton row | 64 icon tile, title 16/500, body 14 muted, one action 15/500; a skeleton uses the parent row's height with 2 to 3 bars in `skeletonFill` | component |
| Info card | card 20, `surface` + 1 dp `outline`, one 13/500 line and one 12 muted line, opens a sheet | inline |
| Row sheet, value sheet | 24 radius, `surfaceHigh`, handle 40×4, title 18; the row sheet carries six key/value rows 48 and two actions, the value sheet a mono 13 value block, meta row and `Copy value` / `Copy key` | component |

No dialog component. Copy runs immediately and the platform confirmation is the only feedback.

## 7. Tokens

Colour roles, by purpose. Values live in the two theme collections in the `.fig`; dark and light
share every name.

| Group | Roles |
|---|---|
| Base | `bg` the ground; `surface` cards and rows; `surfaceHigh` insets, chips and sheets; `outline` 1 dp borders; `divider` hairlines; `scrim` the sheet backdrop |
| Text | `text` for everything readable; `textMuted` for meta, subtitles, flags and paths |
| Accent | `primary` for the one action per screen; `onPrimary` on it; `primaryContainer` for the selected segment and the busy banner; `primaryContainerOutline` for that banner's border |
| Status | `warn` and `warnSurface` for a locked or partial read; `error` and `errorSurface` for a failed read |
| Store kind | `storeDatabase`, `storePreferences`, `typeFiles` on the store icon tile and its row |
| Value kind | `kindNeutral` (text, null), `kindNumber` (int, real, long), `kindBool` on the type chip |
| Fills | `chipFill` on chips, `iconTileFill` behind icons, `skeletonFill` in skeletons, `footerFill` behind the row-count line |

The ramp is deliberately five semantic colours, not a palette: three store kinds and two value-kind
hues, with one neutral for text-like values. A value kind is a label, never the only signal; every
row also names its type in mono. `on`, `traceSurface`, the three unused `error*` roles
(`errorContainer`, `errorSurfaceHigh`, `errorOutline`) and the duplicate kind/type pairs are
removed, so no colour has two names.

| Type role | Spec | Use |
|---|---|---|
| `display32` | 32 / 500 | cover only, never in the viewer |
| `title22`, `sheetTitle18` | 22 / 500, 18 / 500 | screen title, sheet title |
| `section16`, `rowTitle16` | 16 / 500 | section header; store name and empty-state title |
| `body14`, `cardTitle13`, `control15` | 14 / 400, 13 / 500, 15 / 500 | body copy and data-row line one; info card; buttons and text actions |
| `meta12` | 12 / 400, muted | subtitles, counts, flags, sizes |
| `monoKey12` | mono 12 | keys, table names, schema names, paths |
| `monoValue13`, `monoIdentifier15` | mono 13, mono 15 | values and previews; a value in a sheet header |
| `monoChip11`, `monoColumn11`, `monoFooter11` | mono 11 | type and meta chips, sticky column header, row-count line |

Shape: `chip6`, `tile12`, `field16`, `card20`, `sheet24`, `pill999`. Spacing: 4 / 8 / 12 / 16 / 24,
plus 10 admitted as a real step where the board uses it. Heights: `topBar64`, `field48`,
`filterBar44`, `rowStore56`, `rowData64`, `rowList48`, `rowFile52`, `header40`, `chip20`,
`banner72`. A card is `surface` plus a 1 dp `outline`; a chip is `surfaceHigh` plus a 1 dp `outline`.
Nothing carries a shadow; layering is the scrim and the sheet surface only.

Light is a designed scheme, not an inversion: its own surface, outline and muted values, and every
kind colour darkened for contrast. Dark is the inherited host state and both are first-class.

## 8. Copy and terminology

One word, one meaning:

| Term | Means |
|---|---|
| `Database` | one SQLite file in `databases/` |
| `Table` | a table or view inside one database |
| `Preferences store` | one DataStore or SharedPreferences file |
| `Key` | one entry in a preferences store |
| `Value` | the bytes or scalar behind one key, or one cell |
| `Folder` | a directory inside a files root |
| `File` | a non-directory entry inside a root |
| `Cache` | the `cache/` root and its two siblings |

- A locked or unreadable file has two states only: `in use` and `couldn't open`, never a driver code
  in a headline. One retry label everywhere: `Retry`, not `Retry read`.
- Copy verbs: `Copy value`, `Copy key`, `Copy row as JSON`; no `share`, no icon-only copy on a row.
  One search placeholder per scope: `Search databases`, `Search preference stores`, `Search this
  store`, `Search keys and values`, `Search files`.
- Counts are derived from the list they label, never typed: segment counts, section counts,
  `24 rows`, `3 tables`, `1 hidden`.
- Plain language in the UI. A technical identifier (exception class, engine error) may appear only
  as a muted secondary `Details` line, never as the headline or the body.

## 9. Files and cache surface

Five roots, all read-only: app files, cache, no-backup files, external cache, code cache. A root that
does not exist for this install opens to 21 rather than disappearing from the list.

- Row grammar: 52, 20 kind icon, name 14/500, size mono 12 end-aligned, chevron for folders.
- Sizes are 1024-based: one decimal below 10 units, an integer from 10 up, `0 B` for zero, never a
  repeated unit, so 1024 KB prints as 1 MB. They are computed on demand when a folder opens, never
  on the home list; the roots row shows a count only.
- `files/datastore/` is hidden by default with a truthful `1 hidden` count and a
  `Show preference files` toggle. `databases/` and `shared_prefs/` are siblings of the files root,
  so they cannot appear inside it.
- Text preview is capped at 64 KB and always states `N of M bytes`; binary shows a hex dump, 16
  bytes a line. A truncated preview is marked on screen (25), never silently shortened.
- No cache clear in v1. The surface is read-only and no row carries a destructive action.

## 10. Accessibility and adaptivity

- 48 dp targets via hit-area delegation: a 20 dp chip, a 40 dp icon slot and a 15 dp text action all
  sit inside 48. sp only, never dp type. At font scale 1.3 no row clips: two-line rows grow,
  single-line rows wrap to two and keep their leading icon centred.
- Contrast at least 4.5:1 in dark and on every light screen; the light scheme is verified. Colour is
  never the only signal: a kind chip names the type, a banner names the state, a locked store says
  `in use`.
- TalkBack: one node per row reading key, value, then type; a sheet announces title and actions,
  traps focus while open and restores it to the invoking row on dismiss. Back dismisses the sheet,
  then the screen.
- Edge-to-edge: the list draws under the status and navigation bars, and content is inset by the
  navigation bar. Above 600 dp the layout moves to list-detail, stores left and the open surface
  right; it does not stretch a 380 dp column.

## 11. Out of scope for v1

Cache clear, sort, recursive search, breadcrumbs, JSON and XML pretty-print, image rendering, and
export or share. v2 order: cache clear, sort, recursive search, breadcrumbs, pretty-print, images,
export.

## 12. Open decisions

- Whether the value sheet collapses into inline expandable rows long term; inline is cheaper for
  short scalars, the sheet survives long values and binary.
- Whether SharedPreferences ships at all: no app in the repo writes one, so 02's second section, 16
  and 19 exist for the pattern, not for a real store.
