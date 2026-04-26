# EchoRescue Android

This is the native Android version of EchoRescue. It replaces the browser-only rescue path with Android BLE central/peripheral support and native audio emission/capture.

## Architecture

- `victim` mode advertises a BLE GATT service and arms an acoustic responder when the rescuer sends an `ARM` command.
- `rescuer` mode scans for the EchoRescue BLE service, connects, sends `ARM`, emits an ultrasonic chirp, and measures the return chirp.
- Ranging uses acoustic round-trip time with a fixed victim response delay rather than pretending BLE latency is the distance signal.
- Localization uses multiple anchor measurements and trilateration to estimate a victim zone with uncertainty.
- Medical guidance is currently offline guide fallback with a clean abstraction for a future on-device model path.

## Run

Open `/Users/arthikarjee/aiforhealthcare_drivecode_ayushisharma/echorescue-android` in Android Studio and run the `app` configuration on two Android devices.

## Field Notes

- This is a far more realistic direction than the PWA, but it is still a prototype.
- Real deployment requires per-device acoustic latency calibration, speaker/microphone hardware validation, and extensive noisy-environment testing.
- Some phones cannot reproduce or capture 20-22 kHz effectively; those devices will underperform or fail.
- For sub-1 m production localization, move to UWB-first hardware and a dedicated victim beacon.
