---
title: "Verso UI — system, screens and copy"
status: in-progress
summary: "Verso's visual contract: paper-and-ink tokens, the type scale, the component kit, and every screen's contents, states and literal copy."
read_when: "Changing any Verso surface, its tokens, its components, or its copy."
---

> Context: [Main](../../context.md) > [Verso](./spec.md) > UI

# Verso UI

Behaviour is in [the spec](./spec.md). This doc is the visual contract: the system every surface
shares, then each screen's contents, states and words.

Design source: [`apps/verso/verso.fig`](../../../apps/verso/verso.fig). **Foundations** is complete —
cover, colour, typography, shape and layout, iconography, app icon. **Components** carries the full kit
in four batches: library (`Button`, `FilterChip`, `TabBar`, `ContinueCard`, `BookTile`, `BookRow`,
`CoverPlaceholder`, `BookDetailHeader`, `ImportRow`), reader (`ReaderTopBar`, `ReaderBottomBar`,
`PlayBar`, `ProgressSlider`, `SelectionBar`, `FocusFrame`, `FocusPeek`), lists and states (`VoiceRow`,
`SettingsRow`, `StatBlock`, `Sheet`, `EmptyState`), and the `SegmentedChoice`, `Stepper`, `Switch`,
`ThemeSwatch` controls. Variants stack inside each set.

**Screens** carries eight built states — the reader chrome, the three tabs with their empty and populated
variants, and the Settings sub-page. Sheets, focus mode and the book detail screen are
specified here and drawn next.

## The idea

A reading app should look like a book and behave like a lamp. The text is the product; the interface
is furniture that can be taken away.

- **The book is the interface.** Chrome is a guest: it arrives on demand, leaves on its own, and is
  never visible while the eye is moving.
- **Colour belongs only to the voice.** One warm highlight mark, nothing else. Buttons, sliders and
  progress are ink. There is no brand accent competing with a page.
  - Selection is grey. Only the voice is warm.
- **Type carries hierarchy; hairlines carry structure.** No cards, no elevation, no shadows, no
  gradients, no pills.
- **Square by default.** Sheets are the only rounded surface, and only at the top.
- **Focus mode is a different place**, not a zoomed page: one line on the same paper, nothing else.

## Theme

Two app palettes — Light and Night, following the system by default. The reading surface then picks
one of four reading themes, which also recolour the chrome while the reader is open.

| Role | Light | Night | Sepia | Black |
|---|---|---|---|---|
| Paper (ground) | `#F7F4EC` | `#16140F` | `#F1E6D2` | `#000000` |
| PaperSunk (bars, sheets) | `#EFEBDF` | `#1E1B15` | `#E8DBC4` | `#0B0B0A` |
| Ink (text) | `#191712` | `#EDE8D8` | `#2C2419` | `#E6E0D2` |
| InkMuted | `#6E6859` | `#A19A88` | `#6B5F4B` | `#8C8778` |
| Rule | `#DED8C7` | `#2E2A22` | `#D6C6AA` | `#262420` |
| Highlight | `#F2C94C` | `#F2C94C` | `#F2C94C` | `#F2C94C` |
| Selection | ink 14% | ink 20% | ink 16% | ink 22% |
| Error | `#B3261E` | `#F2B8B5` | `#9E2A20` | `#F2B8B5` |

Highlight is applied as tint, never as a second ink:

- Sentence wash: Highlight at 10% (light, sepia) / 14% (night, black).
- Spoken word: Highlight at 28% (light, sepia) / 34% (night, black).
- Optional accessibility cue: 2 dp Highlight rule under the spoken word.
- Bionic emphasis is weight only, never colour. It sits underneath the highlight with no conflict.

Numeric tokens: spacing 4 / 8 / 12 / 16 / 24 / 32 / 48; radii 0 everywhere except sheet tops at 16;
hairline 1 dp; touch target 48 dp; icons 24 dp.

## Typography

