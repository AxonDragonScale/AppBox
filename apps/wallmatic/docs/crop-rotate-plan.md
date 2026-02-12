# Crop & Rotate Feature Plan

## Overview

Add in-screen crop and rotate functionality to the Wallpaper screen using
`WallpaperManager.setStream()` with `visibleCropHint`. No duplicate image
storage — only crop coordinates and rotation metadata are persisted.

## Core Principles

- **No image duplication** — store only metadata (~20 bytes per wallpaper)
- **In-screen edit mode** — toggle overlay on the existing Wallpaper screen
- **True fullscreen** — wallpaper and crop overlay fill edge-to-edge, behind system bars
- **Controls as overlays** — top bar and bottom bar float on top with semi-transparent bg
- **Apply on set** — crop/rotation only applied when wallpaper is actually set via `WallpaperManager`
- **Multi-gesture** — single-finger drag on handles + two-finger pinch to resize

## Database Changes

Update `Wallpaper` entity (no migration needed — pre-release app):

```kotlin
@Entity(tableName = "wallpaper")
data class Wallpaper(
    val uri: String,
    var isBlacklisted: Boolean = false,
    val cropLeft: Int? = null,
    val cropTop: Int? = null,
    val cropRight: Int? = null,
    val cropBottom: Int? = null,
    val rotationDegrees: Int = 0,
)
```

Helper extension converts nullable fields to `android.graphics.Rect?`.

## UI Architecture

### Normal Mode

```
┌─────────────────────────────────────┐
│                                     │
│         FULLSCREEN WALLPAPER        │
│         (ContentScale.Crop)         │
│                                     │
│                            ┌────┐   │
│                            │ ✏️ │   │  ← Edit FAB
│                            └────┘   │
└─────────────────────────────────────┘
```

### Edit Mode

Everything is fullscreen. Controls float as semi-transparent overlays.

```
┌─────────────────────────────────────┐
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  │
│ ┃ [✕]  Edit Wallpaper      [✓] ┃  │  ← TopAppBar overlay (70% black)
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  │
│                                     │
│     FULLSCREEN WALLPAPER            │
│     (ContentScale.Crop)             │
│     with graphicsLayer rotation     │
│                                     │
│     ┌── ─ ─ ─ ─ ─ ─ ─ ─ ─ ──┐     │
│     │   CROP RECT             │     │  ← Draggable crop area
│     │   (clear view)          │     │    Outside is dimmed 55% black
│     │   Rule-of-thirds grid   │     │
│     └── ─ ─ ─ ─ ─ ─ ─ ─ ─ ──┘     │
│                                     │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  │
│ ┃      Rotation: 90°            ┃  │  ← Bottom bar overlay (70% black)
│ ┃  [⟲ 90°] [Reset] [⟳ 90°]   ┃  │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  │
└─────────────────────────────────────┘
```

## State Management

### WallpaperUiState

```kotlin
data class WallpaperUiState(
    val isLoading: Boolean = true,
    val wallpaper: Wallpaper? = null,
    val imageSize: IntSize = IntSize.Zero,  // original image dimensions
    val isEditMode: Boolean = false,
    val tempCropRect: Rect? = null,         // in image pixel coords
    val tempRotation: Int = 0,
)
```

### WallpaperUiEvent

```kotlin
sealed interface WallpaperUiEvent {
    data object ToggleEditMode
    data class UpdateCrop(val rect: Rect)
    data object RotateClockwise            // +90°
    data object RotateCounterClockwise     // -90°
    data object SaveEdits                  // persist to DB
    data object CancelEdits                // discard temp values
    data object ResetEdits                 // clear crop + rotation
}
```

## CropOverlay Component

A fullscreen Canvas composable layered on top of the wallpaper.

### Coordinate System

- All interaction happens in **screen coordinates**
- The overlay maps between screen coords ↔ image pixel coords using
  `ContentScale.Crop` math: `scale = max(canvasW/imageW, canvasH/imageH)`
- Initial crop rect: centered with 10% inset on each side (if no saved crop)
- Saved crop rect: mapped from image pixel coords to screen coords on enter

### Handles

```
        ──T──
   TL ┌─────┐ TR
      │     │
   L  │     │  R
      │     │
   BL └─────┘ BR
        ──B──
```

