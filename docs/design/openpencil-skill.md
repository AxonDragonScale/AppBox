---
name: openpencil-skill
description: Build and edit .fig design files with the OpenPencil MCP tools in Zed — JSX rendering, design tokens and variables, components and variants, multi-page files, screens, overlays, bulk colour binding, and verification. Use whenever a task involves creating, changing, or inspecting an app design in apps/*/*.fig through OpenPencil. Zed is the only host that writes; other hosts are read-only.
---

# OpenPencil MCP — working skill

How to drive the OpenPencil MCP tools without rediscovering the same traps. Every rule
below was learned the hard way while building `apps/devinator/devinator.fig`.

**Read this before your first `render` call.** Then follow the
[update protocol](#update-protocol-required) at the end — this file is expected to grow.

## Who edits what

- **Zed + the OpenPencil MCP tools — the only editing path.** Every mutation (render, move,
  restyle, rename, save) happens here. Use the tools directly; do not shell out to a bridge.
- **Delta — read-only.** Read and export freely (`get_page_tree`, `query_nodes`, `get_jsx`,
  `export_image`); never attempt a mutation.
- **`openpencil` CLI — inspect, lint, export.** Headless runs read, diff and write copies with
  `-o`. They never edit the open document.

`.fig` is a binary format: never edit it by hand, from a terminal, or with a script. A host that
cannot reach the MCP tools inspects and reports — the human or a Zed agent makes the change.

## Golden rules

1. **Pass `page_id` to every call.** `switch_page` reports success but does **not** change
   the page that `render`, `lint_fix`, `arrange` or `export_image` act on. Without an
   explicit `page_id` you will silently write into the wrong page.
2. **`render` with JSX is the workhorse.** Don't build screens with dozens of
   `create_shape` calls. One `render` can create a whole card or screen.
3. **A frame with no `flex` and no `h` becomes 100 × 100.** This is the #1 cause of
   "mystery empty space" under text. Every wrapper frame gets `flex="col"` / `flex="row"`
   or an explicit height.
4. **Text does not inherit fonts.** The default is Inter (not Android's Roboto). Set
   `font="Roboto"` (or `font="Roboto Mono"`) on **every** `<Text>`.
5. **Rows need `w="fill"`** before `justify` or `grow` does anything. A hugging row
   collapses its `grow` child to ~100 px and text wraps absurdly.
6. **Read the `warnings` array from `render`.** Unsupported props are silently ignored,
   then confirmed in the warnings. When unsure, `get_jsx` the node and copy the canonical
   attribute spelling.
7. **An RPC timeout is not a failure.** Heavy `render` / `arrange` calls regularly exceed
   the 20 s transport limit and still commit. See [Timeouts](#timeouts-and-stalls).
8. **Nothing is autosaved reliably.** Call `save_file` at every milestone. The `.fig` is
   binary — never touch it from a terminal.
9. **Tokens are one collection per theme** (there is no add-mode API), and `lint_fix`
   binds hardcoded colours to them in bulk — far cheaper than `bind_variable` per node.
10. **Screens are built from frames, not instances.** Instances cannot take text
    overrides through this API, and `create_instance` has no parent parameter.

## Tool map

| Job | Tools |
|---|---|
| Documents / pages | `list_documents`, `list_pages`, `get_current_page`, `create_page`, `switch_page`, `update_node` (rename a page) |
| Create content | `render` (JSX), `create_shape`, `create_vector`, `create_slice`, `import_svg`, `insert_icon`, `fetch_icons`, `search_icons` |
| Inspect | `get_page_tree`, `get_node`, `get_jsx`, `node_children`, `node_bounds`, `query_nodes`, `describe`, `get_selection` |
| Mutate one node | `node_move`, `node_resize`, `rename_node`, `set_fill`, `set_stroke`, `set_radius`, `set_opacity`, `set_effects`, `set_text`, `set_font`, `set_text_resize`, `set_layout`, `set_layout_child`, `set_minmax`, `set_visible`, `set_locked` |
| Mutate many nodes | `batch_update` (one layout recompute), `reparent_node`, `clone_node`, `delete_node`, `arrange` |
| Tokens | `create_collection`, `create_variable`, `set_variable`, `list_variables`, `design_to_tokens`, `design_to_tokens(format="dtcg")` |
| Components | `node_to_component`, `combine_as_variants`, `create_component`, `create_instance`, `create_slot`, `expose_instance_swap`, `set_behaviour` |
| Verify | `export_image`, `export_svg`, `export_pdf`, `lint`, `lint_fix`, `analyze_overlaps`, `analyze_spacing`, `analyze_typography`, `analyze_colors` |
| Persist | `save_file`, `close_file` (MCP — the only write path); headless `-o` writes a copy, never the open document |

## Front ends

| Front end | Edits the open document | Use for |
|---|---|---|
| **Zed + MCP tools** | **yes — the only editing path** | every mutation, plus verification and `save_file` |
| **Delta** | **no — read-only** | `get_page_tree`, `query_nodes`, `get_jsx`, `export_image` |
| `openpencil` CLI, headless (`<file>`) | no — `-o` writes a copy | bulk reads, exports, lint, `diff`, anything scriptable |
| `openpencil` CLI, app mode (no path) | avoid | liveness probe and `tool call get_font_status` |

- Headless commands are ~0.5 s, cannot hit the 20 s RPC timeout, and need no app:
  `openpencil info|pages|tree|node|find|query|fonts|variables|tokens|analyze|lint|export|diff <file>`,
  `openpencil tool call <name> <file> --args '<json>' [-o out.fig]`. `-o` writes anywhere,
  including the worktree — that is how a script produces a *new* file, never how the document
  being edited is changed.
- App mode is a unix-socket RPC with a hard 20 s timeout and no retry. It is useful for
  `get_font_status` and for a cheap read, not as a fallback editor.
- Parity: 121 of the 132 MCP tools are reachable through `openpencil tool call`; the 11 app-only
  ones (`activate_document`, `close_file`, `get_settings`, `list_documents`, `new_document`,
  `open_file`, `redo`, `save_file`, `undo`, `update_settings`, `get_codegen_prompt`) live behind
  `openpencil documents`, `settings`, `undo` / `redo` and `selection`.
- **Never judge text geometry headless** — see
  [Fonts](#fonts-must-resolve-or-the-design-lies).

## Fonts must resolve or the design lies

A design authored in `Roboto` / `Roboto Mono` is metric-fictional until those families are installed
on the machine: OpenPencil substitutes silently, and its headless host exposes **zero** fonts.

```sh
brew install --cask font-roboto font-roboto-mono   # then quit and reopen the app
```

- Faithful check (app mode, read-only, cheap): `openpencil tool call get_font_status` →
  `{"faithful": true, "faces": [{"family":"Roboto","status":"available","source":"local"}]}`.
  `list_available_fonts` lists what the app can actually use.
- Do **not** trust `openpencil fonts <file>` for the live document: it walks only the current page,
  ignores `--page`, reports `unresolved` without naming the substitute, and in app mode returns `{}`.
- Measured cost of a substitution (same string, stored vs headless): `Devinator` 32/700 138×38 →
  173×45; `Devinator` 22/600 95×26 → 119×31; `Developer options` 16/600 130×19 → 164×23;
  `Developer options & ADB` 12/400 133×14 → 166×17. A flat **+25 %**: unresolved families are laid out
  with a monospace fallback while the rasteriser draws plausible glyphs, so exports look right and
  every box is wrong.
- Consequence: export, lint, tokens, colour and structure analysis are safe headless; **wrapping,
  overflow and row/tile fit are not**. Author and judge them in the app.

## Documents and ids

```
list_documents                 → open tabs, file paths, page ids
list_pages / get_page_tree     → page → screen ids
query_nodes                    → anything deeper
```

- Node ids are per file **and per host**: the same frame has different ids through MCP, CLI app
  mode and headless. Derive ids from the host you are about to mutate — never hardcode or mix them.
- MCP arguments and `openpencil tool call` arguments are **not** the same schema. MCP takes
  `page_id`; headless `render` rejects it (`Invalid key: Expected never but received "page_id"`) and
  wants `parent_id=<page id>` instead.
- `switch_page` reports success but does not change which page a tool acts on; pass `page_id`
  explicitly (golden rule 1).

## JSX reference

```jsx
<Frame name="Card" flex="col" w={380} p={16} gap={8} bg="#171A1F" rounded={20}>
  <Frame name="Row" flex="row" w="fill" h={56} gap={12} justify="between" items="center">
    <Frame name="Badge" w={40} h={40} flex="col" justify="center" items="center" bg="#20242B" rounded={12}>
      <Icon name="material-symbols:usb" size={20} color="#E6E9EE" />
    </Frame>
    <Text font="Roboto" size={16} weight={600} color="#E6E9EE">USB debugging</Text>
  </Frame>
</Frame>
```

| Prop | Applies to | Values / notes |
|---|---|---|
| `flex` | Frame | `"row"` / `"col"` — sets auto-layout. Plain frames have absolute children |
| `w`, `h` | all | number, or `"fill"` (child of a layout frame). `w="fill"` compiles to `grow={1}` |
| `grow` | layout child | number; use in a row/col that has a resolved width |
| `gap` | Frame | item spacing |
| `p`, `px`, `py` | Frame | padding; `px`/`py` are supported |
| `justify` | Frame | `"start"` · `"center"` · `"end"` · `"between"` (round-trips as `"between"`) |
| `items` | Frame | `"start"` · `"center"` · `"end"` · `"stretch"` |
| `bg`, `rounded`, `opacity` | all | `#RRGGBB`, px, `0–1` |
| `stroke`, `strokeWidth` | all | |
| `wrap` | Frame | wrap children onto new lines |
| `font` | Text | family name. **Required for Roboto / Roboto Mono** |
| `size`, `weight`, `color`, `lineHeight` | Text | |
| `name` | all | layer name; use `Parent/Prop=Value` for variants |

**Ignored silently** (confirmed by `render` warnings): `layout`, `fontFamily` (use `font`),
`mt`/margins, `align` on text (centre it with the parent's `items`/`justify`), `clip`.

**Text sizing**
- no `w` → hug content, single line, grows to the right (`WIDTH_AND_HEIGHT`)
- with `w` → fixed width, wraps, height grows (`HEIGHT`)
- to fill a row: wrap it in a `Frame flex="col" grow={1}` instead of giving the text `grow`

**Icons** — call `fetch_icons` once, then `<Icon name="material-symbols:usb" size={20} color="#..." />`.
Iconify names are kebab-case: `open-in-new`, `wifi-tethering`, `progress-activity`,
`arrow-back`, `content-copy`, `grid-view`, `developer-mode`.

## Recipes

### Page scaffolding

```
create_page("Foundations")           → page id
create_page("Components")
create_page("Screens")
```
Rename an existing page with `update_node(id=<page id>, name="Foundations")` — `create_page`
is the only page-creator, and `switch_page` is not a reliable way to aim later calls.

### Tokens (two themes)

```
create_collection("Dark")            → collectionId
create_variable("bg", "COLOR", "#0E1013", collectionId)
... one variable per token, same names in a "Light" collection
```
- A collection exposes exactly **one mode**; there is no tool to add a second. Use one
  collection per theme and keep names identical across them.
- Then bulk-bind literals: `lint_fix(page_id=..., rules=["no-hardcoded-colors"])`. It
  reports `{"applied": N}`; 400+ bindings in one call is normal.
- Same hex twice inside one collection (`primary` and `on`) makes the target ambiguous —
  harmless because the values are identical, but bind those by hand if the distinction
  matters.

### Component library

1. Draw a **specimen**: a 420-wide frame with a label row (name + mono spec line), the
   component, and a note. Consistent specimens read like documentation.
2. `node_to_component(ids=[...])` converts in place and returns the new ids.
3. Name variants `Button/Style=Primary`, then
   `combine_as_variants(ids=[...])` → a set named `Button` with property `Style`.
   Components must be **siblings**; `reparent_node` them into one parent first.
4. `create_slot(frame id)` on an inner frame of a main component gives instances a
   `Content` slot.
5. Screens still use plain frames (see golden rule 10); the library is the contract.

### Screens

```
<Frame name="01 · Home · dark" flex="col" w={412} h={915} bg="#0E1013"> … </Frame>
```
- Number frames `01 · …`, `02 · …` so `arrange` order and the layers panel agree.
- Clone for near-identical states: `clone_node` → `rename_node` → tweak. Much cheaper and
  more consistent than re-rendering.
- Insert an extra block at a specific index with
  `render(parent_id=<col>, insert_index=4, jsx=…)`.

### Overlays (dialogs over a screen)

```
render(parent_id=<screen>, jsx=<scrim frame 412×915 bg #000 opacity .45>)   → scrimId
render(parent_id=<screen>, jsx=<layer 412×915 justify="center" items="center"> dialog </Frame>)
set_layout_child(scrimId, positioning="ABSOLUTE"); node_move(scrimId, 0, 0)
set_layout_child(layerId, positioning="ABSOLUTE"); node_move(layerId, 0, 0)
set_effects(dialogId, type="DROP_SHADOW", offset_y=12, radius=32, color="#000000")
```
**Never nest the dialog inside the scrim** — opacity applies to the whole subtree and the
dialog turns grey. Two sibling layers, both absolute at `0, 0`.

### Menus (dropdown over a card)

A duration/permission menu must match what Material emits, or design and code drift:

- Panel: 16 dp radius, 8 dp vertical content padding, drop shadow.
- Selected row: highlighted fill, **8 dp** radius, `padding(horizontal = 8.dp)`. 16 − 8 = 8 keeps
  the selected row concentric with the panel.
- Panel width hugs its widest item — no fixed width. A `width(148.dp)` menu was rejected.
- Anchor to the trailing control, not the full-width row: a `DropdownMenu` inside a `Box` that
  wraps only the trailing icon/value opens right-aligned; anchored to the row it opens at the
  left edge. Do not compensate with a custom `Popup` — standard `DropdownMenu` with the shape
  and padding above is the accepted implementation.
- Subtitle text that would widen the menu unnecessarily belongs under the label, not in it.

### Cross-page moves and tidy-up

- `reparent_node(id, parent_id=<page id>)` moves a node between pages.
- `arrange(ids=[...], mode="grid", cols=3, gap=40, page_id=...)` packs nodes and puts the
  block at the origin. A header that should sit above the grid can be `node_move`d to a
  negative y, or you shift the grid instead.
- `viewport_get` / `viewport_set` / `viewport_zoom_to_fit` all exist in 0.16.0 (corrected
  2026-11-08; the older "no `viewport_zoom_to_fit`" note was wrong — see learnings log).

### Bulk property changes

`batch_update` applies many `{id, props}` operations with **one** layout pass. Supported
props: `spacing`, `padding`, `padding_horizontal`, `padding_vertical`, `counter_align`,
`sizing_horizontal`, `sizing_vertical`, `grow`, `name`, `visible`, `corner_radius`,
`auto_resize`, `direction`. Two passes are often needed because `HUG` only works on
auto-layout frames — set `direction` first, then `sizing_vertical: "HUG"`.

### Finding nodes

XPath via `query_nodes` beats scanning trees:

```
//FRAME[starts-with(@name, 'Button/')]
//*[@id='0:12']//FRAME[@name='Switch']
//TEXT[contains(@text, 'Setup')]
//FRAME[@width < 300]
```

## Failure modes

| Symptom | Cause | Fix |
|---|---|---|
| Text renders in Inter | `fontFamily` was used, or no `font` at all | add `font="Roboto"` to the node |
| Text squeezes into a 100 px column | row hugged instead of filling | add `w="fill"` to the row |
| Big empty space inside a card | wrapper frame had no height → 100 × 100 default | `flex="col"` or explicit `h` |
| `justify`/`items` ignored, children stack oddly | frame is not auto-layout | add `flex="row"` / `flex="col"` |
| Dialog looks washed out | scrim is the dialog's parent | split into sibling layers |
| Render landed on the wrong page | no `page_id`; `switch_page` did not stick | pass `page_id`, move nodes with `reparent_node` |
| `combine_as_variants` fails | components are not siblings | reparent into one parent first |
| Variant set is named after a whole component | names not `Name/Prop=Value` | rename, then combine |
| Instance shows the master's text everywhere | instances have no text overrides here | build screens from frames |
| `HUG` rejected by `batch_update` | frame is not auto-layout | set `direction` first, then `HUG` |
| RPC timeout on `render` | heavy subtree / app throttled | see below — verify before retrying |

## CLI traps (0.16.0)

| Symptom | Cause | Fix |
|---|---|---|
| Text renders as garbage glyph clusters (`? o ʃ ɥ ʞ ɿ d`) in one node only | The string contains `&` and the node is shaped against the **variable** `Roboto[wdth,wght].ttf` | Content is stored correctly; it is a render bug. Confirm by retyping the same string without `&` (renders clean), then restore the copy |
| Fig text breaks a few characters later than the app at the same size | OpenPencil lays a `grow` text child out ~10 dp wider than its frame | Accept it, or give the node an explicit width. Never widen the app to match |
| Node ids from `query --page` export the wrong frame | `--page` makes headless skip the earlier pages and renumber every id (same cause as `tree --page`) | Get ids from the host you will mutate: app-mode `query_nodes` for app-mode edits, a headless query **without** `--page` for headless reads |
| `node_resize` fails with `width: Invalid key` | Both dimensions are required | Pass `width` **and** `height` |
| No way to change a font size | `set_font` takes `family` only, `set_text_properties` accepts `id` and nothing else, `batch_update` has no fontSize | Delete the text node and `render` a replacement: `<Text size={12} font="Roboto" weight={500}>…</Text>` |
| A footer card will not stay at the bottom | the spacer was a fixed-height frame | Insert `<Frame name="Spacer" grow={1} h={0} />` **before** the card in the column |
| Headless PNG shows text overflowing a card the app renders cleanly | the rasteriser re-measures text with substituted fonts but does not re-solve the layout | Review screens with MCP `export_image` (inline PNG) or the running app; headless export is only a round-trip check |
| `pages` reports 0 nodes after the first page | Known bug | Use `info` or `query` |
| Headless `render` rejects `page_id` | Schema differs from MCP | Pass `parent_id=<page id>`; response ids are not the persisted ids — re-derive with `query` |
| `batch_update` fails with "Expected operations" | It wants a JSON **string** for `operations` | Pass the array as a string |
| `set_font` rejects `font` | Schema drift | It takes `family` |
| `export -f pdf` dies with `Cannot read properties of undefined (reading 'jsPDF')` | Bundled svg2pdf missing | Use png or svg |
| `--font-policy strict` changes nothing | No-op on PNG (byte-identical output) | Do not rely on it |
| `openpencil info` (app mode) hangs for 20 s while another agent edits | App Nap / busy app | Use headless `info` plus app-mode `tool call list_documents` |
| `openpencil fonts --json` returns `{}` in app mode | Fonts only works headless | Use `tool call get_font_status` for the live document |

## Timeouts and stalls

MCP calls give up at ~20 s on the client, but the app usually finishes the work.

1. Wait 15–30 s and probe with a light read (`get_current_page`).
2. If it is still stuck, run `open -a OpenPencil` — macOS App Nap throttles a background app and
   every call then dies at the timeout. One wake covers a batch, so batch work and wake once.
3. **Verify before retrying.** A timed-out `render` / `clone_node` frequently committed. Check
   `query_nodes` / `node_children` first (a timed-out `clone_node` did create `09 · timed pause`
   on 2026-11-08, and a retry would have duplicated it).
4. After a wake the app may report a different "current page". One more reason to pass
   `page_id` everywhere.
5. Keep a single `render` under roughly 120 nodes; split big screens into two calls.

## Verification loop

1. `export_image(ids=[...], page_id=..., maxEdge=500)` — eyeball the result; images come
   back inline, so no files are needed.
2. `lint(page_id=..., rules=["no-hardcoded-colors"])` — should end at 0 after `lint_fix`.
   Also worth running the accessibility rules explicitly (`color-contrast`, `touch-target-size`,
   `min-text-size`) — there is no `accessibility` preset, and `lint_fix` cannot apply those
   suggestions.
3. `analyze_overlaps(page_id=...)` — catches text bleeding out of frames and sibling
   collisions that look fine at thumbnail size.
4. `get_jsx(id)` — ground truth for what actually persisted.
5. `save_file` — then `list_pages` / `page_bounds` to confirm the shape of the file. The CLI
   never saves the open document; `-o` only writes a copy.

## Repository conventions

- One design per app: `apps/<app>/<app>.fig`, sourced from `docs/apps/<app>/ui.md`
  (copy in the spec is literal — do not paraphrase it in the design). When the design and the app
  disagree, the `.fig` is fixed first, then `ui.md`, then the code.
- Type is `Roboto` for UI and `Roboto Mono` for commands and endpoints, at the sizes the app ships
  (command 12, endpoint 13). None of it is metric-accurate until both families are installed — see
  [Fonts](#fonts-must-resolve-or-the-design-lies).
- Draw real tap targets so the design's geometry is the shipped geometry: a text action sits in a
  40 dp frame, an icon button in a 32 dp slot.
- Three pages, always the same order and names: **Foundations**, **Components**, **Screens**.
- Foundations panels are 1000 wide in one column, gap 48: cover, colour, typography,
  shape/spacing/layout, iconography, app icon.
- App icons live in that Foundations panel (`NN · App icon`, contents named `Icon …`):
  glyph concepts, a 512 master, the 108 dp adaptive layers (background / foreground /
  monochrome with the 66 dp safe circle), circle and squircle mask previews, a
  192/96/48/24 size ladder, and a mono spec line bottom-right — art imported with
  `import_svg` at its review size.
- Component specimens are 420 wide (`380` content + `20` padding), 3 per row, gap 40.
- Screen frames are 412 × 915, side padding 16, content width 380, 4-pt grid, radii
  card 20 / inner 12 / button 16 / pill 999.
- Name components `Family/Prop=Value`; name screens `NN · Screen · state`.

## Update protocol (required)

This skill is only useful if it stays true. **Every agent that uses it must update it in
the same change as the work it learns from.**

- Add a dated line to the [Learnings log](#learnings-log) for anything new: a prop that
  works, a prop that is silently dropped, a tool limitation, a timing fix, a better recipe.
- Correct the tables and recipes instead of appending a contradiction next to an old rule.
- Never delete a learning silently. If it no longer holds, mark it `superseded` with the
  date and what replaced it.
- When a tool is added or removed from the MCP server, update the [tool map](#tool-map).
- Keep code blocks copy-pasteable and tested. A recipe that has not been run is marked
  *(unverified)*.

## Learnings log

- **2026-11-06** — ~~Delta's MCP client cannot reach the localhost server; the same tools are
  reachable over streamable HTTP, and a curl wrapper script drove them.~~
  **Superseded 2026-11-08** — Zed has the MCP tools wired in and is the only editing path; Delta
  is read-only on designs. The bridge script was deleted. Never reintroduce a custom bridge or a
  shell wrapper around the MCP server.
- **2026-11-06** — ~~Sessions are capped: a leaked session blocked every new one until the server
  restarted, so each run had to terminate its own session.~~ **Superseded 2026-11-08** — a client
  that owns the session (Zed's MCP client) manages this; hand-rolled HTTP sessions were only a
  property of the removed bridge.
- **2026-11-06** — ~~Concurrent calls answer `503` + `retry-after` because the server serialises
  work. Retry rather than assuming the app died; `GET /health` stays `200` throughout.~~
  **Superseded 2026-11-08** — no hand-rolled client left; an MCP tool gap is now an App Nap
  problem, see [Timeouts](#timeouts-and-stalls).
- **2026-11-06** — `query_nodes` takes `selector`; passing `xpath` fails validation
  ("Expected selector"). `limit` / `page` paginate results.
- **2026-11-06** — `viewport_zoom_to_fit` **does** exist in 0.16.0 (headless tool registry); the
  earlier "does not exist" note was wrong — **superseded 2026-11-07** by the front-end guidance
  in this file, which also replaces the probe-after-timeout workaround for reads.
- **2026-11-06** — Text defaults to Inter. `fontFamily` is ignored; only `font` sets the
  family, and there is no inheritance from the parent frame.
- **2026-11-06** — A frame with no `flex` and no `h` is 100 × 100. This, not padding,
  was the cause of empty space inside cards and dialogs.
- **2026-11-06** — `justify` / `grow` are no-ops on a hugging row. `w="fill"` on the row
  is required; without it a `grow` text collapses to ~100 px and wraps.
- **2026-11-06** — `switch_page` returns success but does not redirect `render`. Pass
  `page_id` on every call, including `lint_fix`, `arrange` and `export_image`.
- **2026-11-06** — Nodes move between pages with `reparent_node(id, parent_id=<page id>)`;
  pages are renamed with `update_node(id=<page id>, name=…)` even though `create_page` is
  the only page creator.
- **2026-11-06** — `lint_fix(rules=["no-hardcoded-colors"])` bound 451 literals to the
  palette variables in a single call; `lint` then reported zero remaining.
- **2026-11-06** — Rendering an overlay inside a screen leaves it in flow. `set_layout_child`
  with `positioning="ABSOLUTE"` plus `node_move(0, 0)` pins it; the scrim must be a
  sibling of the dialog, never its parent (parent opacity dims the dialog).
- **2026-11-06** — `combine_as_variants` requires sibling components and names of the form
  `Family/Prop=Value`; the set takes the family name and the property name from the name.
- **2026-11-06** — `create_instance` takes only `component_id`, `x`, `y` — no parent — and
  instances expose no text overrides, so screen frames are composed literally.
- **2026-11-06** — MCP calls time out at ~20 s on heavy renders while still committing.
  Probing and `open -a OpenPencil` (App Nap) recovers the server; always verify before
  retrying so you don't duplicate frames.
- **2026-11-06** — `arrange` places the block at the origin, so a page header has to be
  moved above it (`y` may be negative) or the grid shifted down.
- **2026-11-07** — `import_svg` sizes the frame it creates from the SVG's `width`/`height`
  attributes and ignores the parent: 512 px art dropped into a 288 px slot overflows the
  layout. Author every SVG at the px size it is displayed at, then import.
- **2026-11-07** — `import_svg` always gives the new frame a white SOLID fill, even with
  `fill="none"` on the root `<svg>`; it shows through wherever the art is transparent.
  `set_fill` the frame to the backdrop right after importing.
- **2026-11-07** — a `<clipPath>` in imported SVG becomes a mask node that `save_file` then
  rejects with `Invalid value "VECTOR" for enum "MaskType"` — the whole document becomes
  unsaveable until that node is deleted. Fake a mask with a shaped background path instead.
- **2026-11-07** — `set_stroke` takes `weight`, not `width`; `width` fails validation with
  `width: Invalid key`.
- **2026-11-07** — `export_image` requires `maxEdge` ≥ 64, so a 24 or 48 px node cannot be
  exported at its native size through it. Omit `maxEdge` and the export lands at the node's
  own size (it never upscales).
- **2026-11-07** — `export_svg` answers instantly for a single VECTOR node and times out at
  20 s for a FRAME or a multi-node selection; export the path, not the frame.
- **2026-11-07** — `save_file` always accepted a `path`; the "save writes back to the opening
  path" note only describes the default. **MCP `save_file` is the only save path that edits the
  open document** (superseded 2026-11-08: the old `openpencil documents save` route is no longer
  used). **Headless:** `-o` on `export -f fig` / `eval` / `tool call` / `lint --fix` writes a copy
  anywhere, and `openpencil diff files <a> <b>` reports `✓ Documents match` on a fig round-trip,
  so hand mirroring is not needed.
- **2026-11-07** — the CLI's headless host has no fonts: it substitutes Roboto / Roboto Mono with
  Inter and measures text **+25 %** wide. `brew install --cask font-roboto font-roboto-mono` plus
  an app restart fixes the app side; headless text geometry stays untrustworthy.
- **2026-11-07** — one node (`Developer options & ADB`, `0:516` in `01 · Home · dark`) renders
  garbage glyph clusters because the string contains `&`; the same string without `&` renders
  clean and `get_node` shows the characters stored correctly. A shaping bug against the variable
  Roboto face — restore the copy, never reword it.
- **2026-11-07** — App Nap throttles the app when it is in the background: every call then dies at
  the 20 s RPC timeout with no retry. `open -a OpenPencil` wakes it, and one wake covers a batch of
  calls for a while, so batch work and wake once at the start instead of before every call.
  Headless commands never need it. *The "a bridge script stays the fallback" clause in the
  original entry is **superseded 2026-11-08**: there is no bridge, and MCP tools are the editing
  path.*
- **2026-11-07** — `tree --page` renumbers ids when it skips pages, `pages` reports 0 nodes after
  the first page, headless `render` rejects `page_id`, `batch_update.operations` must be a JSON
  string, `set_font` takes `family`, `export -f pdf` is broken, and `--font-policy` is a no-op.
  See the CLI traps table.
- **2026-11-08** — `--page` renumbers ids in `query` **and** `export`, not just `tree`: a page-filtered
  headless query returned `0:519` for the frame whose real id is `0:804`, and exporting the filtered
  id rendered an unrelated node from another page. App-mode `query_nodes` ids differ from headless
  ids for the same document — derive ids from the host you are about to mutate, never mix the two.
- **2026-11-08** — there is no font-size tool. `set_font` takes `family`, `set_text_properties`
  rejects every key except `id`, and `batch_update` has no fontSize. Changing a size means deleting
  the text node and rendering a replacement — which is why a specimen whose label changes size gets
  rebuilt instead of edited.
- **2026-11-08** — `node_resize` requires both `width` and `height`; `{"id", "height"}` fails with
  `width: Invalid key`. Ids read off a lint `path` are easy to point at the wrong node — `get_node`
  first (a banner title was resized to 140 × 40 this way).
- **2026-11-08** — `grow={1}` works as a JSX prop on a layout child: `<Frame name="Spacer" grow={1}
  h={0} />` inserted **before** a footer card pins that card to the bottom, with the spacer absorbing
  the difference. The serialized JSX drops `h={0}` but the layout still fills.
- **2026-11-08** — `lint` takes an explicit rule list (`min-text-size`, `touch-target-size`,
  `color-contrast`, …); `accessibility` is not an accepted preset. `lint` returns `suggestions`, but
  nothing applies them — `lint_fix` only knows `no-hardcoded-colors`.
- **2026-11-08** — headless PNG export re-measures text with substitute fonts without re-solving
  auto-layout, so a HUG card can look clipped and overlap its siblings in a file that the app renders
  perfectly. Judge screens with MCP `export_image` (inline PNG) or the running app; use headless
  export only to confirm the file round-trips.
- **2026-11-08** — a `render` script that dies after the RPC (a `NameError` in the trailing `print`)
  still committed the frame; re-running it produced a duplicate card. Check `node_children` before
  retrying any script that renders.
- **2026-11-08** — rebuilding a variant set in place: render plain sibling frames, then
  `node_to_component(ids=…)`, `rename_node` each to `Family/Prop=Value`, `combine_as_variants(ids=…)`.
  Replacing a specimen's rows this way keeps the page grid intact; only the specimen's own height
  changes, so keep ≥8 px of slack to the row below.
- **2026-11-08** — MCP tool schemas and `openpencil tool call` schemas diverge: MCP takes `page_id`,
  the CLI rejects it with `Invalid key: Expected never but received "page_id"` (it wants `page`).
  Derive ids **and** argument shapes from the host you will mutate.
- **2026-11-08** — a `clone_node` that reported a timeout had already created the node. `query_nodes`
  before retrying; the duplicate is otherwise silent.
- **2026-11-08** — `render` takes `replace_id` (swap one card in place, keeping its sibling index) and
  `insert_index` (slot a block into a layout parent). Both are cheaper than clone-and-delete.
- **2026-11-08** — `fetch_icons` then `<Icon name="material-symbols:timer-pause" />` works inside
  `render`; `search_icons` resolves the name when the first guess misses.
- **2026-11-08** — flow arrows are a `create_vector` with
  `path="M0 4 H160 M152 0 L160 4 L152 8"` plus `stroke` / `stroke_weight`; no need to compose three
  shapes.
- **2026-11-08** — a dialog or menu that must float over a screen is rendered as **siblings** with
  the scrim, then `set_layout_child(positioning="ABSOLUTE")` + `node_move(0, 0)` on each.
- **2026-11-08** — `lint(rules=["no-hardcoded-colors"])` then a single `lint_fix` bound 83 new
  colours introduced by a new screen in one call.
- **2026-11-08** — Material `DropdownMenu` carries 8 dp vertical content padding and, with a 16 dp
  panel radius, a selected row needs an 8 dp radius and `padding(horizontal = 8.dp)` to look
  concentric with the panel (16 − 8 = 8). The `.fig` and the Compose code match on 16 / 8 / 8.
- **2026-11-08** — a `DropdownMenu` anchored to a full-width row opens at the **left edge**; put it
  in a `Box` that wraps only the trailing control and it opens right-aligned. Never size the menu
  with a fixed width — let it hug the widest item.
- **2026-11-08** — `lint_fix(rules=["no-hardcoded-colors"])` binds **fills only**. Frame strokes
  (`strokes/0/color`) stay hardcoded while `lint` reports zero remaining — check with
  `node_bindings` on a bordered frame and bind them one by one.
- **2026-11-08** — an alternates/comparison board should be its own padded `surface` sheet with
  `rounded={32}` and the option panels inset as `bg` frames. A transparent top-level frame keeps
  the canvas honest but exports its light text on **white**, so the header disappears in a PNG.
- **2026-11-08** — a 1 px vertical line in a text node's own colour, running far past the line height
  in both MCP and headless exports, is a stale editor **caret** — there is no `LINE` node, no thin
  frame, and nothing in the tree at that x. Replacing the text with a fresh
  `render(replace_id=…)` clears it.
- **2026-11-08** — `render(parent_id=…)` and `set_layout_child` work **inside** a component variant,
  so a component can be repaired in place instead of rebuilt. `replace_id` keeps the old node's
  position, so a wider replacement needs a follow-up `node_move` to keep its right edge aligned.

## Related

- [Devinator UI spec](../../apps/devinator/ui.md) — tokens, components and copy this skill
  was first exercised against.
- Linked from [docs/context.md](../context.md) and [AGENTS.md](../../AGENTS.md) so agents load
  it before any `apps/*/*.fig` design task.
- **Editing split:** Zed (with the OpenPencil MCP tools wired in) is the only path that writes a
  `.fig`; Delta is read-only on designs. There is no HTTP bridge script any more — do not add one
  back.
- It lives here as a flat doc rather than a `.agents/skills/` folder. If it ever needs Zed's
  automatic skill discovery, move it to `.agents/skills/openpencil-skill/SKILL.md` unchanged and
  update the two links above.
