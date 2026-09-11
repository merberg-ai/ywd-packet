#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="9.6.1"
GRADLE_SHA256="9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14"
CACHE_ROOT="${XDG_CACHE_HOME:-$HOME/.cache}/ywd-packet"
GRADLE_HOME="$CACHE_ROOT/gradle-$GRADLE_VERSION"
ZIP="$CACHE_ROOT/gradle-$GRADLE_VERSION-bin.zip"

if [[ ! -x "$GRADLE_HOME/bin/gradle" ]]; then
    mkdir -p "$CACHE_ROOT"
    if [[ ! -f "$ZIP" ]]; then
        echo "[GRADLE] downloading $GRADLE_VERSION"
        curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
    fi
    echo "$GRADLE_SHA256  $ZIP" | sha256sum -c -
    rm -rf "$GRADLE_HOME" "$CACHE_ROOT/gradle-$GRADLE_VERSION.tmp"
    mkdir -p "$CACHE_ROOT/gradle-$GRADLE_VERSION.tmp"
    unzip -q "$ZIP" -d "$CACHE_ROOT/gradle-$GRADLE_VERSION.tmp"
    mv "$CACHE_ROOT/gradle-$GRADLE_VERSION.tmp/gradle-$GRADLE_VERSION" "$GRADLE_HOME"
    rm -rf "$CACHE_ROOT/gradle-$GRADLE_VERSION.tmp"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
