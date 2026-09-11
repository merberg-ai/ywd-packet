#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
exec ./gradlew --no-daemon :tools:kiss-sim:run --args="${1:-8001}"
