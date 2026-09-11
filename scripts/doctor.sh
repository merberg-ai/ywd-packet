#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"

printf '%-18s %s\n' "JAVA" "$(java -version 2>&1 | head -n1 || true)"
printf '%-18s %s\n' "ANDROID SDK" "$SDK_ROOT"
printf '%-18s %s\n' "sdkmanager" "$([[ -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]] && echo OK || echo MISSING)"
printf '%-18s %s\n' "platform android-36" "$([[ -d "$SDK_ROOT/platforms/android-36" ]] && echo OK || echo MISSING)"
printf '%-18s %s\n' "build-tools 36" "$([[ -d "$SDK_ROOT/build-tools/36.0.0" ]] && echo OK || echo MISSING)"
printf '%-18s %s\n' "adb" "$(command -v adb || echo MISSING)"
printf '%-18s %s\n' "gradle" "$($ROOT/gradlew --version 2>/dev/null | awk '/Gradle / {print $2; exit}' || echo MISSING)"
