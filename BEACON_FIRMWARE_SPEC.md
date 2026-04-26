# EchoRescue Victim Beacon Firmware Spec

## Goal

Define the dedicated victim-side hardware/firmware required for reliable sub-1 m field localization.

## Recommended hardware

- MCU or SoC with:
  - BLE peripheral support
  - UWB radio support or companion transceiver
  - deterministic timer/capture peripherals
  - low-power sleep modes
- Ultrasonic transducer and matching analog front-end
- microphone front-end for challenge detection
- rugged battery-backed enclosure

## Firmware responsibilities

### BLE stack

- advertise rescue service continuously in low-duty standby
- accept secure rescuer connection
- expose command/status characteristics
- report battery, temperature, and self-test status

### UWB stack

- join secure ranging session
- respond to rescuer ranging requests
- publish quality/confidence metadata

### Acoustic fallback

- detect coded challenge chirp or matched-filter sequence
- wait calibrated fixed delay
- emit deterministic reply chirp
- expose signal quality metadata

### Safety and resilience

- watchdog restart
- brownout protection
- startup self-test
- battery threshold alarms
- flash-stored calibration constants

## Calibration values

- victim reply delay
- transducer latency
- microphone detection threshold
- temperature compensation if required

## Manufacturing test requirements

- BLE advertising validation
- UWB range validation
- acoustic latency validation
- battery discharge test
- enclosure vibration test
