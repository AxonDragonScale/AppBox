# QRQanvas — Design Document

## Vision

QRQanvas is a QR code studio for Android that lets users generate visually striking, fully customizable QR codes and scan existing ones. The app treats QR codes as a creative medium — every element of a code (dots, eyes, colors, backgrounds, embedded imagery) is a design lever the user can pull.

---

## Features

### 1. QR Code Generator

The core feature. Users input content and customize every visual aspect of the resulting QR code.

**Supported Content Types:**
- Plain text
- URL / Website link
- Wi-Fi network (SSID, password, encryption type)
- Contact card (vCard)
- Email (address, subject, body)
- Phone number
- SMS (number, message)
- Geographic location (lat/lng)

**Customization Options:**

| Element | Options |
|---------|---------|
| **Dot Style** | Square (default), Circle, Rounded square, Diamond, Star, Heart |
| **Dot Color** | Solid color, Linear gradient, Radial gradient |
| **Eye Style (Finder Patterns)** | Square, Circle, Rounded square, Leaf, Diamond |
| **Eye Color** | Independent outer frame color + inner dot color |
| **Background** | Solid color, Transparent (PNG export), Image background |
| **Logo Embed** | Center logo with configurable size, padding, and shape (circle, rounded rect, square) |
| **Error Correction** | Low (7%), Medium (15%), Quartile (25%), High (30%) — auto-set to High when logo is embedded |
| **Quiet Zone** | Configurable margin around the code |

### 2. QR Code Scanner

Camera-based scanner using CameraX and ML Kit.

- Live camera preview with viewfinder overlay
- Auto-detect and decode QR codes in real time
- Display decoded content with type-aware actions (open URL, connect to Wi-Fi, add contact, etc.)
- Flashlight toggle
- Scan from gallery image (pick an image, decode any QR code in it)

### 3. Saved QR Codes

Users can save generated QR codes for later access.

- Persistent library of all saved QR codes (Room database)
- Each saved entry stores the content, all customization parameters, and a rendered preview
- Edit a saved QR code's design and re-save
- Delete saved codes
- Duplicate a saved code as a starting point for a new design

### 4. Export & Share

- Export as PNG (with configurable resolution) or SVG
- Share directly to other apps via Android share sheet
- Copy QR code image to clipboard

### 5. Scan History

- Log of all scanned QR codes with timestamp
- Tap to view decoded content and take type-aware actions
- Clear individual entries or full history

### 6. Settings

- Default error correction level
- Default export resolution
- Theme mode (Light / Dark / System)
- Dynamic color (Material You) toggle

### 7. Developer Tools

- Accessible via hidden gesture or settings
- Wraps `DevToolsScreen` from `:core:commons`
- App-specific tools: clear database, clear scan history, reset preferences

### 8. Templates

Style presets that let users skip manual customization and apply a complete look in one tap.

**Predefined Templates (bundled with the app):**

| Template | Description |
|----------|-------------|
| **Classic** | Black dots, white background, square dot and eye style — the standard QR look |
| **Rounded** | Circle dots, rounded square eyes, black on white |
| **Neon** | Circle dots with a cyan-to-magenta linear gradient, dark background |
| **Elegant** | Rounded square dots, leaf eyes, deep navy on off-white |
| **Sunset** | Diamond dots, orange-to-red radial gradient, warm cream background |
| **Monochrome** | Square dots, solid dark gray, light gray background — subtle and minimal |
| **Ocean** | Circle dots, teal-to-blue linear gradient, white background |
| **Midnight** | Rounded square dots, white on black (inverted), rounded eyes |

- Predefined templates are read-only and always available
- They are defined in code (not in the database) as constant `QRStyle` instances

**User Templates:**

- Save the current style from the Creator screen as a personal template
- User assigns a name when saving a template
- User templates are stored in Room and can be renamed or deleted
- Templates only store style — they are content-agnostic

**Template Picker (in Creator Screen):**

