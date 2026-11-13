# TODO

Bugs and loose ends found while building Places Nearby, the map version on
the About screen and guidance instructions (branch `routing-hmi`). The
device-side items (Yocto layers) are in e39-rpi-yocto's TODO.md.

## Bugs

- [ ] **Map screen handles every knob event three times.** `MapScreen`
      starts `knobListenerService.knobTurnEvents().collect` with
      `scope.launch` directly in the composable body, so every
      recomposition adds a collector (the log shows "collect turn ..."
      three times per event). A press toggles the overlay state several
      times; menus open on one press and close or pick "Zoom" on the next.
      Fix: collect in a `LaunchedEffect(Unit)`.
- [ ] **Map markers aren't drawn.** `MapScreenParameters.markers` (Places
      Nearby's Show on map: the results as orange circles) reach
      `MapScreen` ("POI LIST" in the log lists them) but nothing appears
      on the map, and `PoiOverlayLayer` logs nothing. Not yet known whether
      the user's own POIs are drawn either. Also: `PoiOverlayLayer` creates
      a whole `DaggerApplicationComponent` per POI per recomposition just
      to log (slow with 50 markers).
- [ ] **IBUS reader can't reopen the port.** `BlockingJSerialCommsReader`
      used to crash for good when the port reported -1 (`ByteArray(-1)`);
      it now waits and retries, but retries `openPort()` on its old
      `SerialPort` object, which keeps failing (the provider's
      `setupSerialPort()` makes a new one the reader never uses). After a
      USB adapter glitch IBUS only comes back with an HMI restart. Fix:
      reopen through the provider and use the new port.
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
