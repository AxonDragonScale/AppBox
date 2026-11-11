---
target: the designs in wallmatic.fig
total_score: 20
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 5
target_identity: "file:/Users/ronak/Projects/AppBox/apps/wallmatic/wallmatic.fig"
target_fingerprint: "sha256:cfb6a53e493b83ee725ef0e6ac7566f60f6ba4d6cabebfe31ae04922dacf734d"
target_path: /Users/ronak/Projects/AppBox/apps/wallmatic/wallmatic.fig
timestamp: 2026-11-11T16-59-22Z
slug: apps-wallmatic-wallmatic-fig
---
## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Home answers current/next/interval, but Library never marks which album is active, and the active tab state is a 1 px weight difference |
| 2 | Match System / Real World | 2 | "404" placeholder, "24 sheets" vs "24 wallpapers", "Dark Mode" with a gear icon, "Choose Name" |
| 3 | User Control and Freedom | 2 | Delete has no confirmation or undo; Select Album has no explicit dismiss; fullscreen wallpaper has no visible exit |
| 4 | Consistency and Standards | 2 | Two album-name dialogs, three toggle/active-segment languages, 24 vs 69 vs 3 wallpaper counts, 10–16 built outside the component library |
| 5 | Error Prevention | 1 | No duplicate-name guard, no disabled SAVE on empty input, no validation, destructive delete one tap away |
| 6 | Recognition Rather Than Recall | 2 | Album actions hidden behind long-press, unexplained 01–10 badges, unlabeled "CHANGE" vs "CHANGE WALLPAPER" |
| 7 | Flexibility and Efficiency | 2 | Interval picker is a good accelerator; no bulk add/blacklist, no tile surface, no shortcuts |
| 8 | Aesthetic and Minimalist Design | 3 | Strong type and color, but two large dead zones on Home and a 404 box as the largest element on empty screens |
| 9 | Error Recovery | 1 | No scan failure, missing permission, or broken-album state is designed |
| 10 | Help and Documentation | 2 | Empty states teach the model well; no help for mirror consequences, and no guidance when something fails |
| **Total** | | **20/40** | **Acceptable (50%)** |

## Design Specificity Verdict

**LLM assessment**: Authored, not category-interchangeable. Paper/ink/indigo, Chakra Petch labels, 1 px rules and a phone-aspect preview are a specific world; this could not be a different wallpaper app unchanged. The composition is honest: one image, one album, two times, one interval. Where it goes generic is at the edges — an M3 circular FAB with elevation, stock bottom sheets, and a decorative 404 — and where it goes under-designed is the library's state model. The visual language is stronger than the product model it is expressing.

**Deterministic scan**: not applicable (native platform). Supporting design-file evidence: OpenPencil lint on the Screens page reports 4 contrast errors (1.83:1, all the "404" placeholder) and 95 warnings, almost all 10–11 px text below its 12 px floor; 160 of 286 text nodes are 10 px. There are no variables in the document and no bound colors — 10 hardcoded hexes across the page.

**Visual overlays**: not applicable; no browser surface.

## Overall Impression

This is the best version of Wallmatic so far: the Home screen genuinely answers "what is showing, and when does it change?" in one glance, and the paper/ink system has a point of view. It reads as a designed product, not a template. The gap is between what the design promises and what it has actually resolved: the library has no active state, counts disagree across screens, the same album is named two ways, and the screens added last were hand-built instead of assembled from the library. A thoughtful user would trust the first screen and start doubting the third.

## What's Working

- **The Home glance.** Preview at the phone's own aspect beside ALBUM, CURRENT and NEXT is a product-specific composition. It makes the schedule the subject and the chrome recede.
- **The typographic voice.** Chakra Petch eyebrows and numerals against Roboto body, all at one weight rhythm, gives the app an identity that survives Material components.
- **The interval picker.** Seven labelled stops plus CUSTOM replaces the chip row with something calmer and faster; it is the right signature interaction.

## Priority Issues

- **[P1] The same thing is counted and named differently on almost every screen.** Test is "1 folder · 24 wallpapers" on Home and Library, "24 sheets" in Select Album, and "69 wallpapers" inside the album; Nature is 24 in the library but 3 wallpapers with 5 tiles in the ready state. "Sheets" and "wallpapers" are used interchangeably. **Why it matters**: this is a reliability product; if the counts cannot be trusted on the design board, the user will not trust the schedule either. **Fix**: one noun, "wallpapers"; one count rule ("folders · wallpapers") reused verbatim in cards, sheets and headers; correct the specimens so every screen agrees. **Suggested command**: `$impeccable clarify`.

- **[P1] Creating an album has two different dialogs.** Library's FAB yields "Choose Name / SAVE" with a prefilled "Nature"; the empty state yields "Name your album / CREATE" with helper text. **Why it matters**: the same action visibly behaves differently depending on where it was started, and the prefilled name duplicates an existing album with no guard. **Fix**: one AlbumNameDialog: title "New album", empty field with a real placeholder, duplicate-name validation, disabled CTA until valid, helper text every time. **Suggested command**: `$impeccable harden`.

