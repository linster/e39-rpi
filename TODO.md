# TODO

Bugs and loose ends found while building Places Nearby, the map version on
the About screen and guidance instructions (branch `routing-hmi`). The
device-side items (Yocto layers) are in e39-rpi-yocto's TODO.md.

## Bugs

- [x] **Map screen handled every knob event three times** (a collector
      launched in the composable body per recomposition); now one
      `LaunchedEffect`. Fixed and tested in QEMU.
- [x] **Map markers weren't drawn** (overlays placed with a density of 2
      on a density-1 screen: off the screen); fixed and tested in QEMU
      (docs/guidance/places/06-show-on-map-with-markers.png).
- [x] **IBUS reader couldn't reopen the port**; now reopens through the
      provider (a new SerialPort, the old one closed). Tested in QEMU:
      with gpsd holding /dev/ttyUSB0 the HMI retries, and takes the port
      and the knob works as soon as gpsd lets go, without a restart.
- [ ] Map markers are 32 px circles (the user POIs' style): 50 search
      results crowd the 400x234 map. Smaller ones for search results?
- [ ] The map takes 15+ s to open in QEMU (knob events before it's
      composed are lost); check how long on the Pi.
- [ ] **No theme.conf: the theme crashes.** `ThemeWrapper.defaultTheme`
      fails with "Invalid theme name: BMW Blue DoubledPixels" when
      /var/lib/e39/theme.conf is missing (images ship one, so only fresh
      laptop setups and tests hit it).
- [ ] **Knob turns are dropped while the HMI redraws** (QEMU: turns sent
      0.25 s apart while a screen composes are lost). Check on the Pi.
- [ ] The guidance side panel, the guidance notifications and the
      guidance debug window haven't been seen in the running HMI yet (only
      rendered off-screen and unit/integration tested).

## Fixed on this branch (for the record)

- Uber jar dropped `META-INF/services` providers (first library's copy
  wins): gRPC had no DNS name resolver or pick_first balancer, so every
  maps backend call failed in the image's jar ("Address types of
  NameResolver 'unix' ... not supported"). `packageUberJarForCurrentOS`
  now merges them. Also affects the calendar's PIM client.
- A channel gRPC can't build threw an `IllegalArgumentException` past the
  maps repositories' error handling (uncaught in a coroutine); it's a
  `MapsBackendException` now.
- version.conf was rewritten whole from the HMI's start-up copy on every
  fwHash change, undoing what others wrote (deploy tools: hmiHash; the
  map installer: mapVersion); now only the changed key is set on the file.
- `desktopTest` had no JUnit (the tests didn't compile).

## Later

- Guidance: routes from `RouteService` in the guidance setup screens (a
  RouteCalculator using RemoteRouteRepository), persist the session's
  instructions, and position from gpsd into `GuidanceService.updatePosition`.
- `TrafficArrows.Roundabouts.Roundabout`: a drawn roundabout with the exit
  to take (the side panel shows "Take exit N" and a generic picture).
