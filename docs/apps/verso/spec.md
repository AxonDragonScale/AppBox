---
title: "Verso — reader app spec"
status: in-progress
summary: "Behavioural spec for Verso: a local-first EPUB/FB2/TXT reader built around read-aloud, low-eye-movement focus mode, and bionic reading."
read_when: "Designing or building Verso, or deciding what a feature does — and what it refuses to do."
---

> Context: [Main](../../context.md) > Verso

# Verso — Reader App Spec

Behaviour only. No modules, libraries or architecture — those land in `context.md` once code exists.
The visual contract is [the UI doc](./ui.md).

## Premise

- A local-first reading app for long-form text, built to be listened to as much as looked at.
- Three things it is organised around:
  - Read-aloud as the primary way to consume a book, not an accessory bolted onto a reader.
  - Focus mode, which removes eye movement entirely.
  - Bionic reading as a first-class display toggle.
- Everything works offline. No account, no store, no server, no telemetry.
- Phone-first: good on tablets and foldables, without bespoke large-screen layouts.

Not in v1 (deliberate, not forgotten):

- PDF, MOBI/AZW3, CBZ/CBR, comics and any fixed-layout content.
- DRM-protected books — refused with a clear reason, never half-supported.
- Highlights and free-text notes.
- Cloud sync, accounts, OPDS, online metadata or cover lookup.
- Audiobook files, and aligning an existing audio recording to a book's text.

## App shell

- Three destinations: Library, Stats, Settings.
- The reader is full-screen, outside the nav, and hides all system bars while reading.
- Launch lands on the Library with one prominent continue card:
  - Shows the last book, its chapter and percent.
- Back from the reader returns to the Library with its scroll position, filters and view mode intact.
- Back from a nav destination exits the app; no confirmation dialogs for navigation.

## Library

### Getting books in

- Import sources:
  - System file picker — one or many EPUB, FB2, TXT, Markdown, HTML files.
  - Share sheet from another app ("Open with Verso").
  - Folder import: pick a directory; every supported file inside is imported, subfolders included.
- Import copies the file into app storage.
  - The original is never modified, moved or deleted.
  - Later changes to the original have no effect on the library.
- Duplicates are detected by content, not filename: the second copy is refused as "Already in library".
- Imports are queued and cancellable; one bad file never blocks the rest of the batch.
- Failures are reported per file, in plain language:
  - Unsupported or corrupt file.
  - Protected or password-locked — "This book is protected and can't be opened."
  - Empty document.
- Cover and metadata come from the file only; nothing is fetched.

### Metadata

- Read from the file: title, author(s), series and index, language, publisher, date, description.
- Missing title → cleaned filename. Missing author → "Unknown author".
- Missing cover → generated placeholder from the title's initials, with a stable colour per book.
- Nothing is guessed, "cleaned up" or merged with other sources.

### Browsing

- Two views: cover grid and detail list (cover, title, author, progress).
  - The choice is remembered.
- Sorts: recently read, recently added, title, author, series, progress, length.
- Filters: all / unread / reading / finished, plus shelf, tag, author, series, language, format.
- Library search matches title, author, series, shelf and tag — not book contents.
- Empty state: one import action and one line explaining formats, nothing else.

### Shelves, series, tags

- Shelves are manual collections; a book can sit on many.
- Tags are flat free-form labels; both shelves and tags appear as filters.
- Series group automatically from metadata, ordered by series index; a book with no index sorts last.
- Smart shelves need no curation: Unread, Reading, Finished.
- Removing a book from a shelf, or deleting a shelf, never deletes a book.
- Deleting a book removes its shelf and tag memberships.

### Book detail

- Cover, title, author, series, language, format, size, progress, bookmarks count, time spent.
- Actions:
  - Read / resume.
  - Read aloud from the last position.
  - Add to shelf, edit tags.
  - Mark finished / mark unread.
  - Delete with confirmation.
- Per-book overrides: layout (paged or scroll), font size, theme. Everything else is global.
- Delete removes the file, cover, progress, bookmarks and memberships.
  - Time already recorded stays in the daily stats; the per-book row disappears.

## Reading surface

### Layout modes

- Paged (default) and continuous scroll, switchable per book and from the reading settings.
- Paged: a single column, page turn on horizontal swipe, and volume buttons can turn pages when enabled; progress is percent only, never page numbers.
- Scroll: a single column, position preserved when switching modes by mapping to the nearest sentence.
- Theme and typography changes reflow live, keeping the current sentence in place — never jumping the reader to a different part of the page.

### Gestures — the contract

Page turning is a swipe, so taps are free for text.

