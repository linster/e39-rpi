# Why the map was slow (November 2026)

Opening the map took 26.6 s in QEMU (qemuarm64) the first time and 12.5 s
afterwards, and was slow on the Pi too, with every tile already in the
cache: the time went to the CPU, not to downloads.

## Measured

`MapPerfBenchmark` (desktopTest, opt-in) on a laptop, with the 81 tiles
the QEMU guest had cached:

| | before | after |
|---|---|---|
| new DaggerApplicationComponent + one setting | 10.9 ms | (not done any more) |
| the same setting from one component | 0.002 ms | 0.002 ms |
| cached tile lookup | 0.08 ms | 0.05 ms |
| MapViewer first frame, 400x234 | 1,422 ms | 128 ms (incl. JIT warm-up) |
| MapViewer first frame, 800x468 | 4,038 ms | 37 ms |
| MapViewer total with the tiles loaded, 400x234 | 2,139 ms | 703 ms |
| MapViewer total, 800x468 | 5,587 ms | 308 ms |

In QEMU, from the screen switching to the map until it listens to the
knob: **26.6 s -> 1.2 s** (first opening), Show on map with 50 markers
12.5 s -> 1.75 s.

## Causes

1. **A Dagger component per tile, three times.** `TileView` called
   `DaggerApplicationComponent.create()` for its debug flag (a default
   parameter), for the tile source, and to load the tile. A new component
   has new singletons: its `ConfigurationStorage` re-reads and parses the
   settings files (HOCON), its `TileServerImageCache` scans the cache
   folder. ~11 ms each on a laptop, twice per tile during composition, on
   the UI thread before the first frame; many times that on the Pi's and
   QEMU's CPUs.
2. **Far more tiles than shown.** The grid was six viewport widths of
   tiles each way, square: 81 tiles for the 400x234 HMI (QEMU and the
   Pi's VGA output), 361 at 800x468, where about 2x1 (4x2) are on the
   screen.
3. **Loading on the UI thread.** Cache lookups, file reads and PNG
   decoding ran in `LaunchedEffect`s on the UI dispatcher.

Also found: the marker overlay built a Dagger component per marker per
recomposition to log; `RawTileGrid` built two lists of every tile number
of the zoom level (8,192 entries at zoom 13) on every composition.

## Fixes

- `LocalMapTileServices` (tile fetcher, settings, logger) provided once
  per window by `MenuWindow`; `RawTileGrid` reads the settings once and
  passes them to every `TileView`.
- The grid is the viewport plus one tile on each side: 25 tiles at
  400x234, 35 at 800x468. Panning moves the centre and rebuilds the grid
  around it.
- Tiles load on `Dispatchers.IO`, keyed by x/y (a reused slot doesn't show
  the previous tile after a pan).
- Exact Web Mercator for overlays and centring (`latLngToTile`,
  `latLngToPixels`, `pixelsToLatLng`): the centre is scrolled to the middle
  of the viewport by its pixel (it was a fraction of the scroll range, only
  exact for a point in the middle of a big grid), and the reported centre is
  the inverse. Markers and the centre now line up exactly with the tiles
  (`MapProjectionTest`).

## Running the benchmark

It needs a scratch /var/lib/e39 (the theme and the Dagger component read
the HMI's settings there) and a home folder with cached tiles:

    mkdir -p /tmp/maphome/.e39 && ssh -p 2222 root@localhost 'tar -C /home/hmi/.e39 -cf - mapTiles' | tar -C /tmp/maphome/.e39 -xf -
    unshare -rm sh -c 'mount -t tmpfs tmpfs /var/lib/e39 &&
        cp <e39-rpi-yocto>/layers/meta-e39rpi-gui/recipes-hmi/e39rpi-hmi/files/qemuall/e39-config/*.conf /var/lib/e39/ &&
        GRADLE_USER_HOME=$HOME/.gradle JAVA_TOOL_OPTIONS="-Duser.home=/tmp/maphome -Dskiko.data.path=$HOME/.skiko" \
        E39_MAP_BENCH=1 ./gradlew --no-daemon :composeApp:desktopTest --tests "*MapPerfBenchmark*"'

The numbers are in the test's output (build/test-results/desktopTest).
