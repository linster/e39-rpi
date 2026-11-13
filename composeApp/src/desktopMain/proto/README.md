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