- **[P1] The library cannot tell you what is active, or how to act on an album.** Cards show a wallpaper count and an unexplained 07–10 badge, nothing else; Rename and Delete exist only behind a long-press, and Delete fires with no confirmation or undo. **Why it matters**: "which album is on Home and Lock right now" is the second question the product exists to answer, and discovery of management is pure recall. **Fix**: an ACTIVE HOME / ACTIVE LOCK marker on the card and on the album header, a visible overflow affordance per card, and a confirmation or undo for delete. **Suggested command**: `$impeccable shape` then `$impeccable harden`.

- **[P1] The design covers only the light, default-scale happy path.** There is no night edition, no font-scale state, and no failure state — no folder-scan error, missing permission, unreadable image, or "update did not run". 160 text nodes sit at 10 px and the key text links (CHANGE is 52 × 13 px) are far below the 48 dp minimum. **Why it matters**: PRODUCT calls dark theme first-class and reliability visible; the shipped design cannot absorb either, and the schedule failing silently is the one thing this app must never do. **Fix**: dark screens for Home, Library and Settings; a font-scale 1.3 pass; minimum 12 sp for secondary labels; 48 dp containers under CHANGE, CUSTOM and dialog actions; one error state for background failure and one for folder access. **Suggested command**: `$impeccable harden` then `$impeccable typeset`.

- **[P1] The schedule controls compete instead of composing.** "CHANGE" beside ALBUM and the filled "CHANGE WALLPAPER" both sound like verbs for the same result, Lock has no CHANGE at all, and the picker's current value is stamped top-left while the marker sits elsewhere on the track. **Why it matters**: the user cannot predict what the primary button does, and cannot change the Lock album from the screen that shows it. **Fix**: name the actions by object ("Change album", "Next wallpaper"), give both targets the same CHANGE affordance, and label the active tick in place or move the value beside the marker. **Suggested command**: `$impeccable clarify`.

## Persona Red Flags

**Casey (distracted mobile user)**: CHANGE, CUSTOM, CANCEL and SET are 13 px-high text lines at the screen edges, well outside a comfortable thumb target. Lock requires switching to the Home/Lock tabs and back because its album has no CHANGE. Album management starts with a long-press, which she will never discover in a 20-second check.

**Jordan (first-timer)**: "404" is the largest text on the empty Home screen and means nothing. The 01–10 badges look like counts but count nothing; "24 sheets" becomes "24 wallpapers" two screens later. "CHANGE WALLPAPER" is ambiguous enough that he will avoid it, which is the wrong instinct in a one-button app.

**Sam (accessibility-dependent)**: 160 labels at 10 px fail on a 412 dp phone before font scaling even begins, and 1.3× will clip fixed 400 px preview columns and the 40 dp switcher. The 404 placeholder is 1.83:1. Active state is carried by border weight and bold only, with no icon or colour backup, and there is no designed focus order or content description for the numbered badges.

## Minor Observations

- The masthead repeats the active tab (HOME / LIBRARY / SETTINGS) on every screen; it adds no location value the tab bar does not already give.
- The tab-bar active indicator is a 3 px top rule sitting directly on the bar's own 1 px border — at a glance it is a table row, not a selected tab. The Home/Lock switcher in the screens renders as a full 2 px box outline, while the Components board records option B (bottom underline). Pick one and make the screens match the board.
- The Library FAB is a shadowed circle in a system that refuses rounding and shadows, and it overlaps the fourth album card.
- Fullscreen wallpaper has no chrome at all: no back, no index, no set/blacklist action. It is the one screen where deletion and blacklisting would naturally live.
- The header copy says "Twelve 412 × 915 screens" but the file holds sixteen, and claims all widgets are library instances; screens 12–16 contain no instances.
- The album screen nests "Wallpapers" inside "Test", then opens a screen also titled "Wallpapers" — the folder and the folder's contents share a name.
- "Github" should be "GitHub"; the developer row is the only all-caps value.
- The custom interval's leading zero ("01") disagrees with its own summary ("1 H 30 MIN").
- There is no design for the quick-settings tile, the SAF folder permission, or adding wallpapers as images rather than a folder — all shipped product surfaces.
- The Details screen's step list (01 Create, 02 Add, 03 Set active) is not tappable but looks tappable, and it duplicates the CREATE ALBUM button below it.

## Questions to Consider

- If the Library could answer "what is active" in one badge, would the Home masthead still need the screen name?
- What would the empty state look like if the large frame held the product's mark or a ruled placeholder instead of an error code?
- Should the interval picker's value live at the marker instead of the top-left corner?
- Is "CHANGE WALLPAPER" a command (advance now) or navigation (open the album sheet)? Pick one and the other control disappears.

## Run Notes

- Target: `apps/wallmatic/wallmatic.fig` (slug `apps-wallmatic-wallmatic-fig`); 16 screens, 3 pages, 1644 nodes.
- Ignore list: none present.
- Independence: single-context design review, which is the complete method on a native platform; detector and browser overlay do not apply. No degraded banner.
- Design-file lint: OpenPencil lint, 4 errors (contrast 1.83:1), 95 warnings (10–11 px text), 457 info; 0 fixable. No variables exist in the document.
- Browser visibility, overlay injection, live server: not applicable. No temp server started.
- Images exported through OpenPencil MCP into `/Users/ronak/.cache/wallmatic-critique/` and reviewed at 2× and 4×.
