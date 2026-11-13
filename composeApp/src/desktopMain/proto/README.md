# Maps backend API (gRPC)

`e39maps_api/v1/*.proto` is a **copy** of the maps backend's API
(e39-rpi-maps, `proto/e39maps_api/v1/`); `e39maps_api/SOURCE` says which
commit. Change the API there, then copy and regenerate here:

```sh
./makeMapsProtos.sh                                # from ~/e39-rpi-maps
E39_MAPS_REPO=/path/to/e39-rpi-maps ./makeMapsProtos.sh
./makeMapsProtos.sh --no-copy                      # regenerate from these copies
```

The generated code (messages, Kotlin DSL, grpc-java and grpc-kotlin stubs) is
committed in `../java/ca/stefanm/e39maps/v1/`, like the Pico protos
(`makeProtos.sh`); Gradle doesn't run protoc. The script downloads protoc and
the plugins from Maven Central into `~/.cache/e39-rpi-protoc`; their versions
match the libraries in `composeApp/build.gradle.kts` (protobuf-java 4.28.2,
grpc-java 1.85.1, grpc-kotlin 1.5.0). Change them together.

## Layers in the HMI (`ca.stefanm.ibus.gui.map.repo`)

| Class | Layer |
| --- | --- |
| `MapsBackendSettingsRepository` | backend host/port, in `config.conf` (`E39Config.MapConfig.Backend`, default `localhost:50051`); editable in Map Settings → Maps Backend |
| `MapsBackendChannel` | the gRPC connection, rebuilt when the settings change; `MapsBackendException` for errors |
| `PoiGrpcClient`, `RouteGrpcClient` | transport: one method per RPC, protobuf messages in and out |
| `PoiMapper` | protobuf ⇄ HMI types (`PoiRepository.Poi`, simplelatlng `LatLng`, icons) |
| `RemotePoiRepository`, `RemoteRouteRepository` | HMI types, `Result`s; POIs as a cached `StateFlow`, refreshed every 30 s, after changes and on settings changes |

`gui.map.poi.PoiRepository` keeps the screens' API on top of
`RemotePoiRepository`. Routing answers `UNIMPLEMENTED` until the backend has
Valhalla; guidance still uses `DummyRouteCalculator`.

Development: run the backend on the laptop (`python manage.py
run_grpc_server` in e39-rpi-maps) or the e39-rpi QEMU image (forwards
`localhost:50051`); POIs are visible in its Django admin
(`admin`/`e39rpi`).

# PIM bridge API (gRPC): calendars

`e39pim_api/v1/*.proto` is a **copy** of the PIM bridge's API (e39-rpi-pim,
`proto/e39pim_api/v1/`: Akonadi behind it); `e39pim_api/SOURCE` says which
commit. Change the API there, then copy and regenerate here:

```sh
./makePimProtos.sh                                 # from ~/e39-rpi-pim
E39_PIM_REPO=/path/to/e39-rpi-pim ./makePimProtos.sh
./makePimProtos.sh --no-copy                       # regenerate from these copies
```

Generated code in `../java/ca/stefanm/e39pim/v1/`, same tools and versions as
the maps API above.

## Layers in the HMI (`ca.stefanm.ibus.gui.pim.calendar.repo`)

| Class | Layer |
| --- | --- |
| `backend.PimBackendSettingsRepository` | bridge host/port, in `config.conf` (`E39Config.PimConfig.Backend`, default `localhost:50052`); editable in Calendar → Setup |
| `backend.PimBackendChannel` | the gRPC connection, rebuilt when the settings change; `PimBackendException` for errors (`Conflict` = the event changed meanwhile) |
| `backend.CalendarGrpcClient` | transport: CalendarService/StatusService calls, protobuf messages in and out; `watchChanges()` as a `Flow` |
| `backend.CalendarMapper` | protobuf ⇄ HMI types (`AgendaCalendarEventData`, `CalendarEventRef`, `Instant`) |
| `backend.RemoteCalendarRepository` | `events(start, end)`: a `Flow` that queries again when the bridge reports changes in that range (one shared WatchChanges stream) and retries every 10 s while it's unreachable; `connectionState`; `checkConnection()` |
| `api.WeekViewRepo`, `api.MonthViewRepo` | what the screens use: events for day columns, events by day for the month grid |

Screens must `remember` these flows: every call starts its own queries.
`WeekViewRepo.saveEditedEvent` only logs until the bridge can write events.

Debug: Debug Launchpad (or the loading window's Debug menu) → Calendar (PIM
Bridge) Debug: settings, connection check, calendars, live event queries
through each repository, GetEvent, the raw change stream.

Development: run the bridge on the laptop (`scripts/run-dev.sh
--test-calendar` in e39-rpi-pim; its test calendar is the week of
2026-11-16).