| Style | Size / line | Face | Use |
|---|---|---|---|
| Display | 34 / 40 | Fraunces SemiBold | Library header, Stats numbers |
| Headline | 26 / 32 | Fraunces SemiBold | Book title on detail, dictionary headword |
| Title | 20 / 26 | Fraunces Regular | Sheet titles, section heads |
| UI | 14 / 20 | Inter Regular | Rows, body copy, values |
| UI Strong | 14 / 20 | Inter SemiBold | Row titles, selected filter |
| Label | 11 / 16, 1.6 tracking | Inter SemiBold, caps | Actions, section labels |
| Micro | 10 / 12, 1.2 tracking | Inter Medium | Counts, chapter names, file size |
| Reading | 18 / 28 default | Literata Regular | The book |
| Focus | 32 / 44 default | Literata Regular | Focus mode sentence frame |

- Reading and Focus faces are adjustable: Literata, Inter, system serif, system sans. The chosen face
  applies wherever the book does not specify one.
- Focus mode caps its own display scale at 10% above the user's choice so the frame stays stable, then
  shortens the sentence at clause boundaries rather than shrinking the type.
- No type below 10 sp, ever, and no tracked caps longer than three words.

## Layout

- Design canvas 412 × 915; app padding 24; content width 364.
- Reader: top bar 56, page 753, play bar 48, ribbon 2, bottom bar 56.
- Reader text column 328 dp, centred, with 32 dp gutters that double as the chrome tap band.
- Chrome: top bar 56 dp, bottom bar 56 dp with the 2 dp ribbon as its own band above it, pinned play
  bar 48 dp, sheets up to 60% height, scrim ink 40%.
- Status and navigation bars are transparent; the icon colour follows the current theme.
- Wide screens: text column capped at 560 dp and centred; library grid gains columns, nothing else.
- Grid: 3 columns at 412, 10 dp gutter, cover 3:4, title 12 sp under the cover — three reads as a
  shelf, two reads as a gallery.

## Components

| Component | Variants / properties | Notes |
|---|---|---|
| `ContinueCard` | text `Title`, `Chapter`, `Percent` | 364 × 96; cover 54 × 80 left, `CONTINUE READING` eyebrow in Micro, title UI Strong, chapter Micro, percent right in Display 34; 2 dp progress rule flush under the card |
| `BookTile` | text `Title`; `State=Reading` · `Finished` | cover with 1 dp Rule border, 2 dp progress rule under it; finished tiles show a full rule and no percent |
| `BookRow` | text `Title`, `Author`, `Percent` | 72 dp; cover 40 × 56, percent right in Micro |
| `CoverPlaceholder` | text `Initials` | PaperSunk block, 1 dp Rule border, initials in Fraunces at 20 |
| `FilterChip` | `Selected=Yes` · `No`, text `Label` | square, 1 dp Rule; selected inverts to ink ground with paper label |
| `ImportRow` | text `Name`, `State=Importing` · `Done` · `Failed` | one line per file inside the blocking import flow, not in the library |
| `BookDetailHeader` | text `Title`, `Author`, `Series` | cover 132 × 188 centred, series in Micro under the author |
| `ActionButton` | `Style=Filled` · `Outline` · `Text`, text `Label` | 48 dp, square, no radius; filled = ink ground, paper label |
| `ReaderTopBar` | text `Title`, `Chapter`, `Percent` | 412 × 56; back, title over chapter, then `42%` over `3H 20M LEFT` at the right |
| `ReaderBottomBar` | `State=Idle` · `Playing` | 412 × 58 — 2 dp ribbon over the 56 dp row of five 48 dp targets |
| `ProgressSlider` | — | 2 dp track in Rule, filled in Ink, 12 dp square thumb, chapter ticks; lives in Contents, never on the page |
| `PlayBar` | `State=Playing` · `Paused` · `Loading` | 48 dp; spoken sentence one line, speed chip and glyph right |
| `SpeedStepper` | text `Value` | 48 dp; − / value / +, value in Display 20, presets row beneath |
| `VoiceRow` | `State=Idle` · `Selected` · `Downloaded` · `Downloading` · `Failed` | 64 dp; name, engine in Micro, trailing state |
| `ThemeSwatch` | `Selected=Yes` · `No` | 64 × 64 square, paper ground with ink `Aa`, 2 dp ink border when selected |
| `SegmentedChoice` | 2 / 3 / 4 options | 48 dp, 1 dp track, selected inverts to ink |
| `Stepper` | text `Value` | −/+, live updates behind the sheet |
| `Switch` | `On` · `Off` | 44 × 24 square track, square thumb |
| `Sheet` | text `Title` | top radius 16, PaperSunk, 1 dp rule on top, handle 32 × 2 dp 16 from the top, title 24 below the handle |
| `TabBar` | `Active=Library` · `Stats` · `Settings` | 64 dp, 1 dp Rule on its top edge, 3 dp ink rule flush under the top edge of the active segment |
| `SelectionBar` | text `Actions` | floating 48 dp bar above the selection, text actions only |
| `StatBlock` | text `Value`, `Label` | 114 × 88, value 28/34, label 10 caps, 1 dp rule under — three-up across 364 |
| `FocusFrame` | `Shape=Sentence` · `Word` | 364 × 132 fixed, or 364 × 88 for one word; never resizes between sentences |
| `FocusPeek` | — | current line at full strength, previous and next at InkMuted |
| `SettingsRow` | text `Title`; `Trailing=Chevron` · `Switch` · `Value` | 64 dp, 1 dp Rule inset 24 |
| `EmptyState` | text `Title`, `Description` | no illustration; one centred paragraph and one action |

