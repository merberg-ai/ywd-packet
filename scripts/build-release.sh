#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
mkdir -p dist
./gradlew --no-daemon :app:assembleRelease
src="app/build/outputs/apk/release/app-release-unsigned.apk"
out="dist/ywd-packet-0.0.1-dev-release-unsigned.apk"
cp "$src" "$out"
sha256sum "$out"
echo "UNSIGNED APK: $ROOT/$out"
