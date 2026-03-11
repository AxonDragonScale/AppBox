# Pencil MCP — Learned Behaviours & Gotchas

## Property Persistence

Not all properties persist when using `U()` or `R()`. The engine silently drops certain properties depending on the node type and whether a layout context is established.

### Properties that reliably persist
- `height` (fixed number)
- `width` (fixed number or `fill_container` when inside a layout frame)
- `fill`, `cornerRadius`, `stroke`, `gap`, `padding` (uniform)
- `fontSize`, `fontFamily`, `fontWeight`, `lineHeight`, `letterSpacing`, `content`
- `justifyContent`, `alignItems` (on frames that already have layout)
- `layout` (when the parent is already a layout frame)
- `textGrowth`

### Properties that are silently dropped
- `paddingLeft`, `paddingRight`, `paddingTop`, `paddingBottom` (directional padding) — use uniform `padding` instead
- `layout` on frames that are direct children of non-layout parents
- `marginLeft`, `marginRight` (margins are not supported)
- `flexGrow`, `wrap` on text nodes

### Workaround: always use uniform `padding`
Directional padding (`paddingLeft` etc.) is dropped by the engine. Use uniform `padding` for all spacing needs. If asymmetric padding is required, restructure the hierarchy instead.

```
// WRONG — silently dropped
U("nodeId", { paddingLeft: 16, paddingRight: 16 })

// CORRECT — persists reliably
U("nodeId", { padding: 16 })
```

---

## Text Wrapping

Text nodes do not wrap by default. They render at their natural single-line width regardless of the parent container size.

### Making text wrap
Two properties must both be set on the text node:
- `textGrowth: "fixed-width"` — tells the engine to fix the width and grow vertically
- `width: "fill_container"` — constrains the text to its parent layout frame's width

```
U("textNodeId", { textGrowth: "fixed-width", width: "fill_container" })
```

### Requirements
- The **parent frame must have `layout` set** (e.g. `layout: "vertical"`) for `fill_container` to be resolved
- Setting only `wrap: true` or only `width: "fill_container"` does not work alone
- `wrap: true` is not a valid property — it gets dropped

---

## Layout on Frames

### `layout` only takes effect when the parent has layout too
A frame's `layout` property is silently ignored if its parent is not already a layout frame. To enable layout on a child frame, ensure the entire ancestor chain has layout set, or use `placeholder: true` as a hint.

### `placeholder: true`
Setting `placeholder: true` on a frame persists but does not itself enable flex layout. It can be used as a marker but does not unlock padding or layout.

### Frames with no layout context
Outer wrapper frames (e.g. screen-level Tab Bar frames) often have no layout and will silently reject `layout`, `paddingLeft/Right/Top/Bottom`. Workarounds:
- Use the uniform `padding` property — it persists even without layout
- Use a fixed `width` (not `fill_container`) on child elements to inset them from edges
- Increase outer frame `height` and inner element `height` together to simulate vertical padding

---

## Tab Bar Padding Pattern

Tab bar outer frames reject directional padding. The correct approach to add side insets:

1. Give the pill a fixed pixel `width` = screen_width − (2 × desired_inset)  
   e.g. for 16px inset on a 402px screen: `width: 370`
2. Increase outer bar `height` to create top/bottom breathing room  
   e.g. `height: 86` instead of 62
3. Bump pill `padding` (uniform) for inner spacing  
   e.g. `padding: 6`

---

## Category Chip / Badge Padding Pattern

Chips and badges that use `justifyContent: center` with a text child will not show horizontal padding unless `padding` (uniform) is applied. Directional padding is dropped.

```
// Correct pattern for chips
U("chipId", { padding: 10 })        // comfortable chip
U("badgeId", { padding: 6 })        // compact badge
```

The `height` property can remain fixed alongside `padding` — the engine respects both.

---

## R() (Replace) Behaviour

- `R()` replaces the node and all its children — children from the original node are gone
- You cannot reference existing node IDs inside the `children` array of an `R()` call
- Properties specified in the `R()` definition are subject to the same silent-drop rules as `U()`
- After `R()`, always verify with `batch_get` that the intended properties were actually stored before proceeding

---

## Stats / Equal-Width Row Pattern

A row of equal-width `fill_container` children only distributes evenly if the parent has `layout: horizontal` set. Without it, children overflow or overlap.

```
U("rowFrameId", { layout: "horizontal" })
```

---

## Checking What Actually Persisted

After any batch of `U()` or `R()` calls, verify with `batch_get` on the affected nodes before moving on. The operation result output does not indicate whether the specific property was accepted — it only confirms the operation ran without throwing.

---

## Unsetting / Clearing a Property

Setting a property to `null` in `U()` is not valid and will crash the operation with a TypeError. There is no way to "unset" a property via `U()`.

To remove a property from a node (e.g. a fixed `width` that should become `fit_content`), use `R()` to reconstruct the node without that property. The engine will then use the default/inferred value.

```
// WRONG — crashes with TypeError
U("nodeId", { width: null })

// CORRECT — rebuild without the property
R("nodeId", { type: "frame", layout: "horizontal", height: 38, ... })
```

---

## I() Insertion Order

`I()` always appends the new node at the **end** of the parent's children list. When child order matters — for example, a `justifyContent: space_between` row where a button must appear on the left and a timestamp on the right — the inserted node may land in the wrong position.

### Workaround: follow I() with M()
After inserting, use `M()` to move the node to the correct index.

```
// Insert lands at the end (wrong side of the row)
I("rowFrameId", { type: "ref", ref: "FY5ll", name: "shareBtn" })

// Move it to index 0 so it appears on the left
M("insertedNodeId", "rowFrameId", 0)
```

---

## Efficient Debugging Flow

1. Take a screenshot to see the visual state
2. Use `snapshot_layout` with `problemsOnly: true` to catch clipping and overflow
3. Use `batch_get` with `readDepth: 1` to read the actual stored properties on suspect nodes
4. Compare stored values to intended values — if a property is missing, it was silently dropped
5. Try `padding` (uniform) instead of directional padding, and verify persistence before continuing