## Screens

| # | Screen | Built as |
|---|---|---|
| 1 | Reader — paged | `01 · Reader · paged · chrome` |
| 2 | Library — empty | `02 · Library · empty` |
| 3 | Library — grid | `03 · Library · grid` |
| 4 | Library — list | `04 · Library · list` |
| 5 | Import — blocking flow | to draw — replaces the old inline importing state |
| 6 | Stats — empty | `06 · Stats · empty` |
| 7 | Stats — full | `07 · Stats · full` |
| 8 | Settings | `08 · Settings` |
| 9 | Settings — Data & storage | `09 · Settings · Data & storage` |
| 10 | Library — search | to draw |
| 11 | Shelves | to draw |
| 12 | Book detail | to draw |
| 13 | Reader — scroll | to draw |
| 14 | Contents | to draw |
| 15 | Display | to draw |
| 16 | Voice | to draw |
| 17 | Read aloud settings | to draw |
| 18 | Voice downloads | to draw |
| 19 | Selection and dictionary | to draw |
| 20 | Focus — sentence · word · peek | to draw |
| 21 | End of book | to draw |
| 22 | About | to draw |
| 23 | Dialogs | to draw |

## Library

**Contents**

- Header: `Verso` in Display, with a single magnifier icon in a 48 dp target at the right edge.
- Continue card, only when a book is in progress: eyebrow `CONTINUE READING`, title, chapter, percent,
  2 dp progress line flush under the card. The whole card opens the book at the reading position.
- Filter row: `All`, `Unread`, `Reading`, `Finished` chips, and a trailing `SORT` text action.
- Grid: three columns 114 wide, 10 dp gutters, cover 3:4, 2 dp progress line under each cover, title in
  two lines beneath.
- Tab bar with `LIBRARY` active.

**States**

- *Populated, grid* — the default.
- *Populated, list* — rows 72 dp: cover 40 × 56, title, author, percent right. Denser, same data.
- *Empty* — no books at all: `No books yet`, `Import a book to start.`, `Import books` (filled) and
  `Import a folder` (outlined), then the single reassurance line `BOOKS STAY ON THIS DEVICE.` No
  continue card, no filter row.
- *Filtered to nothing* — `Nothing here yet.` naming the active filter, one action `Clear filter`.
- *No book in progress* — the continue card is absent; the grid starts under the header.
- *Loading* — one line `Preparing your library…`; no skeleton cards.

Importing is **not** a library state. It is a blocking flow that owns the screen until it finishes; the
library never shows import progress inline, never shows a half-imported shelf, and is not usable
mid-import.

