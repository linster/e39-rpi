#!/usr/bin/env bash
# Generate the Java/Kotlin gRPC client code for the PIM bridge's API.
#
# The API belongs to e39-rpi-pim (proto/e39pim_api/v1/*.proto). This script
# copies those files into composeApp/src/desktopMain/proto/ (so this repo
# records which API version it was built against) and generates
#   - protobuf messages (Java + Kotlin DSL) and grpc-java stubs, and
#   - grpc-kotlin coroutine stubs (CalendarServiceGrpcKt, StatusServiceGrpcKt, ...)
# into composeApp/src/desktopMain/java/ca/stefanm/e39pim/v1/, committed like
# the Pico protos (makeProtos.sh), so Gradle needs no protobuf plugin.
#
#   ./makePimProtos.sh                       # copy from $E39_PIM_REPO, generate
#   E39_PIM_REPO=/path/to/e39-rpi-pim ./makePimProtos.sh
#   ./makePimProtos.sh --no-copy             # regenerate from the copies here
#
# protoc and the two plugins are downloaded from Maven Central once into
# ~/.cache/e39-rpi-protoc (checked against Maven's SHA-1). Their versions
# must match the runtime libraries in composeApp/build.gradle.kts: code from
# a newer protoc refuses to load with an older protobuf-java.
set -euo pipefail

PROTOBUF_VERSION=4.28.2     # = com.google.protobuf:protobuf-java (protoc 28.2)
GRPC_JAVA_VERSION=1.85.1    # = io.grpc:grpc-*
GRPC_KOTLIN_VERSION=1.5.0   # = io.grpc:grpc-kotlin-stub

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROTO_DIR="$ROOT/composeApp/src/desktopMain/proto"
OUT="$ROOT/composeApp/src/desktopMain/java"
E39_PIM_REPO="${E39_PIM_REPO:-$HOME/e39-rpi-pim}"
CACHE="${XDG_CACHE_HOME:-$HOME/.cache}/e39-rpi-protoc"
MAVEN=https://repo1.maven.org/maven2

case "$(uname -m)" in
    x86_64) ARCH=linux-x86_64 ;;
    aarch64) ARCH=linux-aarch_64 ;;
    *) echo "unsupported machine $(uname -m)" >&2; exit 1 ;;
esac

fetch() {   # fetch <maven path> <file name>: download once, verify SHA-1
    local url="$MAVEN/$1/$2" dest="$CACHE/$2"
    if [ ! -f "$dest" ]; then
        mkdir -p "$CACHE"
        echo "Downloading $2" >&2
        curl -fsSL -o "$dest.part" "$url"
        local want; want="$(curl -fsSL "$url.sha1" | cut -c1-40)"
        echo "$want  $dest.part" | sha1sum -c --quiet - || { rm -f "$dest.part"; exit 1; }
        mv "$dest.part" "$dest"
    fi
    echo "$dest"
}

PROTOC="$(fetch com/google/protobuf/protoc/$PROTOBUF_VERSION protoc-$PROTOBUF_VERSION-$ARCH.exe)"
GRPC_JAVA="$(fetch io/grpc/protoc-gen-grpc-java/$GRPC_JAVA_VERSION protoc-gen-grpc-java-$GRPC_JAVA_VERSION-$ARCH.exe)"
GRPC_KOTLIN_JAR="$(fetch io/grpc/protoc-gen-grpc-kotlin/$GRPC_KOTLIN_VERSION protoc-gen-grpc-kotlin-$GRPC_KOTLIN_VERSION-jdk8.jar)"
PROTOBUF_JAR="$(fetch com/google/protobuf/protobuf-java/$PROTOBUF_VERSION protobuf-java-$PROTOBUF_VERSION.jar)"
chmod +x "$PROTOC" "$GRPC_JAVA"

# protoc plugins must be executables: wrap the grpc-kotlin jar.
GRPC_KOTLIN="$CACHE/protoc-gen-grpc-kotlin-$GRPC_KOTLIN_VERSION.sh"
printf '#!/bin/sh\nexec java -jar "%s" "$@"\n' "$GRPC_KOTLIN_JAR" > "$GRPC_KOTLIN"
chmod +x "$GRPC_KOTLIN"

# The well-known types (google/protobuf/empty.proto, ...) aren't in the
# protoc download; protobuf-java's jar carries them.
WKT="$CACHE/wkt-$PROTOBUF_VERSION"
if [ ! -f "$WKT/google/protobuf/empty.proto" ]; then
    mkdir -p "$WKT"
    (cd "$WKT" && unzip -qo "$PROTOBUF_JAR" 'google/protobuf/*.proto')
fi

if [ "${1:-}" != "--no-copy" ]; then
    [ -d "$E39_PIM_REPO/proto/e39pim_api" ] || {
        echo "no e39-rpi-pim checkout at $E39_PIM_REPO (set E39_PIM_REPO)" >&2; exit 1; }
    rm -rf "$PROTO_DIR/e39pim_api"
    mkdir -p "$PROTO_DIR"
    cp -R "$E39_PIM_REPO/proto/e39pim_api" "$PROTO_DIR/"
    printf 'Copied from e39-rpi-pim %s by makePimProtos.sh; edit them there.\n' \
        "$(git -C "$E39_PIM_REPO" describe --always --dirty 2>/dev/null || echo '(unknown commit)')" \
        > "$PROTO_DIR/e39pim_api/SOURCE"
fi

rm -rf "$OUT/ca/stefanm/e39pim"
"$PROTOC" -I "$PROTO_DIR" -I "$WKT" \
    --java_out="$OUT" --kotlin_out="$OUT" \
    --plugin=protoc-gen-grpc-java="$GRPC_JAVA" --grpc-java_out="$OUT" \
    --plugin=protoc-gen-grpckt="$GRPC_KOTLIN" --grpckt_out="$OUT" \
    "$PROTO_DIR"/e39pim_api/v1/*.proto

echo "Generated $(find "$OUT/ca/stefanm/e39pim" -type f | wc -l) files in ${OUT#$ROOT/}/ca/stefanm/e39pim"
cat "$PROTO_DIR/e39pim_api/SOURCE"