- Horizontally scrollable row at the top of the customization panel (above Dot Style)
- Each template shown as a small QR code thumbnail rendered with that style
- Predefined templates appear first, followed by user templates
- Tapping a template applies its `QRStyle` to the current QR code, replacing all style fields
- A "Save as Template" action in the Creator screen's overflow/action menu

---

## Architecture

### High-Level Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  Jetpack Compose + Material 3                           │
│  ┌──────┐ ┌─────────┐ ┌─────────┐ ┌────────┐ ┌──────┐ │
│  │ Home │ │ Creator │ │ Scanner │ │ Detail │ │ Settings│
│  └──┬───┘ └────┬────┘ └────┬────┘ └───┬────┘ └──┬───┘ │
│     │ VM+State │ VM+State  │ VM+State │ VM+State│      │
├─────┴──────────┴───────────┴──────────┴─────────┴──────┤
│                   Domain Layer                          │
│  ┌───────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ QRGenerator   │  │ QRRenderer   │  │ QRExporter   │ │
│  │ (ZXing logic) │  │ (Canvas draw)│  │ (PNG/SVG)    │ │
│  └───────────────┘  └──────────────┘  └──────────────┘ │
├────────────────────────────────────────────────────────-┤
│                    Data Layer                           │
│  ┌──────────────────┐  ┌─────────────────────────────┐ │
│  │ QRCodeRepository │  │ AppPrefsRepository          │ │
│  │ (Room)           │  │ (DataStore)                 │ │
│  └──────────────────┘  └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

**UI Layer** — Compose screens, ViewModels, UiState/UiEvent/UiEffect. Pure presentation. ViewModels call into domain and data layers.

**Domain Layer** — Core business logic that is not tied to Android framework or persistence. QR generation (ZXing BitMatrix creation), QR rendering (turning a BitMatrix + style config into a visual), and export logic.

**Data Layer** — Room database for saved QR codes and scan history. DataStore for user preferences. Repositories abstract storage from the rest of the app.

### Package Structure

```
com.axondragonscale.qrqanvas/
├── QRQanvasActivity.kt
├── QRQanvasApplication.kt
│
├── di/
│   └── QRQanvasModule.kt
│
├── database/
│   ├── QRQanvasDatabase.kt
│   ├── dao/
│   │   ├── QRCodeDao.kt
│   │   ├── ScanHistoryDao.kt
│   │   └── TemplateDao.kt
│   ├── entity/
│   │   ├── QRCodeEntity.kt
│   │   ├── ScanHistoryEntity.kt
│   │   └── TemplateEntity.kt
│   └── converter/
│       └── QRStyleConverter.kt
│
├── model/
│   ├── QRContent.kt            # Sealed class for content types (URL, Text, WiFi, etc.)
│   ├── QRStyle.kt              # Data class holding all customization parameters
│   ├── DotStyle.kt             # Enum: Square, Circle, RoundedSquare, Diamond, etc.
│   ├── EyeStyle.kt             # Enum: Square, Circle, Rounded, Leaf, etc.
│   ├── ColorConfig.kt          # Sealed: Solid, LinearGradient, RadialGradient
│   ├── LogoConfig.kt           # Data class: uri, size, padding, shape
│   ├── ExportFormat.kt         # Enum: PNG, SVG
│   ├── ScanResult.kt           # Data class for decoded scan output
│   └── PredefinedTemplates.kt  # Object with constant QRStyle instances for bundled templates
│
├── core/
│   ├── QRGenerator.kt          # ZXing wrapper — content + error correction → BitMatrix
│   ├── QRRenderer.kt           # BitMatrix + QRStyle → Bitmap (Canvas drawing)
│   ├── QRExporter.kt           # Bitmap → PNG file / SVG string / share intent
│   └── QRContentParser.kt      # Parse raw scanned string into QRContent type
│
├── repository/
│   ├── QRCodeRepository.kt     # CRUD for saved QR codes
│   ├── ScanHistoryRepository.kt
│   ├── TemplateRepository.kt   # User templates CRUD + merges with predefined
│   └── AppPrefsRepository.kt   # DataStore preferences
│
├── ui/
│   ├── Route.kt
│   ├── QRQanvasApp.kt          # NavHost
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── common/                  # Shared composables (QRPreview, ColorPicker, etc.)
│   ├── home/
│   │   ├── Home.kt
│   │   ├── HomeVM.kt
│   │   ├── HomeUiState.kt
│   │   └── HomeUiEvent.kt
│   ├── creator/
│   │   ├── Creator.kt
│   │   ├── CreatorVM.kt
│   │   ├── CreatorUiState.kt
│   │   └── CreatorUiEvent.kt
│   ├── scanner/
│   │   ├── Scanner.kt
│   │   ├── ScannerVM.kt
│   │   ├── ScannerUiState.kt
│   │   └── ScannerUiEvent.kt
│   ├── detail/
│   │   ├── Detail.kt
│   │   ├── DetailVM.kt
│   │   ├── DetailUiState.kt
│   │   └── DetailUiEvent.kt
│   ├── history/
│   │   ├── History.kt
│   │   ├── HistoryVM.kt
│   │   ├── HistoryUiState.kt
│   │   └── HistoryUiEvent.kt
│   ├── settings/
│   │   ├── Settings.kt
│   │   ├── SettingsVM.kt
│   │   ├── SettingsUiState.kt
│   │   └── SettingsUiEvent.kt
│   └── devtools/
│       └── QRQanvasDevTools.kt
│
└── util/
    └── Extensions.kt
```

