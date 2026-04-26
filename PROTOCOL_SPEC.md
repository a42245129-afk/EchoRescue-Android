# EchoRescue Protocol Spec

## Purpose

This protocol defines how the rescuer unit and victim beacon coordinate discovery, arming, ranging, and health reporting.

## Transport layers

- `BLE`: pairing, discovery, arming, health/status exchange
- `UWB`: primary precise ranging when supported
- `Acoustic`: fallback ranging through air gaps

## BLE service

- Service UUID: `34c2d5f0-6fd8-4fcb-9f07-50f6919dc001`
- Command characteristic UUID: `34c2d5f0-6fd8-4fcb-9f07-50f6919dc002`
- Status characteristic UUID: `34c2d5f0-6fd8-4fcb-9f07-50f6919dc003`

## Commands

### `ARM`

- Sent by rescuer after BLE connection
- Victim transitions into acoustic reply-ready state
- Victim reply delay must be deterministic and calibrated

### Future commands

- `PING`: health check
- `BATTERY`: battery status request
- `MODE_UWB`: switch to UWB ranging session
- `MODE_ACOUSTIC`: force acoustic fallback
- `PANIC`: conscious victim manual trigger

## Acoustic ranging sequence

1. Rescuer connects over BLE.
2. Rescuer sends `ARM`.
3. Victim arms the acoustic responder.
4. Rescuer emits challenge chirp.
5. Victim detects the challenge.
6. Victim waits fixed calibrated delay.
7. Victim emits reply chirp.
8. Rescuer detects reply and estimates distance.

## Distance model

- `distance = ((elapsed_time - victim_reply_delay) * speed_of_sound) / 2 + calibration_offset`

## Localization mode

To estimate victim position rather than a single distance:

1. Rescuer captures 3 or more measurements from known anchor positions.
2. The app solves a trilateration estimate.
3. Output is a victim zone with uncertainty radius, not a fake exact point.

## Future production requirements

- message signing
- replay protection
- device identity and pairing trust
- compressed rescue logs
- periodic beacon heartbeat
