#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
mkdir -p dist
./gradlew --no-daemon :app:assembleDebug
src="app/build/outputs/apk/debug/app-debug.apk"
out="dist/ywd-packet-0.0.1-dev-debug.apk"
cp "$src" "$out"
sha256sum "$out"
echo "APK: $ROOT/$out"