---

## Data Models

### QRCodeEntity (Room)

```
QRCodeEntity
├── id: Long (PK, auto-generate)
├── title: String                  # User-assigned name (or auto-generated from content)
├── contentType: String            # Discriminator: "url", "text", "wifi", etc.
├── contentData: String            # Serialized content payload (JSON)
├── styleData: String              # Serialized QRStyle (JSON via kotlinx.serialization)
├── createdAt: Long                # Epoch millis
├── updatedAt: Long                # Epoch millis
```

### ScanHistoryEntity (Room)

```
ScanHistoryEntity
├── id: Long (PK, auto-generate)
├── rawValue: String               # The raw decoded string
├── contentType: String            # Detected type
├── scannedAt: Long                # Epoch millis
```

### QRStyle (kotlinx.serialization)

```
QRStyle
├── dotStyle: DotStyle
├── dotColor: ColorConfig
├── eyeStyle: EyeStyle
├── eyeFrameColor: ColorConfig
├── eyeDotColor: ColorConfig
├── backgroundColor: ColorConfig
├── backgroundImageUri: String?
├── logoConfig: LogoConfig?
├── errorCorrection: ErrorCorrectionLevel   # L, M, Q, H
├── quietZone: Int                          # Module units
├── size: Int                               # Export pixel size
```

### QRContent (Sealed Hierarchy)

```
QRContent
├── Text(value: String)
├── Url(url: String)
├── WiFi(ssid: String, password: String, encryptionType: WifiEncryption)
├── Contact(name: String, phone: String?, email: String?, ...)
├── Email(address: String, subject: String?, body: String?)
├── Phone(number: String)
├── Sms(number: String, message: String?)
├── Location(latitude: Double, longitude: Double)
```

### TemplateEntity (Room — user templates only)

```
TemplateEntity
├── id: Long (PK, auto-generate)
├── name: String                   # User-assigned template name
├── styleData: String              # Serialized QRStyle (JSON via kotlinx.serialization)
├── createdAt: Long                # Epoch millis
```

### Template (UI model)

```
Template
├── id: String                     # "predefined_classic" or "user_<roomId>"
├── name: String
├── style: QRStyle
├── isPredefined: Boolean
```

Predefined templates are mapped to this model from `PredefinedTemplates` constants.
User templates are mapped from `TemplateEntity`. The unified `Template` model is what the UI layer consumes.

