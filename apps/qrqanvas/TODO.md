# QRQanvas — TODO

## ~~Task 1: App Foundation~~ ✅
Set up the app skeleton: `QRQanvasApplication` (Koin init), `QRQanvasModule` (empty), update `QRQanvasActivity` (splash screen, edge-to-edge), rename theme to `QRQanvasTheme` with proper system bar handling, update `AndroidManifest.xml` (Application class, camera permission), create `Route.kt` (all routes), and `QRQanvasApp.kt` (NavHost shell with placeholder text for each destination).

## ~~Task 2: Data Models & Enums~~ ✅
Create all model classes: `QRContent` (sealed class), `QRStyle` (data class), `DotStyle` (enum), `EyeStyle` (enum), `ColorConfig` (sealed class), `LogoConfig` (data class), `ExportFormat` (enum), `ScanResult` (data class), `Template` (data class), and `PredefinedTemplates` (object with 8 constant QRStyle instances).

## ~~Task 3: UI Skeleton, Settings & DevTools~~ ✅
Scaffold all screens with real UI structure (TopAppBars, FABs, empty states, section headers, placeholder content areas) so the full app flow is tappable. Create `UIMode` enum, `AppPrefsRepository` (Preferences DataStore with `uiMode` + `dynamicTheme`), wire theme prefs into `QRQanvasActivity`. Build full Settings screen (Appearance section with UIMode segmented button + Dynamic Theme toggle, About section, DevTools entry). Build `QRQanvasDevTools` wrapping commons `DevToolsScreen`. Wire all screens into `QRQanvasApp` NavHost with proper navigation. Register `AppPrefsRepository` and `SettingsVM` in `QRQanvasModule`.

---

## ~~Task 4: QR Generator~~ ✅
Create `core/QRGenerator.kt` — a class that takes a `QRContent` and `ErrorCorrectionLevel` and returns a ZXing `BitMatrix`. Each `QRContent` subtype is encoded to its standard QR string format (e.g. `WIFI:T:WPA;S:...;P:...;;`). Use `QRCodeWriter.encode()` with appropriate `EncodeHintType` hints (error correction, margin = 0, UTF-8 charset). Register in `QRQanvasModule`.

**Verify:** App compiles and launches without crashes. No visible change.

---

## ~~Task 5: QR Renderer — Square Dots Only~~ ✅
Create `core/QRRenderer.kt` — takes a `BitMatrix` + `QRStyle` and produces a `Bitmap` via Android `Canvas`. For now, implement only: solid background color, square dot rendering for data modules, and basic square finder pattern (outer frame + inner dot) drawing. Detect finder pattern regions (the three 7×7 corners) so they are drawn separately from data modules. Register in `QRQanvasModule`.

**Verify:** App compiles and launches. No visible change yet.

## ~~Task 6: Creator Screen — Minimal with Live Preview~~ ✅
Add `CreatorVM`, `CreatorUiState`, `CreatorUiEvent` to the existing Creator screen skeleton. Replace the preview placeholder with a live QR code preview (rendered as an `Image` from the `Bitmap` produced by QRGenerator + QRRenderer). Replace the Content section placeholder with a text input field. Use `QRContent.Text` only. Wire `CreatorVM` into `QRQanvasModule`.

**Verify:** Launch → tap FAB → type text → see a live black-and-white QR code update in real time.

## ~~Task 7: Creator Screen — Content Type Selector~~ ✅
Add a content type selector (row of filter chips) to the Creator screen's Content section. Supported types: Text, URL, WiFi, Email, Phone, SMS. Below the selector, show type-specific input fields (e.g. WiFi shows SSID + password + encryption dropdown, Email shows address + subject + body). Update `CreatorVM` to construct the appropriate `QRContent` subtype from the active inputs.

**Verify:** Launch → Creator → switch between content types → fill in fields → QR preview updates with the correct encoded content.

## ~~Task 8: Dot Style Rendering + Picker~~ ✅
Implement all `DotStyle` shapes in `QRRenderer`: Circle, RoundedSquare, Diamond, Star, Heart (Square is already done). Each style draws its shape within the module's bounding rect on the Canvas. Add a "Dot Style" visual selector to the Creator panel (replace the placeholder) — a row of small icons or labeled chips for each `DotStyle`. Selecting one updates `QRStyle.dotStyle` and the live preview re-renders.

