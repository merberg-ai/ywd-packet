#!/usr/bin/env bash
set -euo pipefail
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
ADB="${ADB:-$SDK_ROOT/platform-tools/adb}"
[[ -x "$ADB" ]] || ADB="$(command -v adb)"
exec "$ADB" logcat -v color 'AndroidRuntime:E' 'YWD-PACKET:D' '*:S'
