#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
./gradlew --no-daemon :packet-core:test :kiss:test :app:testDebugUnitTest :app:lintDebug
