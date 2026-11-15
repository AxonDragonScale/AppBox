---
title: "Verso UI — reader design system"
status: in-progress
summary: "Screen inventory, tokens, components, copy and motion for Verso: a paper-and-ink reader where colour belongs only to the spoken word."
read_when: "Designing or implementing any Verso surface, or changing its tokens, components or copy."
---

> Context: [Main](../../context.md) > [Verso](./spec.md) > UI

# Verso UI

Behaviour lives in [the spec](./spec.md). This doc is the visual contract: what every surface looks
like, which states exist, and what the words say. No `.fig` exists yet — when one does, this doc is
what it gets built from and the design source moves here, as Wallmatic does.

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
| Display | 34 / 40 | Fraunces Soft SemiBold | Library header, Stats numbers |
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
- Focus mode caps its own display scale at 10% above the user's choice so the frame stays stable,
  then shortens the sentence at clause boundaries rather than shrinking the type.
- No type below 10 sp, ever, and no tracked caps longer than three words.

## Layout

- Design canvas 412 × 915; app padding 24; content width 364.
- Reader text column 328 dp, centred, with 32 dp gutters that double as the chrome tap band.
- Reader margins: 32 dp sides, 24 dp above the first line and below the last.
- Chrome: top bar 56 dp, bottom bar 72 dp, pinned play bar 48 dp sitting directly above the bottom
  bar, sheets up to 60% height, scrim ink 40%.
- Status and navigation bars are transparent while reading; their icons follow the reading theme.
- Wide screens: text column capped at 560 dp and centred; library grid gains columns, nothing else.
- Grid: 3 columns at 412, 10 dp gutter, cover 3:4, title 12 sp under the cover — three reads as a shelf, two reads as a gallery.

## Components

| Component | Variants / properties | Notes |
|---|---|---|
| `ContinueCard` | text `Title`, `Chapter`, `Percent` | 364 × 96; cover 54 × 80 left, title UI Strong, chapter Micro, percent right-aligned in Display 34; 2 dp progress rule flush under the card |
| `BookTile` | text `Title`; `State=Reading` · `Finished` | cover with 1 dp Rule border, 2 dp progress rule under it; finished tiles show a full rule and no percent |
| `BookRow` | text `Title`, `Author`, `Percent` | 72 dp; cover 40 × 56, percent right in Micro |
| `CoverPlaceholder` | text `Initials` | PaperSunk block, 1 dp Rule border, initials 20 sp Fraunces in InkMuted |
| `FilterChip` | `Selected=Yes` · `No`, text `Label` | square, 1 dp Rule; selected inverts to ink ground with paper label |
| `ImportRow` | text `Count`, `State=Importing` · `Failed` | full-width rule, 1 dp progress line, count in Micro |
| `BookDetailHeader` | text `Title`, `Author`, `Series` | cover 132 × 188 centred, series in Micro under the author |
| `ActionButton` | `Style=Filled` · `Outline` · `Text`, text `Label` | 48 dp, square, no radius; filled = ink ground, paper label |
| `ReaderTopBar` | text `Title`, `Chapter` | 56 dp; back, title truncated at 24 chars in UI Strong, chapter in Micro below |
| `ReaderBottomBar` | text `Percent`, `Chapter` | 72 dp; percent left, chapter centre, three 48 dp actions right |
| `ProgressSlider` | — | 2 dp track in Rule, filled in Ink, 12 dp square thumb, chapter ticks 1 × 8 dp every boundary, chapter name appears above the thumb while dragging |
| `PlayBar` | `State=Playing` · `Paused` · `Loading` | 48 dp; spoken sentence one line, 1 dp top rule, speed chip and play glyph right |
| `SpeedStepper` | text `Value` | 48 dp tall; − / value / +, value in Display 20, presets row beneath |
| `VoiceRow` | `State=Selected` · `Idle` · `Downloaded` · `Downloading` · `Failed` | 64 dp; name, engine in Micro, trailing state |
| `ThemeSwatch` | `Selected=Yes` · `No` | 64 × 64 square, paper ground with ink "Aa", 2 dp ink border when selected, custom swatch opens the colour fields |
| `SegmentedChoice` | 2 / 3 / 4 options, text `Value` | 48 dp, 1 dp track, selected inverts to ink |
| `Stepper` | text `Value` | −/+, live updates behind the sheet |
| `TabBar` | `Active=Library` · `Stats` · `Settings` | 64 dp, 1 dp Rule on its top edge, full-width 3 dp ink rule flush under the top edge of the active segment |
| `Switch` | `On` · `Off` | 44 × 24 square track, square thumb, no radius |
| `Sheet` | text `Title` | top radius 16, PaperSunk, 1 dp rule on top, drag handle 32 × 2 dp, title 16 dp below handle |
| `FocusFrame` | `Shape=Sentence` · `Word`, text `Line` | 364 wide, fixed height 3 lines (sentence) or 1 line (word), never resizes between sentences |
| `FocusPeek` | — | current line at full strength, previous and next at InkMuted, no chrome |
| `StatBlock` | text `Value`, `Label` | 172 wide, value Display 34, label Label caps, 1 dp rule under |
| `SettingsRow` | text `Title`, `Subtitle`; `Trailing=Chevron` · `Switch` · `Value` | 64 dp, 1 dp Rule inset 24 |
| `SelectionBar` | text `Actions` | floating 48 dp bar above the selection, paper ground, 1 dp rule, text actions only |
| `EmptyState` | text `Title`, `Description` | no illustration; one centred paragraph and one action |

