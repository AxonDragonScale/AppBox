# Wallmatic - TODO List

## 🐛 Code Quality & Bug Fixes

### High Priority

- [ ] **Error Handling**
  - [x] Add try-catch blocks in `WallpaperUpdater.setWallpaper()` for IOException
  - [ ] Add proper error reporting to UI layer (use sealed classes for states)
  - [x] Handle edge case when `getWhitelistedWallpapers()` returns empty list (avoid crash on `.random()`) — `getRandomWallpaperOrNull()`; the screen is skipped, not failed
  - [x] Add error recovery mechanism when wallpaper setting fails — `UpdateCoordinator` arms a bounded retry (1–5 min) instead of dead-ending

- [ ] **Resource Management**
  - [ ] Audit and fix potential URI permission leaks
  - [ ] Add cleanup for orphaned wallpaper entries in database
  - [ ] Implement proper cleanup when folders/albums are deleted

- [ ] **Performance Issues**
  - [ ] Fix N+1 query problem in `WallmaticRepository.getAllFullAlbums()` (use batch queries or Room relations)
  - [ ] Optimize `SyncManager.syncAlbums()` with batch operations
  - [ ] Add pagination for large wallpaper collections
  - [ ] Cache frequently accessed data (current wallpaper, active album)
  - [ ] Use LazyColumn/Grid more efficiently in UI

### Medium Priority

- [ ] **Database Design**
  - [ ] Refactor to use proper Room relations instead of storing ID lists
  - [ ] Create junction tables for many-to-many relationships (Album-Wallpaper, Album-Folder)
  - [ ] Add database migration strategy
  - [ ] Add indices for frequently queried columns
  - [ ] Consider adding `created_at` and `updated_at` timestamps

- [ ] **Code Organization**
  - [ ] Remove empty `FolderManager` class or implement actual functionality
  - [ ] Remove commented-out code in `WallmaticScheduler`
  - [ ] Extract magic numbers to constants (ALARM_REQUEST_CODE, grid sizes, etc.)
  - [ ] Move hardcoded intervals to configuration
  - [ ] Consolidate extension functions into fewer files

- [ ] **Null Safety**
  - [ ] Review all `.firstOrNull()` calls and add proper null handling
  - [ ] Add null checks before accessing config values
  - [ ] Use Elvis operator consistently for default values

### Low Priority

- [ ] **Documentation**
  - [ ] Add KDoc comments to all public functions
  - [ ] Document complex algorithms (sync logic, wallpaper selection)
  - [ ] Add inline comments for non-obvious code
  - [ ] Create architecture documentation
  - [ ] Document proto buffer schema changes

- [ ] **Code Style**
  - [ ] Ensure consistent naming conventions
  - [ ] Use sealed classes for UI events/effects consistently

## ✨ Feature Enhancements

### High Priority Features

- [ ] **Wallpaper Selection Modes**
  - [ ] Add sequential/ordered mode (not just random)
  - [ ] Add shuffle mode that doesn't repeat until all are shown
  - [ ] Add favorites/star system for wallpapers
  - [ ] Show preview of next wallpaper
  - [ ] Add "skip to next" manual control

- [ ] **Error Recovery & User Feedback**
  - [ ] Display error messages when operations fail
  - [ ] Add retry mechanism for failed operations
  - [ ] Show progress indicator for long operations

- [ ] **Search & Filter**
  - [ ] Filter wallpapers by folder
  - [ ] Filter by blacklist status
  - [ ] Sort options (date added, name, size)

### Medium Priority Features

- [ ] **Advanced Scheduling**
  - [ ] Add time-of-day specific schedules (e.g., morning, afternoon, evening)
  - [ ] Different schedules for weekdays vs weekends
  - [ ] Multiple schedules per screen
  - [ ] Calendar-based scheduling
  - [ ] Smart scheduling based on battery level

- [ ] **Bulk Operations**
  - [ ] Multi-select wallpapers for blacklist/whitelist
  - [ ] Bulk delete wallpapers
  - [ ] Move multiple wallpapers between albums
  - [ ] Batch apply settings

- [ ] **Album Management**
  - [ ] Reorder wallpapers/folders within album
  - [ ] Duplicate albums
  - [ ] Merge albums
  - [ ] Album templates
  - [ ] Nested albums/folders
  - [ ] Album sharing/export

- [ ] **Wallpaper Customization**
  - [ ] Crop/adjust before applying
  - [ ] Zoom/fit options (center, fill, fit, stretch)
  - [ ] Apply filters/effects
  - [ ] Blur background
  - [ ] Add vignette or overlay

### Low Priority Features

- [ ] **Notifications**
  - [ ] Optional notification when wallpaper changes
  - [ ] Notification actions (skip, undo, favorite)

- [ ] **Import Sources**
  - [ ] Import from URLs
  - [ ] Import from cloud storage
  - [ ] Integration with wallpaper websites/APIs
  - [ ] Reddit integration
  - [ ] Unsplash integration

- [ ] **Accessibility**
  - [ ] Add haptic feedback consistently

## 🚀 Advanced/Future Features

- [ ] **Smart Features**
  - [ ] Automatic color scheme detection and matching

- [ ] **Multi-Device Support**
  - [ ] Tablet-specific layouts
  - [ ] Foldable device support
  - [ ] Different configurations for different screen sizes

- [ ] **Live Wallpapers**
  - [ ] Support for video wallpapers
  - [ ] Animated GIF support
  - [ ] Slideshow mode
  - [ ] Parallax effects

## 🔧 Technical Debt

- [x] Doze-safe scheduling — inexact `setAndAllowWhileIdle` tick plus a `WorkManager` fallback (see `docs/apps/wallmatic/background-services.md`)
- [ ] Optimize ProtoDataStore usage
- [ ] Review and optimize Coil image loading configuration

## 📋 Existing TODOs in Code

From code analysis:
- [x] `TickReceiver` - BroadcastReceiver 10s limit handled: a provisional tick is armed before `goAsync()` and the apply path never touches SAF
- [ ] `AlbumManager.kt:L55` - Verify wallpaper deduplication logic
- [ ] `WallmaticApp.kt:L34` - Implement deep linking to tabs
- [ ] `WallmaticApp.kt:L35` - Create composables/NavGraphs in screen packages
- [ ] `FolderVM.kt:L60` - Update folder cover when wallpaper is blacklisted
- [ ] `FolderVM.kt:L69` - Update folder cover when wallpaper is whitelisted
- [ ] `Home.kt:L159` - Implement proper loading state UI
- [ ] `Home.kt:L312` - Extract preview controls and consider hiding when autoCycle is disabled

## 🎨 UI/UX Improvements

- [x] Add empty states for albums, folders, wallpapers
- [x] Improve loading states across all screens
- [ ] Add animations for screen transitions
- [ ] Improve bottom sheet designs
- [ ] Add confirmation dialogs for destructive actions

## 📱 Platform Features

- [ ] Optimize for large screens and tablets

---

## Priority Legend
- **High Priority**: Critical for stability, user experience, or commonly requested features
- **Medium Priority**: Important but not critical, quality of life improvements
- **Low Priority**: Nice to have, future enhancements

## Notes
- Review and update this list regularly
- Mark items as complete with date
- Add new items as they are discovered
- Link to related issues/PRs when implemented