**Verify:** Launch → Creator → scroll down → tap different dot styles → preview updates with circles, diamonds, stars, etc.

## ~~Task 9: Color Pickers (Dot & Background)~~ ✅
Add a color picker component (`ui/common/ColorPicker.kt`) supporting solid color selection (a palette of preset colors + a custom color option). Replace the "Dot Color" and "Background Color" section placeholders in Creator with these pickers. For now, support `ColorConfig.Solid` only — gradient support comes later. Selecting colors updates `QRStyle.dotColor` and `QRStyle.backgroundColor`.

**Verify:** Launch → Creator → pick a dot color and background color → preview renders with those colors.

## ~~Task 10: Eye Style Rendering + Picker~~ ✅
Implement all `EyeStyle` shapes in `QRRenderer`: Circle, RoundedSquare, Leaf, Diamond (Square is already done). Each eye style defines how the outer frame and inner dot of the three finder patterns are drawn. Replace the "Eye Style" placeholder in Creator with a visual selector. Replace "Eye Color" placeholder with two color pickers (reuse the color picker component) for eye frame and eye dot colors.

**Verify:** Launch → Creator → pick different eye styles and colors → finder patterns in the preview update accordingly.

## ~~Task 11: Gradient Color Support~~ ✅
Extend `QRRenderer` to support `ColorConfig.LinearGradient` and `ColorConfig.RadialGradient` for dot color, eye colors, and background. Update the color picker component to allow switching between Solid / Linear Gradient / Radial Gradient modes, with start/end color selection for gradients.

**Verify:** Launch → Creator → set dot color to a linear gradient → preview shows gradient-colored dots.

## ~~Task 12: Logo Embedding~~ ✅
Implement logo rendering in `QRRenderer`: scale the logo image, clip to configured shape (circle, rounded rect, square), add padding, draw centered over QR code. Skip data modules behind the logo area. Auto-set error correction to High when a logo is present. Replace the "Logo" placeholder in Creator with: image picker (via photo picker intent), size slider, padding slider, shape selector. Use Coil to load the selected image URI.

**Verify:** Launch → Creator → pick a logo image → logo appears centered on the QR code with the selected shape/size.

## ~~Task 13: Error Correction & Quiet Zone Controls~~ ✅
Replace the "Error Correction" placeholder with a segmented button (L / M / Q / H). When a logo is embedded, auto-lock to H and show a note. Replace the "Quiet Zone" placeholder with a slider (0–4 module units). Both update `QRStyle` and re-render the preview.

**Verify:** Launch → Creator → change error correction → QR density changes. Adjust quiet zone → margin changes.

## ~~Task 14: Template Picker in Creator~~ ✅
Replace the "Templates" placeholder in Creator with a horizontally scrollable row. Each template shown as a small QR code thumbnail rendered with its `QRStyle`. Predefined templates from `PredefinedTemplates` are displayed. Tapping a template replaces all style fields in the ViewModel.

**Verify:** Launch → Creator → scroll template row → tap "Neon" → preview switches to gradient dots on dark background. Tap "Classic" → back to plain black-on-white.

## ~~Task 15: Database — Room Setup & QRCode Persistence~~ ✅
Create `database/QRQanvasDatabase.kt` (Room database), `database/entity/QRCodeEntity.kt`, `database/dao/QRCodeDao.kt` (insert, getAll as Flow, getById, update, delete), `database/converter/QRStyleConverter.kt` (serialize `QRStyle` to/from JSON via kotlinx.serialization). Create `repository/QRCodeRepository.kt` wrapping the DAO. Wire into `QRQanvasModule`.

**Verify:** App compiles and launches. No visible change yet.

## ~~Task 16: Creator Screen — Save Action~~ ✅
Wire the Save button in Creator's top bar. On tap, persist the current `QRContent` + `QRStyle` as a `QRCodeEntity` via `QRCodeRepository`. Auto-generate a title from the content (e.g. truncated URL or "Wi-Fi: SSID"). After saving, navigate back. If editing an existing QR code (passed via `qrCodeId` route arg), update instead of insert and pre-fill Creator on entry.

