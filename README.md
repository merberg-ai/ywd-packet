<div align="center">

# YWD Packet

**Retro/cyber native Android AX.25 packet terminal**

TCP KISS · live packet monitor · platform-independent AX.25 core · headless Ubuntu builds

![Status](https://img.shields.io/badge/status-P0%20bootstrap-orange)
![Android](https://img.shields.io/badge/Android-native-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-blue)
![License](https://img.shields.io/badge/license-GPL--2.0--or--later-blue)

</div>

YWD Packet is a native Android packet-radio terminal designed first for **YWD-MMDVM-TNC** and other TCP KISS TNCs. It is being built as a real AX.25 application rather than a skin around a desktop packet stack: the Android app will own connected-mode AX.25 session behavior while the TNC remains the modem/RF boundary.

The project is intentionally developed in small qualification-style phases. **P0 is passive:** it establishes the Android project, Ubuntu build environment, TCP KISS transport, AX.25 frame decoder, fake KISS server and live monitor UI. Connected-mode transmit behavior comes later.

## Current P0 features

- Native Kotlin + Jetpack Compose Android application.
- Retro YWD dark/cyan/magenta monitor interface.
- TCP KISS client with streaming KISS framing/escaping.
- Basic AX.25 address, UI-frame and control-field decoding.
- Live read-only packet monitor.
- Pure-JVM protocol modules with unit tests.
- Fake TCP KISS server for testing the phone without touching RF.
- Reproducible command-line Android setup for Ubuntu x86_64.
- GitHub Actions unit test, lint and debug APK build.

## Architecture

```text
Android / Compose
      |
packet presentation + later session manager
      |
packet-core  (AX.25)
      |
kiss         (TCP KISS)
      |
YWD-MMDVM-TNC :8001
      |
MMDVM_HS / Bell-202 / RF
```

YWD Packet does **not** replace YWD-MMDVM-TNC's modem, channel-access or RF safety policy. The app sees raw AX.25 frames over KISS and keeps application/session logic on the Android side.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Ubuntu-mini setup

The development path is designed to work over SSH from a phone without Android Studio.

```bash
git clone https://github.com/merberg-ai/ywd-packet.git
cd ywd-packet
git switch dev
./scripts/setup-ubuntu.sh
```

The setup script installs OpenJDK 17 and basic host tools, downloads the pinned Android command-line tools, verifies their SHA-256, accepts SDK licenses, and installs:

- Android platform API 37;
- Android Build Tools 36.0.0;
- platform-tools / adb.

It writes `local.properties` for the local Android SDK path. Run a quick environment check with:

```bash
./scripts/doctor.sh
```

## Build and test

```bash
./scripts/test.sh
./scripts/build-debug.sh
```

The debug APK is copied to:

```text
dist/ywd-packet-0.0.1-dev-debug.apk
```

The script prints its SHA-256 after every build.

An unsigned release build can be produced with:

```bash
./scripts/build-release.sh
```

## Install on an Android phone

Once `adb` can see or pair with the phone:

```bash
./scripts/install-adb.sh
```

That builds the debug APK, runs `adb install -r`, and launches YWD Packet.

## Test without RF

Start the included fake KISS server on the Ubuntu machine:

```bash
./scripts/run-kiss-sim.sh
```

It listens on TCP port `8001` and emits a synthetic AX.25 UI beacon every few seconds. Point YWD Packet at the Ubuntu machine's LAN address and connect. This exercises Android -> TCP -> KISS -> AX.25 decode -> monitor UI without using a radio.

## Real TNC use

For YWD-MMDVM-TNC, configure its TCP KISS listener for trusted-LAN access and point YWD Packet at the Raspberry Pi's real LAN address, port `8001` by default.

KISS has no authentication or encryption. Do not expose a KISS listener directly to the public Internet or an untrusted network.

## Development phases

| Phase | Goal |
| --- | --- |
| P0 | Project/toolchain, passive monitor, KISS/AX.25 core, simulator |
| P1 | Monitor polish, persistence, transport hardening |
| P2 | Single outgoing connected-mode session |
| P3 | I/RR/RNR/REJ, FRACK, retries, MAXFRAME, PACLEN |
| P4 | Multiple sessions + incoming connections |
| P5 | Unproto/UI terminal, saved stations, macros, history |
| Later | USB/Bluetooth KISS, richer diagnostics, optional APRS-aware features |

## Toolchain pins

The initial P0 baseline uses:

- Android Gradle Plugin 9.4.0;
- Gradle 9.6.1;
- JDK 17;
- compileSdk 37;
- targetSdk 36;
- Jetpack Compose BOM 2026.08.00;
- Kotlin / Compose compiler plugin 2.3.21.

Versions are intentionally pinned. Build infrastructure should change deliberately, not because a dependency happened to publish something new overnight.

## License

GPL-2.0-or-later. A full license file will be carried in the repository before the first tagged release.
