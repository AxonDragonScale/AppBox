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

- Database: store → tables → schema → rows → row sheet.
- Preferences: store → keys → value sheet.
- Files: roots → folders → file preview.

One store switcher concept: the tabs stay reachable from every root surface, including its loading,
empty, no-match and error states. Deep surfaces go back instead of switching stores, so nobody
loses the store they were in; depth is three below the list, plus a sheet.

## 3. Screen inventory

29 screens and 5 sheet overlays. Every screen is 412 × 915; sheets are 396 wide.

| # | Screen | Shows | State |
|---|---|---|---|
| 01 | `01 · Stores · databases · dark` | database rows with the file size; table count and version arrive when the store opens | content |
| 02 | `02 · Stores · preferences · dark` | DataStore and SharedPreferences stores in one list, each naming its kind | content |
| 03 | `03 · Stores · files · dark` | five roots, each with a description and an entry count | content |
| 04 | `04 · Stores · empty · dark` | no store of the active kind; the copy names that tab | empty |
| 05 | `05 · Stores · no matches · dark` | query with no store-name match; the search field's clear action empties it | no results |
| 06 | `06 · Stores · loading · dark` | ellipsis count and skeleton rows | loading |
| 07 | `07 · Database · tables · dark` | table rows, each with row and column counts | content |
| 08 | `08 · Database · tables empty · dark` | a database that holds no tables | empty |
| 09 | `09 · Database · busy · dark` | locked database, busy banner with retry over the empty surface | busy |
| 10 | `10 · Database · schema · dark` | DDL card, column rows, index rows | content |
| 11 | `11 · Database · rows · dark` | sticky column header, two-line data rows, row count | content |
| 12 | `12 · Database · rows · no matches · dark` | rows loaded, zero matches; the filter's clear action empties it | no results |
| 13 | `13 · Table empty · dark` | no records; the definition stays on 10 | empty |
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
| Stores per tab | 01 / 02 / 03 | 06 | 04 | 05 | banner with retry; a store that fails to open reports on its own surface | n/a: the scan never blocks | 26 |
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

- **Database:** 01 → store → 07 → table → 10, then the schema's `Rows` action → 11. A table opens
  its definition first, and back from 10 or 11 returns to 07. A locked open shows the busy banner,
  an unreadable one the error banner; 08, 13 and 12 cover no tables, no records and no match.
- **Preferences:** 02 → store → 15 → row → value sheet. Typing in the store search stays on 15; 18
  is the cross-store search from the home search; 16, 17 and 19 sit on the same surface.
- **Files:** 03 → root → 20. A folder opens 20 one level deeper and the path line grows; a file
  opens 23, 24 or 25; 21 and 22 replace the list only.

Back: a sheet dismisses first (back, scrim tap, close), a second back leaves the screen; every step
between a store and its rows is a screen, never an in-place swap. A sheet never resets the list
behind it: search text, the rows filter and the scroll offset survive dismiss on 11 and 15, and
scroll is restored under the light variants. Leaving a surface clears its query; the stores search
is one query that survives a tab switch until the viewer closes.

## 5. Layout and density

- Screen 412 wide, 16 side padding, 380 content, 4-pt grid. Top bar 64: title 22/500, then one 12
  muted line carrying identity and the read-only statement (`3 databases · 5 preference stores`,
  `Database · v1 · read-only`, `cache · read-only`). One line, never two.
- Lists scroll under a sticky 40 dp column header on 11 and a sticky section header elsewhere; the
  row-count line (`24 rows`) sits with that header. No paging, no footer, no "showing 1 to 10".
- Row grammar: store 56 two-line, table 48 two-line, database data row 64 two-line, keys 48
  single-line, file row 52 single-line, schema row 40 single-line.
