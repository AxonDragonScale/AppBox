# QRQanvas — TODO

## ~~Task 1: App Foundation~~ ✅
~~Set up the app skeleton: `QRQanvasApplication` (Koin init), `QRQanvasModule` (empty), update `QRQanvasActivity` (splash screen, edge-to-edge), rename theme to `QRQanvasTheme` with proper system bar handling, update `AndroidManifest.xml` (Application class, camera permission), create `Route.kt` (all routes), and `QRQanvasApp.kt` (NavHost shell with placeholder text for each destination).~~

## ~~Task 2: Data Models & Enums~~ ✅
~~Create all model classes: `QRContent` (sealed class), `QRStyle` (data class), `DotStyle` (enum), `EyeStyle` (enum), `ColorConfig` (sealed class), `LogoConfig` (data class), `ExportFormat` (enum), `ScanResult` (data class), and `PredefinedTemplates` (object with 8 constant QRStyle instances).~~

## Task 3: QR Generation & Rendering Engine
Implement `QRGenerator` (ZXing wrapper: QRContent → BitMatrix) and `QRRenderer` (BitMatrix + QRStyle → Bitmap via Canvas, with finder pattern detection and per-module custom shape drawing). Add `QRContentParser` for parsing raw scanned strings into `QRContent`.

## Task 4: Database Layer
Create Room database (`QRQanvasDatabase`), entities (`QRCodeEntity`, `ScanHistoryEntity`, `TemplateEntity`), DAOs (`QRCodeDao`, `ScanHistoryDao`, `TemplateDao`), `QRStyleConverter`, and repositories (`QRCodeRepository`, `ScanHistoryRepository`, `TemplateRepository`, `AppPrefsRepository`). Wire everything into `QRQanvasModule`.

## Task 5: Creator Screen
Build the Creator screen with live QR preview (Compose Canvas), content type input (text/URL/WiFi/etc.), style customization panel (dot style, dot color, eye style, eye color, background, logo, error correction, quiet zone), template picker row, save to database, and save-as-template action.