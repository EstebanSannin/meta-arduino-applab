# meta-arduino-applab

Run [Arduino App Lab](https://github.com/arduino/arduino-app-lab) apps (Docker
containers + MCU sketches) on Arduino boards with a Yocto-based distribution,
with the same experience as Arduino's own images.

Supported: **Arduino VENTUNO Q** (`ventuno-q`). The UNO Q will follow.

## What it adds

| Recipe | What |
|---|---|
| `arduino-app-cli` | App Lab daemon, brick assets and examples, setup/first-boot services, mDNS advertisement, sudo rules and helpers |
| `arduino-router` | MCU Bridge (MessagePack-RPC over the MCU's USB serial) |
| `arduino-cli` | Arduino CLI |
| `openocd-arduino` | ST's OpenOCD with `linuxgpiod`, at `/opt/openocd` for Arduino's `remoteocd` (MCU flashing over SWD) |
| `arduino-applab-apt-shim` | `apt`/`dpkg` stand-ins: App Lab's update check sees "nothing to upgrade" |
| `arduino-user` | `arduino` user, UID 1000 (default user provider) |
| `packagegroup-arduino-applab` | All of the above |

The Arduino binaries are Arduino's release packages (static Go binaries).
The Arduino core (MCU toolchain) is installed at first boot with
`arduino-app-cli system init --only-arduino-platform`, and updated from App Lab.

## Use

```
IMAGE_INSTALL:append = " packagegroup-arduino-applab"
```

The image needs Docker, systemd, sudo and avahi. App Lab expects the user at
UID 1000; if your distribution already has one, set
`ARDUINO_APPLAB_USER_PROVIDER` to a recipe that adds `arduino` for it.

## Variables

| Variable | Default | Meaning |
|---|---|---|
| `ARDUINO_BOARD` | `ventunoq` on `ventuno-q` | Board name in the mDNS advertisement |
| `ARDUINO_USB_VID` / `ARDUINO_USB_PID` | `0x2341` / per board | Board USB IDs in the advertisement |
| `ARDUINO_USER` | `arduino` | User running the daemon |
| `ARDUINO_PASSWORD_EXPIRED` | `1` | `arduino-user`: force a password change at first use |
| `ARDUINO_APPLAB_USER_PROVIDER` | `arduino-user` | Recipe providing the App Lab user |

## Status

Tested on Torizon OS 8 (wrynose) on the VENTUNO Q: setup wizard, sketch upload,
Bridge, container apps, NPU apps (Gesture Booth, Edge Impulse object detection).