## Screens

| # | Screen | Purpose | Key states |
|---|---|---|---|
| 1 | Library — empty | First run | Empty, importing |
| 2 | Library — grid | The shelf | Reading, finished, many books |
| 3 | Library — list | Denser shelf | Same |
| 4 | Library — importing | Progress | Importing, failed files |
| 5 | Library — search | Metadata search | Typing, no matches, results |
| 6 | Shelves | Manage collections | Empty, populated, rename/delete |
| 7 | Book detail | One book | Unread, in progress, finished |
| 8 | Reader — paged | The book | Idle, playing, chrome on/off |
| 9 | Reader — scroll | The book | Same |
| 10 | Display sheet | Typography | Live preview above the sheet |
| 11 | Voice sheet | Voice, speed, pitch | No voice, engine missing |
| 12 | Voice downloads | Neural voices | Available, downloading, failed |
| 13 | Contents sheet | Chapter navigation | Nested, flat, no chapters |
| 14 | Bookmarks sheet | Saved places | Empty, populated |
| 15 | Selection | Word actions | Word, phrase |
| 16 | Dictionary sheet | Definition | Found, not found, no provider |
| 17 | Focus — sentence | Low eye movement | Live, paused, peek |
| 18 | Focus — word | Low eye movement | Live, paused, peek |
| 19 | Stats | Time and streaks | Empty, partial, full |
| 20 | Settings | All preferences | — |
| 21 | Data & storage | Export, import, size | — |
| 22 | About | Version, attributions | — |
| 23 | Dialogs | Deletes, conflicts | Delete book, clear stats, import conflict, import summary |

## Library

### Empty

- Header `Verso` in Display, one line under it: `EPUB, FB2, TXT, Markdown, HTML.`
- One filled action `Import books`, one text action `Import a folder`.
- One closing line in InkMuted: `Books stay on this device.`
- Nothing else: no carousel, no sample book, no tour.

### Grid

- Continue card pinned above the grid, one only.
- Filter row: `All` `Unread` `Reading` `Finished` chips, scrolling horizontally at large text sizes.
- Sort is a text button in the header row, not a chip.
- Grid 3-up; a book never shows a badge, a price, or a download state — every book here is local.
- Empty filter result: one line `Nothing here yet.` with no action.

### List

- Same data as grid, one row per book, percent right-aligned.
- The view choice is remembered per app, not per screen instance.

### Importing

- A single `ImportRow` at the top of the grid: `Importing 2 of 5`.
- Failures never interrupt; when the batch ends, a summary sheet lists file name and reason.
- The library is usable during an import.