**Verify:** Launch → Creator → enter content → customize → tap Save → navigates back.

## ~~Task 17: Home Screen — Saved QR Codes Grid~~ ✅
Add `HomeVM`, `HomeUiState`, `HomeUiEvent`. Display saved QR codes from `QRCodeRepository.getAll()` in a `LazyVerticalGrid`. Each card shows a small rendered QR preview thumbnail (on-the-fly at ~100px), the title, and a content type icon. Show the empty state when no codes exist. Tapping a card navigates to Detail. Wire `HomeVM` into `QRQanvasModule`.

**Verify:** Launch → see empty state → tap FAB → create & save → return to Home → saved code appears in grid.

## ~~Task 18: Detail Screen — View & Manage~~ ✅
Add `DetailVM`, `DetailUiState`, `DetailUiEvent`. Load `QRCodeEntity` by ID and render a large QR preview. Show content summary (type + text) and timestamps. Wire Edit (navigates to Creator with `qrCodeId`), Delete (confirmation dialog → navigate back to Home). Wire `DetailVM` into `QRQanvasModule`.

**Verify:** Tap a saved QR → Detail with full preview → Edit → Creator pre-filled → Save → back to Detail updated. Delete → confirm → back to Home, card gone.

## ~~Task 19: Export & Share~~ ✅
Create `core/QRExporter.kt` — takes `Bitmap`, exports as PNG to cache dir, returns `Uri` via `FileProvider`. Add `FileProvider` in manifest and `res/xml/file_paths.xml`. Add Export/Share buttons to Creator and Detail. "Export" saves to Downloads via `MediaStore`. "Share" opens Android share sheet.

**Verify:** Detail → Share → share sheet with QR image. Export → file saved confirmation.

## ~~Task 20: Scanner — Camera Preview & Permission~~ ✅
Wire the Scanner screen skeleton with CameraX. Handle camera permission with Accompanist `rememberPermissionState` (request on entry, show rationale if denied). Replace the camera placeholder with a live CameraX preview. Wire flashlight toggle. Add navigation from Home.

**Verify:** Launch → tap Scan → permission prompt → grant → live camera preview with viewfinder overlay → toggle flashlight.

## ~~Task 21: Scanner — QR Detection & Results~~ ✅
Integrate ML Kit `BarcodeScanner` with CameraX `ImageAnalysis` (or `MlKitAnalyzer`). Create `core/QRContentParser.kt` to parse raw scanned strings into `QRContent`. On successful scan: show a bottom sheet with decoded content, type icon, and action buttons (Open URL, Copy, Share). Add "Scan from Gallery" button.

**Verify:** Scan → point at QR → bottom sheet with decoded content → Copy → content copied. Pick gallery image → same result.

## ~~Task 22: Scan History~~ ✅
Create `database/entity/ScanHistoryEntity.kt`, `database/dao/ScanHistoryDao.kt`, `repository/ScanHistoryRepository.kt`. Auto-save each scan from Scanner. Add `HistoryVM`, `HistoryUiState`, `HistoryUiEvent` to the History screen skeleton. Populate chronological list with type icon, truncated value, timestamp. Tap to expand with actions. Swipe-to-delete. Wire "Clear All" overflow menu. Wire into `QRQanvasModule`.

**Verify:** Scan a few QR codes → History → see all scans → tap for actions → swipe to delete → Clear All.

## ~~Task 23: User Templates~~ ✅
Create `database/entity/TemplateEntity.kt`, `database/dao/TemplateDao.kt`, `repository/TemplateRepository.kt` (merges predefined + user templates into unified `Template` list). Add "Save as Template" action in Creator (overflow menu) — prompts for name, saves current `QRStyle`. Update the template picker row to show user templates after predefined. Wire the Templates screen skeleton with actual data (rename, delete user templates). Wire into `QRQanvasModule`.

**Verify:** Creator → customize → Save as Template → name it → appears in picker. Templates screen → see it → rename → delete.