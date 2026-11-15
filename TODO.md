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
- [x] **The map was slow** (26.6 s to open in QEMU, slow on the Pi): a
      Dagger component per tile (three), 81-361 tiles for a 2x1 view,
      loading on the UI thread. Now 1.2 s in QEMU; see
      docs/map-performance.md. Check the time on the Pi.
- [ ] **No theme.conf: the theme crashes.** `ThemeWrapper.defaultTheme`
      fails with "Invalid theme name: BMW Blue DoubledPixels" when
      /var/lib/e39/theme.conf is missing (images ship one, so only fresh
      laptop setups and tests hit it).
- [ ] **Knob turns are dropped while the HMI redraws** (QEMU: turns sent
      0.25 s apart while a screen composes are lost). Check on the Pi.
- [ ] The guidance side panel, the guidance notifications and the
      guidance debug window haven't been seen in the running HMI yet (only
      rendered off-screen and unit/integration tested).

- [x] **`EditConnectionScreen` wasn't registered** in `Navigator.kt`
      (network setup; it's `@AutoDiscover`, so the HMI Navigator Debug
      window lists it as unregistered, and navigating to it does nothing).
      Found by uitest/ `test_00_screens_registered` (known there; register
      it, then drop it from `KNOWN_UNREGISTERED`).

- [ ] **SmoothScroll doesn't scroll to a row behind the footer.** Its
      page size is its `BoxWithConstraints` height, which reaches under
      the window's footer (the clock bar, drawn over the content): a row
      that ends under the footer counts as on screen, so selecting it
      doesn't scroll, and half of it (and its knob frame) stays hidden.
      Seen on the contact details screen (the e-mail row; found by the UI
      tests). Fix: take the footer's height off the viewport (or pad the
      list's bottom by it).

## KnobListenerService: dropped knob events

Turns and presses get lost while screens change (QEMU: turns 0.25 s
apart while a screen composes; see the bug above). Knowing when a screen
has finished drawing is hard and isn't needed: the events can wait until
someone is listening again. Where they get lost:

- [ ] **No one subscribed = event gone.** `ApplicationModule`'s
      `inputEventsWriter` is a `MutableSharedFlow()` (no replay, no
      buffer). `emit` with zero subscribers returns at once and the event
      is dropped; `extraBufferCapacity` doesn't help (it only buffers for
      slow subscribers). Every screen change has such a gap: the old
      screen's collectors are cancelled before the new screen's
      `LaunchedEffect`s subscribe.
- [ ] **`takeWhile(listenerEnabled)` eats an event.** In
      `KnobListenerService.knobTurnEvents`, the first event after
      `disableListener()` fails the predicate: it's consumed (lost) and
      the flow completes. The next subscription only starts when the
      composable recomposes (`KnobObserverBuilderState.setupListener`'s
      `LaunchedEffect(listenerEnabled)`), and events in between are lost
      too (the gap above). Filtering instead of `takeWhile`, with the
      collector kept, would avoid both.
- [ ] **Resubscribing per screen state.** Each `KnobObserverBuilderState`
      (and every `key()` that recreates one, e.g. a SmoothScroll rebuilt
      when its row count changes) subscribes anew, so each rebuild opens a
      gap.
- [ ] **A possible design: one long-lived dispatcher.** An app-scoped
      collector of INPUT_EVENTS, started once, routes events to the
      current owner (main, main aux or modal listener, whichever is
      enabled, then its registered handler). Events that arrive while
      nobody is registered are queued briefly (e.g. up to 1 s, in order)
      and handed to the next handler that registers, then dropped if
      stale. "A handler registered" stands in for "the screen is ready";
      no drawing detection needed. `KnobListenerService` keeps its API
      (`knobTurnEvents()`, `enableListener()`), so screens don't change.
- [ ] **Measure first.** Count drops: in the writer path,
      `inputEventsWriter.subscriptionCount.value == 0` at emit time means
      the event is lost; log it with the event and the current screen. The
      UI test probe (uitest/) can report the count, and the runner's
      `--input ibus` mode (real IBus packets, short gaps) can then show
      whether a fix works.
- [ ] **Tests.** A unit test that emits events while listeners are
      enabled/disabled and screens' collectors come and go, asserting
      every event is delivered exactly once, in order.
- [ ] Not a drop: the Griffin Powermate reader skips turns on purpose
      (its "desensitize" setting).
- [x] **SmoothScroll lost the knob's item when it laid out again** (found
      by the UI tests, uitest/ test_14). `SmoothScroll` subcomposes each row
      with `slotId = index + knobState.getMaxAllocatedIndex()`, read at every
      measure. After the first layout the maximum includes the rows
      themselves, so any later measure (a notification, a count or label
      changing, a row changing size) gets new slot IDs: every row is
      composed anew and allocates new knob IDs, and the old ones are
      deallocated. `AllocatedIdManager.removeId` sets the current ID to the
      lowest allocated ID *before* removing the one asked for, so after the
      old rows go one by one the current ID is one that no longer exists:
      nothing is highlighted, and the next turn lands on the first row. In
      the car: every notification on a list screen leaves the knob without
      an item. Fixes: take the toolbar offset once (or key rows by their own
      index and map positions by allocated ID), and in `removeId` remove
      first, then move the current ID only if it was the removed one (to its
      neighbour). Workaround in the mail screens until then: `key()` on the
      rows' content, so a change rebuilds the list (the knob goes back to the
      first row) — doesn't help when a notification arrives.
      Fixed: SmoothScroll uses the row's index as its slot, CallWhen calls
      the latest action, removeId moves the pointer only if its item went;
      the mail screens' workaround is gone.
- [x] **Worse: after such a re-layout a press could do another row's
      action.** The new slot IDs overlap the old ones, so an old row's slot
      is reused for another row: e.g. slot 7 (the compose screen's "Send")
      becomes row 0 ("Cancel"). The reused slot keeps its remembered knob ID
      (`KnobObserverBuilder`'s `remember(Unit) { allocateIndex() }`) and
      `CallWhen` keeps the click handler it registered for that ID (its
      `DisposableEffect` is keyed on the ID only, the lambda isn't updated).
      The screen highlights "Cancel"; pressing runs "Send". Reproduced by
      uitest/ `test_10_cancel` (compose: quick reply, Cancel, Keep Writing,
      one turn, press: "Not sent: Add a recipient first"). Fixes: in
      SmoothScroll `subcompose(slotId = index)` (the toolbar offset only for
      mapping rows to knob IDs, taken from the IDs the rows got); in
      `CallWhen`, `rememberUpdatedState(call)` and register a lambda that
      calls its current value.
- [ ] **The car platform's services have their own Dagger component**
      (`ConfigurablePlatform` builds `DaggerApplicationComponent.create()`):
      every `@ApplicationScope` object exists twice, once for the screens
      and once for the services, each with its own `ConfigurationStorage`
      (config.conf read into memory twice; a change on screen isn't seen by
      the services). `NotificationHub` is static for this reason; the mail
      app's sample data and notification settings now are too. Other
      services that read settings changed on screen may have the same
      problem.

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