- **Corner handles (TL, TR, BL, BR)**: L-shaped white lines, **72px touch radius**
- **Edge handles (T, B, L, R)**: Short white lines at midpoint, **56px touch radius**
- **Center**: Drag anywhere inside crop rect to move it
- **Pinch**: Two-finger pinch anywhere to uniformly resize from center

### Visual Style

| Element           | Style                              |
| ----------------- | ---------------------------------- |
| Dim overlay       | `Color.Black` at 55% opacity      |
| Crop border       | `Color.White`, 2px stroke          |
| Grid lines        | `Color.White` at 40%, 0.8px stroke |
| Corner handles    | White L-shapes, 32px arms, 5px    |
| Edge handles      | White lines, 40px long, 5px       |
| Min crop size     | 80px (screen coords)               |

### Gesture Handling

Uses `awaitEachGesture` for combined single-touch and multi-touch handling:

- **Single-finger drag**: `awaitFirstDown` → detect handle → track drag delta → `applyDrag`
- **Two-finger pinch**: Track distance between pointers → compute scale factor → `applyPinch`
  - Pinch scales uniformly around the crop rect center
  - Clamped to bounds and minimum crop size
  - Once pinch starts, single-finger drag is suppressed until all fingers lift
- **On gesture end**: Convert screen rect → image rect, emit `UpdateCrop`

### Fullscreen

- TopAppBar uses `windowInsets = WindowInsets(0)` to avoid consuming system bar space
- TopAppBar adds its own `statusBarsPadding()` for visual positioning only
- Bottom controls add `navigationBarsPadding()`
- The CropOverlay Canvas fills edge-to-edge behind both system bars

## Rotation Preview

The `AsyncImage` in edit mode applies `Modifier.graphicsLayer { rotationZ = tempRotation }`.
This gives immediate visual feedback. The actual bitmap rotation happens in
`WallpaperUpdater` when the wallpaper is set.

## WallpaperUpdater Changes

```kotlin
fun setWallpaper(wallpaper: Wallpaper, target: TargetScreen) = runCatching {
    // 1. Load bitmap from URI
    // 2. If rotationDegrees != 0 → apply Matrix rotation, recycle original
    // 3. Get cropRect from entity fields → visibleCropHint
    // 4. Compress to PNG → ByteArrayOutputStream
    // 5. manager.setStream(inputStream, cropHint, true, targetFlag)
    // 6. Recycle bitmap
}
```

## ViewModel Flow

```kotlin
// Image size computed on init (inJustDecodeBounds, no full bitmap load)
// Stored in uiState.imageSize for CropOverlay coordinate mapping

ToggleEditMode  → copy temp values from wallpaper entity, flip isEditMode
UpdateCrop      → update tempCropRect
RotateCW/CCW    → tempRotation = (tempRotation ± 90) % 360
SaveEdits       → persist crop + rotation to DB via repository, exit edit mode
CancelEdits     → discard temp values, exit edit mode
ResetEdits      → tempCropRect = null, tempRotation = 0
```

## Files Changed

| File                     | Change                                            |
| ------------------------ | ------------------------------------------------- |
| `entity/Wallpaper.kt`   | Add crop fields, rotation field, getCropRect()    |
| `WallpaperUiState.kt`   | Add imageSize, isEditMode, tempCropRect, tempRotation |
| `WallpaperUiEvent.kt`   | Add all edit events                               |
| `WallpaperVM.kt`        | Handle events, compute imageSize on init          |
| `Wallpaper.kt`          | Normal/Edit mode split, overlay controls, insets  |
| `CropOverlay.kt`        | **NEW** — fullscreen crop overlay with drag+pinch |
| `WallpaperUpdater.kt`   | Apply rotation + cropHint in setWallpaper()       |
| `WallmaticRepository.kt`| Add `updateWallpaper(Wallpaper)` overload         |

## Key Advantages

- **Zero storage overhead** — metadata only
- **Non-destructive** — original images never touched
- **True fullscreen** — edge-to-edge behind system bars
- **Immediate feedback** — rotation preview via graphicsLayer
- **Multi-gesture** — drag handles + pinch to resize
- **No new dependencies** — pure Compose Canvas + gestures