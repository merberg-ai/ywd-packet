#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
TOOLS_REV="15859902"
TOOLS_SHA256="4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583"
TOOLS_ZIP="/tmp/ywd-android-commandlinetools-$TOOLS_REV.zip"

if [[ "$(uname -s)" != "Linux" || "$(uname -m)" != "x86_64" ]]; then
    echo "This bootstrap currently targets x86_64 Linux." >&2
    exit 1
fi

sudo apt-get update
sudo apt-get install -y openjdk-17-jdk git curl unzip zip ca-certificates

mkdir -p "$SDK_ROOT/cmdline-tools"
if [[ ! -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]]; then
    echo "[SDK] downloading Android command-line tools $TOOLS_REV"
    curl -fL --retry 3 "https://dl.google.com/android/repository/commandlinetools-linux-${TOOLS_REV}_latest.zip" -o "$TOOLS_ZIP"
    echo "$TOOLS_SHA256  $TOOLS_ZIP" | sha256sum -c -
    tmp="$(mktemp -d)"
    unzip -q "$TOOLS_ZIP" -d "$tmp"
    rm -rf "$SDK_ROOT/cmdline-tools/latest"
    mkdir -p "$SDK_ROOT/cmdline-tools/latest"
    cp -a "$tmp/cmdline-tools/." "$SDK_ROOT/cmdline-tools/latest/"
    rm -rf "$tmp" "$TOOLS_ZIP"
fi

export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$SDK_ROOT/cmdline-tools/latest/bin:$SDK_ROOT/platform-tools:$PATH"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager \
    "platform-tools" \
    "platforms;android-36" \
    "build-tools;36.0.0"

printf 'sdk.dir=%s\n' "$SDK_ROOT" > "$ROOT/local.properties"

cat > "$ROOT/.android-env" <<ENV
export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools:\$PATH"
ENV

echo
"$ROOT/scripts/doctor.sh"
echo
echo "Setup complete. Build with: ./scripts/build-debug.sh"