**Copy** — `No books yet` · `Import a book to start.` · `Books stay on this device.` · `Import books` ·
`Import a folder` · `Nothing here yet.` · `Clear filter` · `Sort`

## Library · search

Opens as a sheet over the library, from the magnifier.

**Contents**

- Field pinned at the top, square, 1 dp Rule, placeholder `Title, author, series, tag`.
- Results grouped: Books, Series, Shelves, Tags; the matched fragment in UI Strong.

**States**

- *Empty query* — nothing but the field; no recents, no suggestions.
- *Results* — grouped rows; tap opens.
- *No matches* — `No matches.` one line.

**Copy** — `Title, author, series, tag` · `No matches.`

## Shelves

Reached from the `SORT` sheet's shelves entry, or a book's `Add to shelf`.

**Contents**

- Shelves with book counts, plus `New shelf`.
- Each row: name, count, trailing menu for rename and delete.

**States**

- *Empty* — `No shelves yet.` and `New shelf`.
- *Populated* — rows with counts; tapping opens the library filtered to that shelf.
- *Rename* — dialog with the name pre-filled; an empty name is refused.
- *Delete* — confirmation stating books are not deleted.

**Copy** — `No shelves yet.` · `New shelf` · `Delete shelf? Books stay in your library.`

## Import

A blocking flow. Import owns the screen from the moment files are chosen until every file has landed or
failed; there is no half-imported library and no progress row inside it. The flow is entered from the
library's empty state, from the magnifier's overflow, or from the system share sheet.

**Contents**

- A 56 dp top bar with `Import` and nothing else — no cancel in the bar, because cancelling mid-file is
  a decision, not an escape.
- The overall line: `Importing 3 of 12`, percent in Display beside it.
- The current file name in UI, one line, ellipsized in the middle so the extension stays visible.
- A 2 dp overall progress line, determinate, with a per-file segment marked beneath it.
- A rolling list of finished files, one line each, checkmark and name, capped at the last five.
- Failures accumulate silently; they are reported at the end, never mid-run.

**States**

- *Choosing* — the system file picker is up; the screen behind it is the library, and nothing is
  blocked yet.
- *Preparing* — reading metadata and covers: `Preparing…` with an indeterminate line, no numbers yet.
- *Importing* — `Importing 3 of 12`, determinate overall progress, the current file named.
- *Slow file* — a single large file holds the line at `7 of 12` with the file named; the line animates
  at a lower rate rather than stalling silently.
- *Finishing* — covers written, index updated: `Finishing…`, once, briefly.
- *Done, all good* — `12 books imported.` and one action, `Back to library`.
- *Done, with failures* — `9 of 12 imported.` then the failure list: file name and one plain reason
  each (`This book is protected and can't be opened.`). One action, `Back to library`.
- *Cancel requested* — a confirmation: `Stop importing? 3 books are already in your library.` with
  `Keep importing` and `Stop`. Books already imported stay.
- *Stopped* — `Import stopped. 3 books were added.` and `Back to library`.
- *Nothing selected* — returns to the library unchanged, no message.

The flow never leaves a partially written book behind: a file either lands whole or not at all.

**Copy** — `Import` · `Preparing…` · `Importing 3 of 12` · `Finishing…` · `12 books imported.` ·
`Back to library` · `Stop importing? 3 books are already in your library.` · `Import stopped.`

## Book detail

**Contents**

- Cover 132 × 188 with a 1 dp Rule; title in Headline, author in UI, series and `EPUB · 2.4 MB` in Micro.
- Progress block: percent in Display, chapter beneath, `about 3 h left` in Micro when an estimate exists.
- Actions: filled `Read` / `Resume` / `Read again`, outlined `Read aloud`.
- Rows: `Add to shelf`, `Tags`, `Bookmarks` with a count.
- `Mark finished` / `Mark unread`, then `Delete` alone at the bottom, separated by 24 dp and a rule.

**States**