### Search

- Field pinned at the top, square, 1 dp Rule, cursor in ink, placeholder `Title, author, series, tag`.
- Results are list rows with the matched fragment in UI Strong.
- Groups in order: Books, Series, Shelves, Tags.
- `No matches.` is a single line, no illustration, no suggestions.

## Book detail

- Cover 132 × 188 with a 1 dp Rule; title in Headline, author in UI, series and `EPUB · 2.4 MB` in Micro.
- Progress block: percent in Display, chapter under it, `about 3 h left` in Micro when an estimate exists.
- Two actions side by side: filled `Read` / `Resume`, outlined `Read aloud`.
- Rows: `Add to shelf`, `Tags`, `Bookmarks` (count), `Mark finished` / `Mark unread`.
- Destructive `Delete` sits alone at the bottom, separated by 24 dp and a rule, never in a row list.
- Finished books swap the primary action to `Read again` and show no percent.

## Reader

### Page

- Text column only. No header, no footer, no page number, no chapter name — the chrome carries all of it, and position is always a percent.
- Paragraph spacing is the book's, first-line indent is the book's, unless "Use my typography" is on.
- A page never ends with a single orphan word; the last line is pushed to the next page.
- Images stay inside the column, 1 dp Rule frame, caption in Micro.

### Chrome

- Top bar: back, book title (one line, ellipsized), chapter in Micro below, then `Bookmark`, `Contents`, `More`.
- Bottom bar: percent, chapter name, then `Read aloud`, `Focus`, `Aa`.
- Bars are PaperSunk over the paper with a 1 dp Rule on their inner edge, no shadow, no blur.
- They animate in with a 160 ms fade and 8 dp slide, and leave on the first swipe.

### Play bar

- Pinned above the bottom bar whenever audio is active, including while the bottom bar is hidden.
- Shows the spoken sentence, ellipsized to one line, the speed as `1.4×`, and play/pause.
- Tapping the sentence opens the reader at that sentence without stopping playback.
- When the bottom bar is hidden, the play bar sits on the navigation inset, 48 dp tall.

### Display sheet

- 60% height, so the page above stays visible and updates live as controls move.
- Order: theme swatches, `Use my typography`, font, size, line height, margins, alignment, hyphenation,
  `Bionic reading`, `Keep screen on`.
- Every change is visible immediately behind the sheet; nothing needs a confirm.

### Voice sheet

- Current voice as a selected `VoiceRow` at the top, with a `Sample` text action.
- Speed: `SpeedStepper` with the value, plus a preset row `0.75 1 1.25 1.5 2`.
- Pitch appears only when the engine supports it; when absent there is no empty row and no explanation.
- `Download voices` row at the bottom, showing total space used.

### Voice downloads

- Grouped by language, each row a `VoiceRow` with size and state.
- Downloading rows show `Downloading… 37%` and a cancel action; no numeric ring, no progress bar.
- Failed rows show `Failed` in Error and a `Retry` action.
- Bottom of screen: `1.2 GB used by voices` and a `Delete all` text action with confirmation.

## Selection and dictionary

- Long-press selects a word with standard handles; selection tint is grey, never Highlight.
- `SelectionBar` floats above the selection: `Define`, `Translate`, `Copy`, `Read from here`.
  Actions that have no handler on the device are absent, not disabled.
- Dictionary sheet: headword in Headline, part of speech in Micro, definition paragraphs in UI,
  source in Micro at the bottom, `Open in dictionary` text action last.
- `No definition found.` is one line, with `Translate` still available.

## Focus mode

- **Focus keeps its own token set per theme.** It reads as the same room as the page, but each theme
  sets these values independently, so a theme can strengthen contrast without touching its reading
  tokens:
  - Ground: one step deeper than Paper — PaperSunk in light and sepia, near-black in night and black.
  - Type: full-strength Ink, never InkMuted, for the current sentence.
  - Highlight: stronger than the page's, 34% in light and sepia, 40% in night and black.
  - Context (peek lines, the chapter hairline): 55% ink.
