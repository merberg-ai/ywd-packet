# Architecture

YWD Packet is an Android packet-radio application. The modem/TNC remains outside the app.

```text
Compose UI
   |
ViewModel / session presentation
   |
AX.25 application/session engine        <- packet-core
   |
raw AX.25 frames
   |
KISS transport                          <- kiss
   |
TCP KISS
   |
YWD-MMDVM-TNC :8001
   |
Bell-202 / RF
```

## Safety boundary

The app must not take ownership of modem RF policy. YWD-MMDVM-TNC remains responsible for its qualified modem, channel-access and RF lifecycle. YWD Packet treats KISS as a raw AX.25 frame transport.

P0 is intentionally passive from the app UI: it can connect to TCP KISS and monitor incoming data frames, but no connected-mode transmit/session engine is exposed yet.

## Modules

- `app`: Android UI, lifecycle and user settings.
- `packet-core`: platform-independent AX.25 framing and, later, connected-mode state machines.
- `kiss`: platform-independent KISS framing plus TCP KISS transport.
- `tools/kiss-sim`: a fake TCP KISS server for Android testing without RF.

## Planned protocol progression

1. P0: KISS transport, AX.25 decode, passive monitor and build infrastructure.
2. P1: monitor polish, settings persistence and deterministic transport fixtures.
3. P2: outgoing SABM/UA/DISC state machine and one connected session.
4. P3: I/RR/RNR/REJ sequencing, FRACK, retries, MAXFRAME and PACLEN.
5. P4: multiple sessions and incoming connects.
6. P5: unproto/UI terminal, saved stations, macros and history.

The connected-mode engine belongs in `packet-core`, not in Compose or the TNC.