- Swipe horizontally: next/previous page (paged); next/previous chapter (scroll).
- Tap a word: start read-aloud from that word's sentence.
  - While playing, the same tap relocates playback.
  - With read-aloud unavailable, taps do nothing and no repeated toasts appear.
- Tap the margin band — the gutters either side of the text column, or the top/bottom edge bands: show or hide the chrome.
- Long-press a word: select it and open the selection toolbar (define, translate, copy, read from here).
- Drag after a long-press: extend the selection by word.
- Pinch: adjust font size, persisted as that book's override.
- Volume buttons: previous/next page when enabled in reading settings; off by default so the buttons keep meaning volume.
- Read-from-here has two paths on purpose: a single tap on a word, or the selection toolbar after a long-press.
- No gesture is hidden behind a modifier, and no gesture both turns a page and touches text.

### Position and progress

- Progress is tracked per book to the sentence and restored exactly, including after a process kill or reboot.
- Percent is the only position number. Page numbers are never shown, because they change whenever the type does.
- Any number of books can be in progress; the continue card reflects the most recent.
- Position survives rotation, mode switch, theme change, font change, and read-aloud interruption.

### Chrome

- Hidden while reading; a margin tap brings it up, and so does a swipe — down in paged mode, up from the bottom edge band in scroll mode, where a downward swipe has to mean scroll.
- Top bar: back, title and chapter, bookmark, table of contents, overflow.
- Bottom bar: progress slider with chapter ticks, chapter name, play/pause, focus mode, display settings.
- Auto-hides after a few seconds of inactivity, and immediately on the first swipe.
- While read-aloud is running, a compact play bar stays pinned above the bottom bar showing the spoken sentence and speed.

### Navigation

- Table of contents nested by heading level, current chapter marked, tap to jump.
- Progress slider shows the chapter under the thumb while dragging; releasing commits.
- Chapter next/previous available from the chrome.
- Footnote and endnote links open a bottom sheet with the note text; closing returns to the exact sentence that held the marker.
- External links ask once, then open in the browser.
- Reaching the last page of the book shows an end-of-book panel with "Mark finished" and "Back to library".

### Content handling

- Images fit the text column; tapping one opens it full-screen with zoom.
- Image alt text is exposed to screen readers, and can optionally be spoken during read-aloud (default off).
- Tables scroll horizontally when they don't fit; they never break the column.
- Unsupported or malformed constructs degrade to plain text rather than blocking the chapter.
- Empty chapter: "This chapter has no text" with a next-chapter action.
- RTL books read right-to-left: page-turn direction and gutter side follow the language.
- CJK text uses its own line-breaking rules and no hyphenation.
- Book language comes from metadata and is overridable; it drives hyphenation, the default voice and the dictionary pair.

### Typography and theme

- Themes: light, sepia, dark, black, plus custom background, text and accent colours.
- Controls: font family (bundled set and system fonts), size, line height, margins, alignment (ragged or justified), hyphenation, letter spacing, word spacing, paragraph spacing.
- Presets: Compact, Comfortable, Large print — one tap, then editable.
- Publisher styling rule:
  - Theme colours always win over the book's colours.
  - The book's fonts, margins and alignment apply where it specifies them, unless "Use my typography" is on, which overrides the book's layout entirely.
- Default is publisher fonts with app colours, which is what most books expect.
- Keep screen awake while reading, on by default.

### Windows and rotation

- Rotation and window resize reflow in place without losing position.
- Text column width is capped for comfortable line length, centred on wide screens.
- Landscape is the same single column, not a spread.
- Fold and unfold behave as a resize; nothing reloads.
- Phone portrait is the tuned case; larger screens get more columns in the library grid.

## Read aloud

### Presence and entry points

- Entry points: tapping a word, the play control in the chrome, "Read aloud" on the book detail, and the notification or lock screen.
- Playback is a real session, not a screen feature: it continues with the screen off and the app in the background.
- One session at a time; starting another book stops the first.

### Controls

- Play/pause, previous/next sentence, previous/next chapter.
- Speed: any value from 0.5× to 3.0× on a continuous slider, with 0.75, 1, 1.25, 1.5 and 2 as one-tap shortcuts.
- Voice picker with a sample line, filtered to the book's language, showing engine and quality where the engine reports it.
- Pitch slider, shown only when the selected engine supports pitch.
- Settings are global in v1 — one voice, one speed, one pitch for every book.
- Changing voice mid-playback continues from the current sentence, not the current word.

### Highlighting

