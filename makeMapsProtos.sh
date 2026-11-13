#!/usr/bin/env bash
# Copy the maps backend's API (.proto files) into this repo.
#
# The API belongs to e39-rpi-maps (proto/e39maps_api/v1/*.proto). This script
# copies those files into composeApp/src/desktopMain/proto/e39maps_api/ (so
# this repo records which API version it's built against) and notes the
# e39-rpi-maps commit in e39maps_api/SOURCE.
#
# The Java/Kotlin code is generated from them at build time, not committed:
# Gradle's generateProtos task (composeApp/build.gradle.kts) runs protoc with
# the grpc-java and grpc-kotlin plugins into composeApp/build/generated/proto/.
# To regenerate by hand: ./gradlew :composeApp:generateProtos
#
#   ./makeMapsProtos.sh                       # copy from $E39_MAPS_REPO
#   E39_MAPS_REPO=/path/to/e39-rpi-maps ./makeMapsProtos.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROTO_DIR="$ROOT/composeApp/src/desktopMain/proto"
E39_MAPS_REPO="${E39_MAPS_REPO:-$HOME/e39-rpi-maps}"

[ -d "$E39_MAPS_REPO/proto/e39maps_api" ] || {
    echo "no e39-rpi-maps checkout at $E39_MAPS_REPO (set E39_MAPS_REPO)" >&2; exit 1; }
rm -rf "$PROTO_DIR/e39maps_api"
mkdir -p "$PROTO_DIR"
cp -R "$E39_MAPS_REPO/proto/e39maps_api" "$PROTO_DIR/"
printf 'Copied from e39-rpi-maps %s by makeMapsProtos.sh; edit them there.\n' \
    "$(git -C "$E39_MAPS_REPO" describe --always --dirty 2>/dev/null || echo '(unknown commit)')" \
    > "$PROTO_DIR/e39maps_api/SOURCE"

echo "Copied $(find "$PROTO_DIR/e39maps_api" -name '*.proto' | wc -l) .proto files"
cat "$PROTO_DIR/e39maps_api/SOURCE"
echo "The Gradle build generates the code from them (./gradlew :composeApp:generateProtos)."