- *Unread* — `Read`, no progress block.
- *In progress* — `Resume`, percent and chapter, time left when known.
- *Finished* — `Read again`, no percent.
- *No cover* — generated placeholder from the title's initials.
- *No metadata* — `Unknown author`; the series line is absent rather than empty.
- *Deleted while open* — returns to the library with `That book is no longer in your library.`

**Copy** — `Resume` · `Read aloud` · `Add to shelf` · `Tags` · `Bookmarks` · `Mark finished` · `Delete`

## Reader

The product. Everything else exists to get here.

**Contents**

- Top bar 56: back, title in UI Strong with the chapter in Micro beneath, then a two-line readout at the
  right — `42%` over `3H 20M LEFT`. Nothing here is an action except back.
- Page 753: the text column, 348 wide, 32 dp gutters.
- Play bar 48, pinned above the ribbon whenever audio is active: the spoken sentence ellipsized to one
  line, the speed as `1.4×`, play/pause. Tapping the sentence opens the reader there without stopping
  playback.
- Ribbon 2: its own band above the bar, track in Rule, ink to the reading position, chapter ticks.
- Bottom bar 56: five 48 dp targets — `Contents`, `Aa`, the read-aloud primary, `Focus`, `More`. The
  primary is an ink square with a paper glyph and is the only filled control in the reader. Targets keep
  4 dp of air above and below; the ribbon never overlays them.
- Bars are PaperSunk with a 1 dp Rule on their inner edge, no shadow, no blur, and animate in with a
  160 ms fade and 8 dp slide.

**Page behaviour**

- No header, no footer, no page number — the chrome carries it, and position is always a percent.
- Paragraph spacing and first-line indent are the book's, unless `Use my typography` is on.
- A page never ends with a single orphan word; the last line moves to the next page.
- Images stay inside the column, 1 dp Rule frame, caption in Micro.

**States**

- *Chrome hidden* — the default: text only on the full 915. A margin tap brings the bars back; they hide
  after four seconds or on the first page turn.
- *Chrome up* — all bars visible, page still legible above them.
- *Read aloud idle* — no play bar; primary shows play.
- *Read aloud playing* — play bar present, sentence highlighted in the page, primary shows pause, the
  ribbon advances with the voice.
- *Read aloud paused* — as playing, primary shows play, highlight stays where the voice stopped.
- *Read aloud unavailable* — no engine: word taps do nothing, the primary is disabled, and one line of
  explanation appears once in a dialog, never as a toast.
- *No voice for this language* — the primary opens the voice sheet with the language named.
- *End of book* — last page plus the end-of-book panel.
- *Scroll mode* — same chrome; the page scrolls instead of turning, the ribbon still tracks.

There is no slider on the page. Scrubbing belongs to Contents, which owns the progress track and the
bookmarks.

**Copy** — `Contents` · `Aa` · `Focus` · `More` · `42%` · `3H 20M LEFT` · `1.4×`

## Contents

**Contents**

- Progress track at the top: thumb at the current position, a tick per chapter, the chapter named under
  the thumb while dragging. Releasing jumps; the sheet stays open so the next chapter is one tap away.
- Chapter list, nested by heading level, the current chapter marked.
- Bookmarks: snippet, chapter, percent, date; tap to jump, long-press to delete. `Add bookmark` marks
  the current sentence. Bookmarks are made and browsed here; the chrome has no bookmark action.

**States**

- *Scrubbing* — the chapter follows the thumb; the page does not move until release.
- *No chapters* — a single `Start` entry, or the book's headings when it has them.
- *No bookmarks* — `No bookmarks yet.` with the add action still present.
- *Many chapters* — the list scrolls under the pinned track; the current chapter is in view on open.

**Copy** — `Contents` · `Add bookmark` · `No bookmarks yet.`

## Display

A 60% height sheet, so the page above stays visible and updates live.

**Contents**

- Theme swatches: Light, Sepia, Night, Black, Custom.
- `Use my typography`, then font, size, line height, margins, alignment, hyphenation, letter and word
  spacing.
- `Bionic reading`, `Keep screen on`.

**States**

- *Default* — publisher styling on, theme Light.
- *Publisher CSS on* — font and margin controls read as inactive under `Using the book's styling`;
  colours and size still apply.
