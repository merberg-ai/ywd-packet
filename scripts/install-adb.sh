#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
ADB="${ADB:-$SDK_ROOT/platform-tools/adb}"
[[ -x "$ADB" ]] || ADB="$(command -v adb)"

"$ROOT/scripts/build-debug.sh"
"$ADB" devices
"$ADB" install -r "$ROOT/dist/ywd-packet-0.0.1-dev-debug.apk"
"$ADB" shell am start -n net.kj6ywd.packet/.MainActivity