- A database data row is two lines: the row's label at mono 13 plus one type chip, then
  `#id · column value` for up to two other cells at mono 11. `label` is the row's first readable
  text column, and the chip names the kind of the cell that label came from.
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
| Filter bar | 44; two 44 slots, sort and column filter, inert placeholders for v2; the rows filter is the surface's search field | component |
| Section header | 40; label 16/500, count 12 muted, optional text action 13/500 | component |
| Store row | 56; 36 icon tile, name 16/500, meta 12 muted, size mono 12 end, chevron 20 | component |
| Table row | 48, two lines; 18 icon, mono 12 name, mono 11 muted `24 rows · 6 columns`, chevron 16 | component |
| Database data row | 64, two lines; `#id · label` 14/500, preview mono 13 + type chip 20 | component |
| Key-value row | 48; key mono 12 in 150, type chip 20 in 64, value mono 13 flex | component |
| Schema row | 40; name mono 12 in 120, type chip 20, flag 12 muted | component |
| File row | 52; 20 kind icon, name 14/500, size mono 12 end; the folder variant adds a chevron | component |
| Path line | 24; mono 12 muted, one line, middle-ellipsized from the left, scrolls with the list | component |
| Meta chip, type chip | 20, radius 6, padding 8, mono 11, `chipFill` + 1 dp `outline`; the type chip names the kind in that kind's colour | component |
| Status pill, status banner | pill 20, radius 999, mono 11, unused in v1: the loading state is the `…` count plus skeletons; banner 72 with 20 icon, title 14/500, body 12 muted, an optional muted Details line and a 44 action below; the busy variant is 131 with that action inline | component |
| Empty state, skeleton row | 64 icon tile, title 16/500, body 14 muted, one action 15/500; a skeleton uses the parent row's height with 2 to 3 bars in `skeletonFill` | component |
| Info card | card 20, `surface` + 1 dp `outline`, one 13/500 line and one 12 muted line, opens a sheet | inline |
| Row sheet, value sheet | 24 radius, `surfaceHigh`, handle 40×4, title 18; the row sheet carries one 48 row per cell and two copy actions, the value sheet a mono 13 value block, meta row and `Copy value` / `Copy key` | component |

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
| Fills | `chipFill` on chips and the section count pill, `iconTileFill` behind icons, `skeletonFill` in skeletons |

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
  One search placeholder per scope: `Search databases`, `Search preference stores`, `Search tables`,
  `Filter rows`, `Search this store`, `Search files`.
- Counts are derived from the list they label, never typed, and each count appears once per surface:
  in the active tab segment on the home surface, in the section header on every surface below it.
  `24 rows`, `3 tables`, `Keys 8`. A top-bar subtitle carries no count; it names the store's version
  and `Read-only`.
- Plain language in the UI. A technical identifier (exception class, engine error) may appear only
  as a muted secondary `Details` line, never as the headline or the body.

## 9. Files and cache surface

Five roots, all read-only: app files, cache, no-backup files, external cache, code cache. A root that
does not exist for this install opens to 21 rather than disappearing from the list.

- Row grammar: 52, 20 kind icon, name 14/500, size mono 12 end-aligned, chevron for folders.
- Sizes are 1024-based: one decimal below 10 units, an integer from 10 up, `0 B` for zero, never a
  repeated unit, so 1024 KB prints as 1 MB. A file row shows the file's own size; a folder shows
  none, because sizing it would mean the recursive walk v1 does not do; the roots row shows a count
  only.
- `files/datastore/` is listed like any other folder: the files surface hides nothing, so there is
  no show/hide toggle and no hidden count. `databases/` and `shared_prefs/` are siblings of the files
  root, so they cannot appear inside it.
- Text preview is capped at 64 KB and always states `N of M bytes`; binary shows a hex dump, 8
  bytes a line with an ASCII gutter. A truncated preview is marked on screen (25), never silently
  shortened.
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
  navigation bar. Above 600 dp the board moves to list-detail, stores left and the open surface
  right; v1 keeps one column at every width instead of stretching a 380 dp column.

## 11. Out of scope for v1