- *Custom theme* — three colour fields appear under the swatches.
- *Any change* — applies immediately behind the sheet; no confirm, no apply.

**Copy** — `Use my typography` · `Using the book's styling` · `Bionic reading` · `Keep screen on`

## Voice

Quick controls, opened from the play bar. Choosing the engine and setting defaults is **Read aloud
settings**, not this sheet.

**Contents**

- Engine line at the top in Micro — the current engine's name, tapping it opens Read aloud settings.
- Current voice as a selected `VoiceRow` with a `Sample` text action.
- Speed: `SpeedStepper` with the value, plus `0.75 1 1.25 1.5 2`.
- Pitch, only when the engine supports it — absent, not disabled, when it does not.
- `Download voices` row at the bottom with total space used.

**States**

- *Engine present* — voice list filtered to the book's language.
- *No engine* — the sheet is replaced by `Install a voice to hear books read aloud.` and `Open settings`.
- *No voice for the language* — `No voice for this language.` above the download entry.
- *Voice removed while playing* — falls back to the system default and says so once.

**Copy** — `Sample` · `Download voices` · `No voice for this language.` · `Install a voice to hear books read aloud.`

## Read aloud settings

The full configuration, reached from Settings → Read aloud, from the engine line in the Voice sheet,
or from a missing-voice prompt.

**Contents**

- **Engine** — two sources, one chosen at a time:
  - `Device engine` — every text-to-speech engine installed on the phone, listed with its name and
    version; a radio marks the active one. At least one is normally present (`Google speech services`,
    plus any device vendor engine).
  - `Downloaded engine` — Verso's own on-device model, with its size, quality tier and install state.
    Choosing it while no model is installed offers the download.
- **Voice** — the voices the chosen engine exposes for the book's language, grouped by language. Each
  row shows the name, and where the engine reports them, gender and variant — `English (UK) · Female ·
  1`. A quality label appears when the engine exposes one. Selecting a row applies immediately; `Sample`
  speaks a fixed sentence.
- **Speed** — 0.5× to 3.0×, continuous, with `0.75 1 1.25 1.5 2` as shortcuts. Stored per engine, so
  switching engines does not carry an impossible value.
- **Pitch** — 0.5 to 2.0, shown only when the engine supports it.
- **Voices** — a row into Voice downloads with total space used.

**States**

- *Device engine, several installed* — all listed; the active one marked. Switching keeps the book's
  place and re-selects a voice in the same language.
- *Downloaded engine, not installed* — the row reads `Not downloaded · 284 MB` with `Download`; the
  engine cannot be selected until the model is present.
- *Downloaded engine, downloading* — the row carries inline progress and a cancel action; the current
  engine stays in use until it completes.
- *No engine at all* — the screen reduces to `Install a voice to hear books read aloud.` and
  `Open settings`.
- *Engine without gender metadata* — gender is omitted, never guessed.
- *No pitch support* — the pitch section is absent, not disabled.
- *Voice list empty for a language* — `No voice for this language.` with the downloads entry.

**Copy** — `Device engine` · `Downloaded engine` · `Voice` · `Speed` · `Pitch` · `Sample` ·
`Not downloaded` · `No voice for this language.`

## Voice downloads

**Contents**

- Languages as sections; each voice a `VoiceRow` with size, quality, offline state.
- Footer: `1.2 GB used by voices` and a `Delete all` text action with confirmation.

**States**

- *Available* — `284 MB`, download action.
- *Downloading* — `Downloading… 37%`, cancel action; no numeric ring, no progress bar.
- *Downloaded* — `Downloaded`, with delete.
- *Failed* — `Failed` in Error with `Retry`.
- *Offline* — download actions disabled with `Connect to download voices.`

**Copy** — `Downloading…` · `Failed` · `Retry` · `1.2 GB used by voices` · `Delete all`

## Selection and dictionary

**Contents**

- Long-press selects a word with standard handles; the selection tint is grey, never Highlight.
- `SelectionBar` floats above the selection: `Define`, `Translate`, `Copy`, `Read from here`. Actions with
  no handler on the device are absent, not disabled.