- The spoken word is highlighted in the page; the current sentence carries a faint tint so the eye can find the line.
- When an engine speaks a phrase as a unit, the phrase highlights as a unit.
- Highlight tracks the audio word by word:
  - Exact when the engine reports word boundaries.
  - Proportional through the sentence when it doesn't — visibly smooth and never a whole sentence behind.
- No speech recognition is involved: the voice's own position is the only source of truth for the highlight in v1.
- Paged mode turns the page at a sentence boundary, ahead of the voice, so a sentence is never split across a page turn.
- Scroll mode keeps the spoken line inside the middle third of the screen.
- Highlighting stops when the app goes to the background and resumes at the spoken word on return.
- Highlight colour and strength are configurable; an underline cue can be added for colour-blind readers.

### Tap to read

- A tap starts at the beginning of the tapped word's sentence — never mid-word, never mid-sentence.
- A tap while playing moves playback there immediately, with the highlight following.
- Starting from a chapter's first word does not replay the previous chapter.

### Background session

- Notification: book title, chapter, play/pause, previous/next sentence, stop, and a progress bar.
- Lock screen and media controls mirror the notification.
- Notification actions work without opening the app.
- Closing the notification stops the session; the position is kept.

### Interruptions

- Auto-pause on interruption, a setting per cause, both on by default:
  - Calls, alarms and any other audio-focus loss.
  - Headphones unplugged or Bluetooth disconnected.
- Auto-resume after an interruption is its own setting, off by default.
  - When on, it waits for the interruption to actually end and never speaks over another app.

### Chapter and book end

- Default: playback continues into the next chapter, turning the page itself.
- A setting can make playback stop at chapter ends.
- At the end of the book, playback stops, the end-of-book panel appears, and the next book is never started automatically.
- Interruptions arriving during a chapter transition pause without double-advancing.

### What gets spoken

- The book in reading order, including front matter when started from the beginning.
- Not spoken: page numbers, running heads, footnote markers, link and UI labels.
- Image descriptions only when the setting is on.
- Sentence boundaries follow the text's punctuation and never break inside common abbreviations.
- No number or abbreviation rewriting: what the book says is what the voice says.

### Failures and voice availability

- No voice engine installed: read-aloud controls are disabled with one line of explanation and a hint to install one; word taps do nothing and the message never repeats.
- Engine dies mid-sentence: playback pauses with "Voice stopped" and a retry action.
- Selected voice disappears: fall back to the system default for that language with a notice.
- No voice for the book's language: the play entry stays visible but explains what's missing, with download options; reading is unaffected.

### Downloadable neural voices

- Optional, per language, downloaded on demand.
- Each voice shows size, quality tier and whether it works offline — all bundled voices work offline after download.
- Downloads can be paused, resumed and cancelled; no partially downloaded voice is ever selectable.
- Existing system voices stay usable during a download and while offline.
- Deleting a voice frees its space immediately and reverts to a system voice if it was selected.
- Downloading is the only thing in the app that uses the network.

## Focus mode

### Purpose

- Read without moving the eyes: one unit of text, always in the same place.

### Shapes

- Sentence frame (default): one sentence at a time, large type, centred in a fixed frame.
  - Longer sentences wrap to at most three lines and are never cut mid-word.
  - The frame never resizes between sentences, so nothing shifts.
- Word mode: one word at a time at a fixed anchor.
  - Punctuation pauses are respected so the rhythm isn't flat.
- Shape is chosen when entering and remembered.

### Behaviour

- Works with or without audio.
  - Audio on: advance is driven by speech, and the spoken word is highlighted inside the frame.
  - Audio off: advance is timed by a silent reading speed in words per minute, with a longer beat at punctuation.
- Entering focus mode starts at the sentence currently on screen.
- Exiting returns to the normal page at the current sentence.
- Chapter and book end behave exactly as in read-aloud, including the finished panel.

### Interaction

- Tap centre: pause or resume, never a new position.
- Tap left third: previous sentence. Tap right third: next sentence.
- Vertical swipe: change speed — audio speed when listening, words per minute when silent.
- Long-press: peek at the current sentence with the previous and next around it; release returns to the frame.
- Back exits to the reading surface.
- Images, tables and footnote markers are skipped with a small marker ("1 image"); leaving focus mode shows them.

### Presentation

- Large type on the reading theme's own ground, high contrast by default.
- Bionic reading is off here.
- Reduced motion is respected: no fades, no sliding between sentences.

## Bionic reading

