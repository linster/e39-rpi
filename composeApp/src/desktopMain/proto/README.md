# Maps backend API (gRPC)

`e39maps_api/v1/*.proto` is a **copy** of the maps backend's API
(e39-rpi-maps, `proto/e39maps_api/v1/`); `e39maps_api/SOURCE` says which
commit. Change the API there, then copy it here:

```sh
./makeMapsProtos.sh                                # from ~/e39-rpi-maps
E39_MAPS_REPO=/path/to/e39-rpi-maps ./makeMapsProtos.sh
```

The code (messages, Kotlin DSL, grpc-java and grpc-kotlin stubs) is
**generated at build time, not committed**: Gradle's `generateProtos` task
(`composeApp/build.gradle.kts`) runs protoc into
`composeApp/build/generated/proto/desktopMain`, which is part of the
`desktopMain` sources; compiling depends on it. protoc and the plugins are
Maven artifacts that Gradle resolves like the libraries, and their versions
match the runtime libraries there (protobuf-java 4.28.2, grpc-java 1.85.1,
grpc-kotlin 1.5.0): change them together. Which API folders it compiles:
`generatedProtoApis` in that file.

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

# PIM bridge API (gRPC): calendars and mail

`e39pim_api/v1/*.proto` is a **copy** of the PIM bridge's API (e39-rpi-pim,
`proto/e39pim_api/v1/`: Akonadi behind it); `e39pim_api/SOURCE` says which
commit. Change the API there, then copy it here:

```sh
./makePimProtos.sh                                 # from ~/e39-rpi-pim
E39_PIM_REPO=/path/to/e39-rpi-pim ./makePimProtos.sh
```

The code is generated at build time like the maps API's (`generateProtos`,
`generatedProtoApis` in `composeApp/build.gradle.kts`), not committed.

## Layers in the HMI (`ca.stefanm.ibus.gui.pim.calendar.repo`)

| Class | Layer |
| --- | --- |
| `backend.PimBackendSettingsRepository` | bridge host/port, in `config.conf` (`E39Config.PimConfig.Backend`, default `localhost:50052`); editable in Calendar → Setup |
| `backend.PimBackendChannel` | the gRPC connection, rebuilt when the settings change; `PimBackendException` for errors (`Conflict` = the event changed meanwhile) |
| `backend.CalendarGrpcClient`, `backend.TodoGrpcClient` | transport: CalendarService/StatusService/TodoService calls, protobuf messages in and out; `watchChanges()` as a `Flow` |
| `backend.PimChangeStream` | the bridge's change notices: one WatchChanges stream shared by the repositories, reconnecting |
| `backend.CalendarMapper` | protobuf ⇄ HMI types (`AgendaCalendarEventData`, `CalendarEventRef`, `TodoItem`, `EventTimes`, `Instant`) |
| `backend.RemoteCalendarRepository` | `events(start, end)`: a `Flow` that queries again when the bridge reports changes in that range and retries every 10 s while it's unreachable; `calendars()`, `defaultEventCalendar()`; `createEvent`, `updateEvent`, `deleteEvent` (this occurrence or the whole series); `connectionState`; `checkConnection()` |
| `backend.RemoteTodoRepository` | `todos(includeCompleted)`: a `Flow` updated on to-do changes; `createTodo`, `updateTodo`, `setCompleted`, `deleteTodo` |
| `api.WeekViewRepo`, `api.MonthViewRepo` | what the screens use: events for day columns, events by day for the month grid; `saveEditedEvent` for the editor |

Screens must `remember` these flows: every call starts its own queries.
Writes return `Result`s; `PimBackendException.Conflict` means the event or
to-do changed meanwhile (e.g. a sync came in): reload and redo the change.
The editor saves the name and, if entered, new times; for a repeating event
only that occurrence.

Debug: Debug Launchpad (or the loading window's Debug menu) → Calendar (PIM
Bridge) Debug: settings, connection check, calendars, live event queries
through each repository, GetEvent, the raw change stream.

Development: run the bridge on the laptop (`scripts/run-dev.sh
--test-calendar` in e39-rpi-pim; its test calendar is the week of
2026-11-16).

## Mail (`ca.stefanm.ibus.gui.pim.email.repo`)

| Class | Layer |
| --- | --- |
| `MailGrpcClient` | transport: MailService calls; `watchMail()` as a `Flow` |
| `MailChangeStream` | one WatchMail stream shared by the mail flows and the notifier, reconnecting |
| `MailMapper`, `MailModels.kt` | protobuf ⇄ HMI types (`MailFolder`, `MailSummary`, `MailMessage`, `MailEvent`, ...) |
| `MailRepository` | what screens and the notifier use: `RemoteMailRepository` (the bridge) or `FakeMailRepository` (sample data), picked by `E39Config.PimConfig.Mail.useFakeData` at startup (`MailModule`) |
| `MailSettingsRepository` | quick replies and notification settings (`E39Config.PimConfig.Mail`) |

`email.notifier.MailNotifierService` (platform service group "Pim") posts
new-mail and sent/not-sent notifications. Debug: Debug Launchpad → Mail
(PIM Bridge) Debug.
