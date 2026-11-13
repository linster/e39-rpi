# Guidance and places: pictures

Screenshots and renders made while building turn-by-turn guidance
instructions and Places Nearby (branch `routing-hmi`, November 2026), kept
for writing the documentation in `~/e39-rpi-book/`.

## icons/

The guidance instruction pictures (`composeApp/src/commonMain/composeResources/drawable/guidance-*.svg`),
rendered at 96 px on the HMI's green; `guidance-icons-sheet.png` has all 29
with their names. Same style as the notification icons (Feather: 24x24,
white 1.5 px strokes); the road to take is solid, other roads faint. Which
Valhalla maneuver gets which picture: `gui/map/guidance/GuidanceIcons.kt`.

## side-panel/

The guidance side panel (`GuidanceSidePanel.Panel`, shown by
`SideBarGuidanceInstructionConsumer`), rendered off-screen by
`GuidanceSidePanelRender` (desktopTest) for five instructions: a right
turn at 350 m, a roundabout (2nd exit), a highway exit with a long street
name, a turn "Now", and the destination.

- `normal-*.png`: 160x200, the side split (40 %) of the 400x234 HMI window
  (QEMU), BMW Blue theme.
- `doubled-*.png`: 320x400, the pixel-doubled theme.
- `sheet-normal-400x234.png`: the five side by side (the final layout).
- `sheet-*-before-fix.png`: the first layout: the roundabout's exit cut
  off at the bottom, the long street name pushing the instruction to one
  line. Fixed by showing the exit under the distance and the street on one
  line.

## places/

Places Nearby in QEMU (qemuarm64, the Ottawa map), 3x:

0. `00-map-menu.png`: the map's menu (knob press on the map), on POIs.
1. `01-map-poi-menu.png`: POIs -> the new "Places Nearby..." entry.
2. `02-places-nearby.png`: categories near the map's centre, and search
   by name.
3. `03-fuel-near-map-centre.png`: fuel stations from the map data, nearest
   first, with distance and direction.
4. `04-place-side-pane.png`: a station's side pane: distance from the map
   centre, category, address, hours, website; Show on map, Save as POI
   (then "Save to address book").
5. `05-show-on-map.png`: Show on map: the map centred on the station. (The
   other results should be drawn as orange circles; they aren't yet, see
   TODO.md.)

## qemu-raw/

Every screenshot taken in QEMU while testing, in order of the letters
(`scripts/hmi.py` in e39-rpi-yocto takes them: the 400x234 screen and a
3x copy). Many show the HMI catching up (QEMU redraws slowly) or the map
screen in an unexpected state (it handles each knob event three times,
see TODO.md); kept as a record.

- `s*`: first run: the main menu while the IBUS reader was dead (the
  gpsd and IBUS bugs in TODO.md).
- `t*`, `u*`: the knob working again; the map and its menu.
- `d*`-`h*`: getting to the POI menu (turns dropped while the HMI redraws).
- `i*`: the Places Nearby walk-through (copied to places/).
