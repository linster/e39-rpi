#!/usr/bin/env bash
# Copy the PIM bridge's API (.proto files) into this repo.
#
# The API belongs to e39-rpi-pim (proto/e39pim_api/v1/*.proto). This script
# copies those files into composeApp/src/desktopMain/proto/e39pim_api/ (so
# this repo records which API version it's built against) and notes the
# e39-rpi-pim commit in e39pim_api/SOURCE.
#
# The Java/Kotlin code is generated from them at build time, not committed:
# Gradle's generateProtos task (composeApp/build.gradle.kts) runs protoc with
# the grpc-java and grpc-kotlin plugins into composeApp/build/generated/proto/.
# To regenerate by hand: ./gradlew :composeApp:generateProtos
#
#   ./makePimProtos.sh                       # copy from $E39_PIM_REPO
#   E39_PIM_REPO=/path/to/e39-rpi-pim ./makePimProtos.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROTO_DIR="$ROOT/composeApp/src/desktopMain/proto"
E39_PIM_REPO="${E39_PIM_REPO:-$HOME/e39-rpi-pim}"

[ -d "$E39_PIM_REPO/proto/e39pim_api" ] || {
    echo "no e39-rpi-pim checkout at $E39_PIM_REPO (set E39_PIM_REPO)" >&2; exit 1; }
rm -rf "$PROTO_DIR/e39pim_api"
mkdir -p "$PROTO_DIR"
cp -R "$E39_PIM_REPO/proto/e39pim_api" "$PROTO_DIR/"
printf 'Copied from e39-rpi-pim %s by makePimProtos.sh; edit them there.\n' \
    "$(git -C "$E39_PIM_REPO" describe --always --dirty 2>/dev/null || echo '(unknown commit)')" \
    > "$PROTO_DIR/e39pim_api/SOURCE"

echo "Copied $(find "$PROTO_DIR/e39pim_api" -name '*.proto' | wc -l) .proto files"
cat "$PROTO_DIR/e39pim_api/SOURCE"
echo "The Gradle build generates the code from them (./gradlew :composeApp:generateProtos)."
