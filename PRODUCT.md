# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

People who keep a personal wallpaper collection on their phone and want it to rotate on a
schedule without tending it. They set up an album or folder once, choose home and lock behaviour,
then return occasionally to check what is showing, change the album, or adjust the interval.
They are not power users of automation tools; the app should feel like a small, reliable utility.

## Product Purpose

Wallmatic cycles user-created wallpaper albums on a schedule, with separate home and lock screen
configurations, quick settings tiles, and a tick that survives reboots. Success is that the right
wallpaper is already on the phone when the user looks, and changing the schedule takes seconds.

## Positioning

Album- and folder-based rotation with independent home and lock configuration, driven by one
AlarmManager tick plus a WorkManager fallback. It works entirely from the user's own images; there
is no catalogue, account, or cloud service.

## Operating Context

- Android 12+ phone, edge-to-edge, light and dark themes, dynamic colour opt-in.
- Setup happens with the phone in hand, often one-handed; the user may be in a dim room and care
  about the wallpaper more than the chrome.
- Wallpapers arrive through SAF folders and multi-image picks; albums and folders are local.
- The app is checked in short bursts: open, glance at the current/next wallpaper, change something,
  leave. Background work is invisible unless it fails.
- Quick settings tiles and the home-screen widget-adjacent tiles are part of the product surface.

## Capabilities and Constraints

- Album CRUD, folder scanning, per-image blacklist, grid size preference, theme mode.
- Home and lock configs, mirror-home toggle, one shared interval, last/next update timestamps.
- Material 3 components and Android navigation conventions; Compose; 48 dp touch targets;
  edge-to-edge insets; system Back; predictive Back.
- Local-only persistence (Room + Proto DataStore); no network account.
- Design is currently the source of truth in `apps/wallmatic/wallmatic.fig`; code follows later.

## Brand Commitments

- Name: Wallmatic.
- Existing launcher identity: indigo `#4B46C0` on white; splash `#4B46C0`.
- Chakra Petch is the established display/UI face; a redesign may rebalance it but should keep a
  recognizable connection to it.
- Material 3 remains the interaction rulebook; brand expresses through colour roles, type scale,
  shape, and motion rather than through non-Material navigation.

## Evidence on Hand

- Running debug build and device screenshots of every screen (Home, scrolled Home, Albums, Album,
  Folder, Settings, Select Album sheet, album-name dialog, context menu, wallpaper preview).
- Real wallpaper imagery from the device, used as design material.
- Source of truth: `apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/ui/`.
- No marketing claims, testimonials, or commercial proof exist; do not invent them.

## Product Principles

1. The wallpaper is the product; the interface should get out of its way.
2. Reliability is visible: current and next update times are never ambiguous.
3. One glance should answer "what is showing, and when does it change?".
4. The user's own images carry the colour; the app's chrome stays quiet.
5. Native Android behaviour is part of the brand, not a compromise.

## Accessibility & Inclusion

- 48 dp minimum touch targets; text follows the system font scale.
- Light and dark schemes are first-class; contrast meets Material guidance.
- Motion respects the system Remove animations setting.