- A single global toggle, with no per-book override: on or off. One fixed, tuned strength — roughly the leading half of each word emphasised.
- Applies live to body text, headings and captions in both paged and scroll modes.
- The emphasis is weight, not colour: the word still reads as one word.
- Never changes the text itself: copying, dictionary lookups and speech all use the plain word.
- Coexists with the read-aloud highlight and with selection without visual conflict.
- Not applied in focus mode, not applied to UI labels.
- Off by default; enabling it explains itself in one line the first time.

## Bookmarks

- One tap in the chrome bookmarks the current sentence.
- Bookmark list per book: snippet, chapter, percent, date; jump or delete.
- Bookmarking the same sentence twice merges instead of duplicating.
- Bookmarks survive theme, typography, layout and mode changes, and read-aloud.
- Bookmarks are removed with the book.

## Dictionary and translation

- A long-press selects a word and offers: Define, Translate, Copy, Read from here.
- Define uses the best dictionary available on the device; if none exists the action is hidden rather than broken.
- Translate hands a word or a dragged phrase to a translation provider on the device, showing the result in a sheet where the provider supports it.
- Language pair defaults to book language → device language and is overridable per lookup.
- The app itself never makes a network request; anything online belongs to the other app and leaves Verso.
- "Read from here" in the toolbar is the same action as tapping the word.

## Stats

- One metric per book: time spent with it. Listening and looking both count.
- Shown: minutes per day, current streak, longest streak, per-book time, books finished.
- Time accrues only while the reader is open in the foreground or audio is playing.
  - The reader open and untouched for five minutes stops counting until the next interaction.
  - Paused audio does not count.
- Pace appears once there's enough data: minutes per 1,000 words, with a labelled "time left in chapter/book" estimate on the progress bar.
- Book detail shows the share of that book's time that was listening.
- Finishing a book records a finish event; un-finishing removes it.
- Deleting a book removes its per-book row but leaves daily totals and streaks alone.
- Stats never leave the device and can be cleared from Settings with a single confirmation.

## Settings

- Reading: theme and colours, typography and presets, layout mode, volume-button page turns, bionic reading, keep screen awake.
- Read aloud: engine, voice, speed, pitch, read image descriptions, stop at chapter ends, auto-pause per interruption cause, auto-resume, voice downloads.
- Focus mode: shape, silent reading speed.
- Library: default view, default sort.
- Stats: clear.
- Data: export, import, storage usage, clear cache.
- About: version and attribution for any downloaded voice model.

## Data, export and privacy

- Everything is local. There is no account, no sync and no server.
- Export writes one archive containing:
  - Library index (metadata and identity of each book).
  - Reading positions and bookmarks.
  - Shelves and tags.
  - Stats and settings.
  - Book files themselves, only if the user ticks that option.
- Import merges rather than replaces:
  - Books are matched by content; a book missing from the archive is never deleted.
  - The newer position wins; a genuine conflict asks once per book, with "apply to all".
- The data screen states once, plainly, that an uninstall loses whatever was not exported.
- Storage view: size per book, per voice model, and total; deleting either frees space immediately.

## Edge cases worth pinning

- A book deleted while open closes the reader to the library with a note instead of an error.
- An import during reading never interrupts; the new book appears when done.
- A very large book shows its first page immediately; contents may take a moment with visible progress, never a blank screen.
- No cover, no table of contents, no chapter headings: contents shows a single entry, or says so.
- A position near the end plus a font change clamps inside the book; the reader never lands on a blank page.
- A page turn on the last page opens the end-of-book panel rather than doing nothing.
- Rotation during playback never interrupts audio; the highlight re-renders at the spoken word.
- A word the engine skips never leaves the highlight stuck; it advances.
- With a screen reader active, tap-to-read yields to the screen reader; playback is started from the chrome and highlighting still shows.
- Switching from scroll to paged mid-sentence lands with that sentence fully visible.

## Accessibility

- Every control is labelled; play/pause and progress changes are announced.
- Large text settings still leave the reading surface usable; type scales without clipping the chrome.
- Every theme meets AA contrast for body text.
- Reduced motion removes page-turn animation and highlight fades.
- The read-aloud highlight adds a non-colour cue when asked, and never conveys state by colour alone.
- All core actions are reachable one-handed on a phone in portrait.

## Deferred past v1

- PDF, CBZ/CBR, MOBI/AZW3 conversion.
- Highlights and notes.
- Cloud sync and accounts.
- Sleep timer, auto-advance to the next book, headset and Bluetooth buttons, per-book voice settings.
- Two-page tablet and landscape spreads.
- Full-text search across the whole library.
- Custom font import.
- Per-span language voices within a mixed-language book.
- Word-level timing for engines that don't report it, including recovering it by listening to the voice itself.
- Aligning a purchased audiobook to a book's text.
