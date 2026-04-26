# EchoRescue Advanced Architecture

## Best platform

For the most advanced fully functional version, build on **native Android** first:

- Kotlin for app orchestration and UI
- Jetpack Compose for operator workflows
- Foreground services for victim mode survivability
- Android BLE peripheral and central APIs for the discovery/control layer
- Native low-latency audio with `AudioRecord`/`AudioTrack` now, then move the DSP core to **NDK + Oboe** for tighter timing
- Optional **UWB** path on supported Android hardware, with BLE retained as the pairing/control channel

## Production-grade stack

### Tier 1: Phone-only advanced prototype

- Two Android phones
- BLE advertising + GATT trigger
- Acoustic round-trip ranging
- Per-device calibration profile
- Offline medical guide engine

### Tier 2: Most advanced realistic field stack

- Android rescuer app as the operator console
- Dedicated victim beacon hardware:
  - BLE radio for discovery/control
  - UWB radio for primary ranging
  - Ultrasonic transducer as fallback when UWB is blocked or unavailable
  - MCU/DSP for deterministic timing
- Optional thermal camera / audio array on the rescuer side

## Why this is the right direction

- Browsers do not give enough control over BLE peripheral behavior, background execution, or audio timing.
- iOS is more restrictive for BLE peripheral workflows and on-device experimental AI access.
- Android gives the best mix of BLE, foreground service control, UWB support, and native audio access.

## Next engineering steps

1. Add a calibration screen using a known 1 m and 3 m reference setup.
2. Move chirp generation and detection into an NDK DSP module.
3. Add a matched-filter / cross-correlation detector instead of Goertzel-only scoring.
4. Add UWB ranging via Jetpack UWB on supported devices.
5. Add a protocol layer with device health, battery, and rescue logs.
