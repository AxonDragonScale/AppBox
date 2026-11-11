# Wallmatic — wallpaper library

Scope: the whole Wallmatic Android app UI, designed in `apps/wallmatic/wallmatic.fig`.
Visitor mode: Operate.

## Audience and task

People who keep a personal wallpaper collection and want it to rotate on a schedule. They open
the app in short bursts to see what is showing, what is next, and to change the album or interval.
The interface must answer those three questions in one glance.

## Constraints

- Material 3 structure, navigation and controls; Android edge-to-edge, system Back, 48 dp targets.
- Local-only albums and folders; one shared interval for Home and Lock; separate albums and
  auto-cycle per target; the mirror-home toggle lives in Settings.
- Chakra Petch is the established brand face; indigo `#4B46C0` is the launcher signal.
- Light edition is the shipped design; the night edition is defined but not screen-built.

## Chosen direction

The wallpaper is an image in a library; the app is the index, caption and schedule.

## Memorable moment

The schedule picker: seven ticks from 15m to 24h, a moving marker, the value stamped beside it.
It replaces the chip row and is the signature interaction. Tabs and segmented controls share one
language: a 1 px ruled track and a 3 px ink underline under the active label.

## Unresolved

- Night-edition screens.
- The exact stepper increments for `CUSTOM` (design shows 1 h 30 min).
- Whether the masthead stays on every tab or only Home.

## Direction contract

THESIS: A wallpaper library. The image is the product; the app is the index, caption and
schedule. It refuses the M3 dashboard of stacked cards and the floating pill navigation bar.

OWN-WORLD: Paper `#F2EFE6`, ink `#141414`, indigo `#4B46C0`; Chakra Petch display and labels,
Roboto body; 1 px rules, square images, 0-radius buttons, switches, radios, dialogs and sheets;
a centred ink square above the active tab or segment.

STORY: The user sees the current image, the next update time and the shared interval at a glance,
then changes the album or interval without leaving the screen. Home and Lock are targets, not two
duplicated cards; mirror is a Settings decision, and the target tabs disappear when it is on.

FIRST VIEWPORT: status bar, masthead and rule; device-aspect 180 × 400 preview; current and next
times in the right column; shared interval picker; one filled change action; text-only 64 dp tab
bar with a centred ink square above the active destination. No mirror row, no page title.

FORM: wallpaper library / catalogue, candidate 5 of the grounded list. seed:c7b982c05662242e

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the
verdict, DESIGN.md, and every shipping raster carrying its provenance.