- No chrome, no bars, no page edges. A hairline row at the top carries the chapter in Micro at 55%
  ink and an `Exit` glyph with a 48 dp target.
- Sentence frame: 364 × 132 dp fixed, sentence set at 32/44, centred, maximum three lines.
- Word frame: 364 × 88 dp fixed, one word at 40/48, centred, anchor never moves.
- Spoken word: the focus Highlight tint with full-strength ink type; the rest of the sentence at InkMuted.
- When silent, advance is shown only by the highlight moving — no spinner, no countdown.
- `FocusPeek` overlays the previous and next sentences at InkMuted while the finger is down.
- Finished frame replaces the sentence with `Finished` in Headline and two text actions: `Back to book`,
  `Mark finished`. Same panel in both shapes.

## Stats

- Three `StatBlock`s across the top: `2 h 14 m` today, `12` day streak, `7` books finished.
- Seven-day strip: seven 24 dp columns, filled ink for minutes, Micro labels under, no axis.
- Per-book list: cover 40 × 56, title, time in UI, proportional 2 dp bar in Rule/Ink.
- Empty state: `No reading yet.` and one line explaining that listening counts too.
- No comparison to friends, no goals, no notifications.

## Settings

- Groups, each a section label then rows: `Reading`, `Read aloud`, `Focus`, `Library`, `Data & storage`, `About`.
- Rows carry their current value on the right in InkMuted; destructive actions are never in a row list.
- `Data & storage` shows per-book size, voice model size and total, each with a delete action.
- `About` carries version and voice-model attribution; nothing else.

## Dialogs

- Paper ground, square, 280 wide, 1 dp Rule border, title in Title, body in UI, two text actions.
- Delete confirmations name the book and say what goes: `Delete "The Left Hand of Darkness"? Progress and bookmarks go with it.`
- Import conflict: `Newer position found` with `This device` / `Imported` and `Apply to all`.
- Import summary: one row per failure, file name truncated in UI, reason in Micro.
- Clear stats: `Clear reading history?` — one action, irreversible, no undo offered after the fact.

## Copy

Rules:

- Sentence case everywhere except the tracked caps labels, which are never more than three words.
- No exclamation marks, no "Oops", no "Whoops", no emoji, no "Let's".
- An error states what happened and what the reader can do, in that order.
- Percentages are whole numbers. Durations read `42 m`, `2 h 14 m`. Counts read `1 book`, `3 books`.
- The book is always "the book", never "the title" or "the file".
- Listening and reading are never named differently in copy: it is all `reading`.

Literal strings:

- `Import books` · `Import a folder` · `Books stay on this device.`
- `No books yet` · `Nothing here yet.`
- `Read` · `Resume` · `Read again` · `Read aloud` · `Continue`
- `This book is protected and can't be opened.`
- `No voice for this language.` · `Install a voice to hear books read aloud.` · `Voice stopped.`
- `Download voices` · `Downloading…` · `Failed` · `Retry`
- `Focus` · `Exit focus`
- `Finished` · `Mark finished` · `Back to book`
- `No reading yet.`
- `Delete "…"? Progress and bookmarks go with it.`
- `Newer position found` · `This device` · `Imported` · `Apply to all`

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

Reduced motion: page turns become instant, highlight becomes instant, focus crossfade becomes instant.
Sheets still slide, because a sheet that appears with no transition reads as a glitch.

## Accessibility

- Every target is at least 48 dp; the reader's margin band is 32 dp because the action it triggers is
  reversible, and it never overlaps the text column.
- Body text meets 4.5:1 in all four reading themes; Highlight tints keep ink readable and are never
  the only cue — the optional underline exists for that.
- TalkBack order in the reader: chrome, text, play bar. Reading text is exposed per paragraph, not per
  word, so navigation stays usable.
- Dynamic type scales all UI chrome; the reading surface scales independently through its own control.
- Focus mode scales with both, then shortens sentences at clause boundaries instead of shrinking type.
- The play bar announces state changes: `Playing`, `Paused`, `Voice stopped`.