---

## Screen Designs

### Home Screen

The landing screen. Shows saved QR codes in a grid and provides entry points to Create and Scan.

- **Top app bar** with app title and settings icon
- **Bottom navigation** or **FAB cluster** with two primary actions: Create, Scan
- **Grid/List** of saved QR codes showing preview thumbnail, title, and content type icon
- **Empty state** illustration when no codes are saved
- Long-press or swipe for delete/duplicate actions
- Tapping a saved code navigates to Detail screen

### Creator Screen

The main customization studio. Split into two logical zones:

**Top zone — Live Preview:**
- Real-time rendered QR code preview that updates as the user changes settings
- The preview is the actual rendered output (not a placeholder)

**Bottom zone — Customization Panel (scrollable):**
1. **Content Input** — Content type selector (chips/tabs) + type-specific input fields
2. **Template Picker** — Horizontally scrollable row of template thumbnails (predefined first, then user templates); tapping one applies its `QRStyle`
3. **Dot Style** — Visual selector showing each dot shape
4. **Dot Color** — Color picker with solid/gradient modes
5. **Eye Style** — Visual selector for finder pattern shapes
6. **Eye Color** — Separate pickers for frame and inner dot
7. **Background** — Color picker or image picker
8. **Logo** — Image picker + size slider + shape selector
9. **Error Correction** — Segmented button (L / M / Q / H) — auto-locks to H when logo is set
10. **Advanced** — Quiet zone slider, export size

**Actions:**
- Save button — persists to database
- Export/Share button — opens export options (format, resolution, share)
- Save as Template — saves current `QRStyle` as a user template (overflow menu or action icon)

### Scanner Screen

- Full-screen camera preview with a semi-transparent overlay and viewfinder cutout
- Animated scan line or corner brackets in the viewfinder
- Flashlight toggle button
- Gallery pick button (scan from image)
- On successful scan: bottom sheet slides up with decoded content, type icon, and action buttons (Open, Copy, Share)
- Auto-saves to scan history

### Detail Screen

View and manage a saved QR code.

- Large rendered QR code display
- Content summary (type + decoded text)
- Style summary chips
- Action buttons: Edit (opens Creator pre-filled), Export, Share, Delete
- Creation/last-modified timestamps

### History Screen

Chronological list of scanned QR codes.

- Each row: content type icon, truncated raw value, timestamp
- Tap to expand with action buttons (Open, Copy, Share)
- Swipe to delete individual entries
- "Clear All" in top bar overflow menu

### Settings Screen

- Theme mode selector (Light / Dark / System)
- Dynamic color toggle (Material You)
- Default error correction level
- Default export resolution preset
- About section (version, licenses)
- Developer tools entry (long-press version or hidden gesture)

---

## Navigation Graph

```
Home ──────────► Creator (new)
  │                 │
  │                 ├──► (save) ──► back to Home
  │                 ├──► (export/share)
  │                 └──► (save as template)
  │
  ├──► Detail ────► Creator (edit, pre-filled)
  │       │
  │       ├──► (export/share)
  │       └──► (delete) ──► back to Home
  │
  ├──► Scanner
  │       │
  │       └──► (scan result actions)
  │
  ├──► History
  │       │
  │       └──► (result actions)
  │
  ├──► Templates ──► (manage user templates: rename, delete)
  │
  └──► Settings
          │
          └──► DevTools
```

**Route Definitions:**
```
Route.Home                        — Saved QR codes grid
Route.Creator(qrCodeId: Long?)    — Create new (null) or edit existing
Route.Detail(qrCodeId: Long)      — View saved QR code
Route.Scanner                     — Camera scanner
Route.History                     — Scan history list
Route.Templates                   — Manage user templates
Route.Settings                    — App settings
Route.DevTools                    — Developer tools
```

---

## QR Generation & Rendering Pipeline

### Step 1: Generate BitMatrix (QRGenerator)