- Dictionary sheet: headword in Headline, part of speech in Micro, definitions in UI, source in Micro,
  `Open in dictionary` last.

**States**

- *Word* — the full bar.
- *Phrase* — `Define` absent; `Translate` and `Copy` remain.
- *No dictionary installed* — `Define` absent; `Translate` offered when a provider exists.
- *No definition found* — `No definition found.` with `Translate` still available.

**Copy** — `Define` · `Translate` · `Copy` · `Read from here` · `No definition found.`

## Focus mode

**Contents**

- Focus keeps its own token set per theme. Same room as the page, but each theme sets these values
  independently:
  - Ground one step deeper than Paper — PaperSunk in light and sepia, near-black in night and black.
  - Type at full-strength Ink, never InkMuted, for the current sentence.
  - Highlight stronger than the page's: 34% light and sepia, 40% night and black.
  - Context — peek lines, chapter hairline — at 55% ink.
- No chrome, no bars, no page edges. A hairline row at the top carries the chapter in Micro and an
  `Exit` glyph with a 48 dp target.
- Sentence frame 364 × 132, sentence at 32/44, centred, three lines maximum. Word frame 364 × 88, one
  word at 40/48, anchor never moves.
- The spoken word carries the focus tint with full-strength ink; the rest of the sentence stays at
  InkMuted.

**States**

- *Sentence, silent* — advance is timed; only the highlight moves, no spinner, no countdown.
- *Sentence, playing* — advance follows the voice.
- *Word mode* — one word at the anchor; punctuation holds the beat.
- *Paused* — the frame holds; a tap resumes.
- *Peek* — long-press shows the previous and next sentences at InkMuted, current at full.
- *Finished* — `Finished` in Headline with `Back to book` and `Mark finished`.
- *Images and tables* — skipped with a small `1 IMAGE` marker, not tappable.

**Copy** — `Finished` · `Back to book` · `Mark finished` · `1 IMAGE`

## Stats

**Contents**

- Header `Stats`.
- Four blocks in a 2 × 2 grid: `Today`, `Day streak`, `Total time`, `Books read`.
- Seven-day strip: one column per day, ink for minutes, day letters under, no axis.
- `Books` list: cover 40 × 56, title, time, a proportional 2 dp bar per book.

**States**

- *Empty* — `No reading yet.` and `Listening counts as reading. Time appears here once you open a book.`
  No zeroed blocks, no empty chart.
- *Partial* — first session today: today and streak are real, total time reads `Under an hour`, the
  strip shows one bar.
- *Full* — four blocks, seven bars, the book list.
- *Time left unknown* — a book row omits the estimate rather than showing a dash.
- *Cleared* — back to the empty state after the confirmation.

No comparison to friends, no goals, no notifications.

**Copy** — `Today` · `Day streak` · `Total time` · `Books read` · `No reading yet.`

## Settings

**Contents**

- Sections: `Reading`, `Read aloud`, `Focus`, `Data`, `About`, each a label over rows.
- Rows carry their current value at the right in InkMuted; controls sit where a value would.
- Reading: theme, type, bionic reading.
- Read aloud: `Engine` (`Google speech services` / `Verso voice`), `Voice`, `Speed`, `Pitch` (when the
  engine supports it), and `Voices` into downloads. Each row is a value with a chevron; all of them
  open Read aloud settings, which owns the detail. The screen shows the current choice, it does not
  edit it inline.
- Focus: shape.
- Data: `Data & storage`.
- About: `About`.

**States**

- *Default* — values reflect current settings.
- *No voice engine* — the `Engine` row reads `None installed` in Error and still opens the settings.
- *Downloaded engine selected but not installed* — the engine row reads `Not downloaded` with a chevron.
- *Changed value* — the row updates in place; there is no save button anywhere.

Destructive actions never live in a row list.

## Data & storage

**Contents**

- Summary: total size, split between books and voices.
- `Export library`, `Import a backup`, `Clear cache` with its size.
- `Books`: title and size per book. `Voices`: language and size per voice.