Cache clear, sort, recursive search, breadcrumbs, JSON and XML pretty-print, image rendering, and
export or share. v2 order: cache clear, sort, recursive search, breadcrumbs, pretty-print, images,
export.

## Implementation notes

Where the build is narrower than the board, and why. The board above stays the target.

- **Each count appears once.** The tab segments carry the three store counts on the home surface;
  every surface below it puts its single count in the section header (`Tables 12`, `Columns 5`,
  `Rows 24`, `Keys 8`) and states only the store's version and `Read-only` in the top bar. The files
  surface names its path once, in the mono path line, and its top-bar subtitle is `Read-only` too.
- **Rows stop at 500.** `rows()` reads one capped page and `TableRows.total` keeps the real count,
  which the section header does not print: paging is not implemented, so a table past the cap lists
  its first 500 rows under a `Rows 500` header.
- **Hex is 8 bytes a line plus an ASCII gutter.** Sixteen bytes and a gutter do not fit the 356 dp
  content column at mono 11, and the gutter earns more than four extra bytes.
- **`Calculate size` is not implemented.** A folder size needs a recursive walk the file seam does
  not do, so a folder row shows no size rather than a partial one.
- **`View raw bytes` is not implemented.** The seam reads a preference store into entries and has no
  raw-bytes read, so an unreadable store offers `Retry` only.
- **The schema screen is reached from a table row, and the rows screen from the schema's `Rows`
  action.** A table therefore always opens its definition before its records.
- **Search is client-side over loaded data.** A table or store search filters one loaded page; the
  home preference search reads every store after a 250 ms settle and keeps 50 hits per store, and a
  query shorter than two characters is a store-name filter instead.
- **SharedPreferences is supported but empty in this repo.** Discovery reads `shared_prefs/*.xml`
  as XML, never through `getSharedPreferences`, but no app here writes one.
- **A WAL database opens through a query-only writable handle.** A read-only open cannot build the
  `-shm` index a live WAL file needs, so the fallback opens read-write and pins `query_only=ON`;
  no statement touches data and the database file is neither created nor modified.
- **Row typography differs from §6.** The data row leads with its label at mono 13 and puts
  `#id · column value` on line two at mono 11; table and file names are mono 13 and schema names
  mono 15, not mono 12; control labels are 14/500 where §6 says 15/500.
- **The filter bar holds one search field, with no sort or filter icons.** v1 drops the board's
  icon slots instead of shipping clickable buttons with no action; they return with v2's sort. The
  cross-store preference search likewise reports a failed read instead of an empty result: every
  store failing shows the error banner, and some failing keeps the hits under
  `Some stores could not be read.`
- **The row sheet also copies TSV.** `Copy as TSV` has no entry in §8's copy table.
- **Rows are one data grid, not two-line label rows.** 11 renders a sticky 40 dp header of real column
  names over 44 dp rows, one cell per column at mono 12; widths come from the header and a
  24-character sample, clamped to 88..240 dp, numeric columns end-align and the whole table (header
  included) scrolls sideways together. The schema loads with the rows so a table with no records
  still names its columns. The row sheet is unchanged.
- **A table's columns are a four-column grid.** Name 150, Type 96, Null 64, Key 56 over a 40 dp
  header and 44 dp rows, striped by a hairline only. `View rows` is a filled action under the DDL
  card; the section header has no text action.
- **The files surface hides nothing.** `files/datastore/` is a row like any other folder, with no
  show/hide toggle and no hidden count, so every section count is the whole truth.
- **A table row carries `DDL` and `Rows` actions.** Two 32 dp outlined buttons inside 48 dp targets
  grow the row from 48 to 56 dp; tapping the body still opens the definition.

## 12. Open decisions

- Whether the value sheet collapses into inline expandable rows long term; inline is cheaper for
  short scalars, the sheet survives long values and binary.
- SharedPreferences ships and is read as XML, but no app in the repo writes one, so 02's second
  section, 16 and 19 stay unexercised by a real store.