```
QRContent → encode to raw string → ZXing QRCodeWriter.encode() → BitMatrix
```

- Content types are serialized to their standard QR string formats (e.g., `WIFI:T:WPA;S:MyNetwork;P:password;;` for Wi-Fi)
- Error correction and quiet zone are passed as `EncodeHintType` hints
- Output is a `BitMatrix` — a 2D boolean grid where `true` = dark module

### Step 2: Render Styled Bitmap (QRRenderer)

```
BitMatrix + QRStyle → Canvas drawing → Bitmap
```

1. Create a `Bitmap` of the target size
2. Calculate module size = `bitmapSize / matrixSize`
3. Draw background (solid color, gradient, or image)
4. Iterate the BitMatrix:
   - **Finder patterns** (top-left, top-right, bottom-left 7×7 regions): Draw with eye style and eye colors
   - **Data modules**: Draw with dot style and dot color
   - Skip modules in the logo area if a logo is configured
5. Draw logo overlay (scaled, clipped to shape, with padding)

The renderer must also expose a Compose-friendly path: rendering into a `Canvas` composable for real-time preview without bitmap allocation on every frame. Use `remember` and `derivedStateOf` to minimize recomposition.

### Step 3: Export (QRExporter)

- **PNG**: Write `Bitmap` via `Bitmap.compress()` to a file in app cache, then share via `FileProvider`
- **SVG**: Generate SVG path data from the BitMatrix + style (vector output, resolution-independent)
- **Share**: Create a `ShareCompat.IntentBuilder` or direct `Intent.ACTION_SEND` with the exported file URI

---

## Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| QR rendering approach | Custom Canvas drawing over ZXing BitMatrix | ZXing only generates the data matrix; all visual customization requires custom rendering |
| Real-time preview | Compose `Canvas` composable reading style state | Avoids Bitmap allocation churn during editing; recomposes only the canvas |
| Thumbnail rendering | On-the-fly from stored `QRStyle` — no cached preview images | `styleData` is already the single source of truth; eliminates file storage overhead, staleness, and cache cleanup complexity. Thumbnails render at small resolution (~100px) which is trivial. If the Home grid ever lags with many items, add an in-memory `LruCache<id+updatedAt, Bitmap>` — no disk needed |
| Style persistence | kotlinx.serialization JSON stored as String column in Room | Flexible schema evolution without Room migrations for style changes |
| Content type modeling | Sealed class hierarchy | Type-safe, exhaustive when-matching, easy to extend |
| Scan integration | CameraX `MlKitAnalyzer` | First-party integration, avoids manual frame conversion boilerplate |
| Logo embedding | Render logo on top of QR + use High error correction | Standard approach; 30% error correction compensates for obscured modules |
| Export file sharing | FileProvider with cache directory | No WRITE_EXTERNAL_STORAGE permission needed; standard Android sharing pattern |
| Predefined templates | Hardcoded `QRStyle` constants in `PredefinedTemplates` object | No DB migration needed when adding/modifying bundled templates; always available offline |
| User template storage | Room entity with serialized `QRStyle` JSON | Same serialization strategy as QR codes; consistent and flexible |
| Template model split | Separate `TemplateEntity` (Room) and `Template` (UI) | Predefined templates have no Room entity; unified UI model hides the source distinction |

---

## Permissions

| Permission | Usage | Required |
|------------|-------|----------|
| `CAMERA` | QR code scanning | Runtime (Accompanist) |

No other permissions needed. File export uses app cache + FileProvider. Gallery access uses the photo picker (no permission required on API 31+).

---

## Future Considerations

- **Batch generation**: Create multiple QR codes from a CSV or list input
- **Animated QR codes**: GIF/APNG export with animated dot patterns
- **Widget**: Home screen widget showing a pinned QR code for quick access (Glance)
- **QR code in-painting**: Use the camera to overlay a styled QR code onto a real-world surface for AR preview
- **Template sharing**: Export/import user templates as files or deep links