**States**

- *Populated* — sizes per book and voice.
- *Empty library* — the sections read `No books` / `No voices`; export remains.
- *Exporting* — one progress row replaces `Export library`, cancellable.
- *Import conflict* — merges, never replaces; conflicts ask once per book with `Apply to all`.
- *Clearing cache* — confirmation first; the size updates in place.

**Copy** — `Export library` · `Import a backup` · `Clear cache` · `No books` · `No voices`

## About

Version, voice-model attribution, licences. One state, nothing dynamic.

## End of book

`Finished` in Headline, the book title beneath, `Mark finished` and `Back to library`. If the book was
already marked, the primary becomes `Back to library` and marking drops to a text action. If read-aloud
reached the end, playback has already stopped.

## Dialogs

| Dialog | Shown when | Copy |
|---|---|---|
| Delete book | `Delete` on book detail | `Delete "…"? Progress and bookmarks go with it.` |
| Delete shelf | Shelf menu | `Delete "…"? Books stay in your library.` |
| Clear stats | Settings | `Clear reading history? This cannot be undone.` |
| Import summary | Batch ends with failures | File name and reason per row |
| Import conflict | Import finds newer positions | `Newer position found` · `This device` · `Imported` · `Apply to all` |
| No engine | First attempt to read aloud | `Install a voice to hear books read aloud.` |
| Engine stopped | Voice dies mid-sentence | `Voice stopped.` · `Retry` |

Paper ground, square, 280 wide, 1 dp Rule border, title in Title, body in UI, two text actions.
Destructive confirmations put the verb last and never say "OK".

## Copy

Rules:

- Sentence case everywhere except the tracked caps labels, which are never more than three words.
- No exclamation marks, no "Oops", no "Whoops", no emoji, no "Let's".
- An error states what happened and what the reader can do, in that order.
- Percentages are whole numbers. Durations read `42 m`, `2 h 14 m`. Counts read `1 book`, `3 books`.
- The book is always "the book", never "the title" or "the file".
- Listening and reading are never named differently in copy: it is all `reading`.
- One exception: the reader's top-bar readout is a tracked label, not prose, so it keeps compact caps —
  `42%` over `3H 20M LEFT`. Everywhere else, durations are spaced lowercase.

Literal copy lives with its screen above; do not add strings here that a screen does not use.

## Motion

| Event | Motion |
|---|---|
| Page turn | 180 ms horizontal slide, ease-out; no curl, no fade through white |
| Chrome in / out | 160 ms fade + 8 dp slide |
| Sheet | 240 ms slide up, scrim fades with it |
| Highlight advance | 90 ms tint crossfade; no bounce, no scale |
| Focus sentence change | 120 ms crossfade; word mode is instant |
| Theme change | 200 ms crossfade of ground and ink only |
| Finished panel | 160 ms fade |

Reduced motion: page turns, highlight and focus crossfade become instant. Sheets still slide, because a
sheet that appears with no transition reads as a glitch.

## Accessibility

- Every target is at least 48 dp; the reader's margin band is 32 dp because its action is reversible, and
  it never overlaps the text column.
- Body text meets 4.5:1 in all four reading themes; Highlight tints keep ink readable and are never the
  only cue — the optional underline exists for that.
- TalkBack order in the reader: chrome, text, play bar. Reading text is exposed per paragraph, not per
  word.
- Dynamic type scales all UI chrome; the reading surface scales through its own control.
- Focus mode scales with both, then shortens sentences at clause boundaries instead of shrinking type.
- The play bar announces state changes: `Playing`, `Paused`, `Voice stopped`.

## Cross-cutting states

- **Offline** — everything works. Only voice downloads need the network, and say so plainly when it is
  absent.
- **Rotation, resize, fold** — position preserved on every screen; the reader reflows in place.
- **Process death** — the reader restores to the sentence, the library to its scroll position.
- **Errors** — one sentence, one action, never an error code.
- **Empty vs loading** — an empty list and a loading list never look alike: loading is one line, empty
  is a sentence.
