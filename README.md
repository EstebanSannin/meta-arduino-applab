# meta-arduino-applab

Run [Arduino App Lab](https://github.com/arduino/arduino-app-lab) apps (Docker
containers + MCU sketches) on Arduino boards with a Yocto-based distribution,
with the same experience as Arduino's own images.

Supported: **Arduino VENTUNO Q** (`ventuno-q`) and **Arduino UNO Q** (`uno-q`).

## What it adds

| Recipe | What |
|---|---|
| `arduino-app-cli` | App Lab daemon, brick assets and examples, setup/first-boot services, mDNS advertisement, sudo rules and helpers |
| `arduino-router` | MCU Bridge (MessagePack-RPC over the MCU's USB serial) |
| `arduino-cli` | Arduino CLI |
| `openocd-arduino` | ST's OpenOCD with `linuxgpiod`, at `/opt/openocd` for Arduino's `remoteocd` (MCU flashing over SWD) |
| `arduino-applab-apt-shim` | `apt`/`dpkg` stand-ins: App Lab's update check sees "nothing to upgrade" |
| `arduino-user` | `arduino` user, UID 1000 (default user provider) |
| `arduino-zephyr-core` | The Arduino core (platform, Zephyr toolchain, tools, libraries) as Arduino's archives, installed at first boot without network. Regenerate for a new core with `scripts/arduino-core-seed.py` |
| `arduino-adbd` | adbd over USB with the board's Arduino USB IDs and a serial function, for App Lab's first-run setup |
| `android-tools` 35.0.2, `android-libboringssl` | Backport from meta-openembedded master: App Lab needs adb's shell v2 protocol, which wrynose's adbd 5.1.1 lacks. Patched so the adb shell runs as `ADBD_USER` and adbd does not fall back to unauthenticated TCP |
| `packagegroup-arduino-applab` | All of the above |

The Arduino binaries are Arduino's release packages (static Go binaries).
The Arduino core (MCU toolchain) ships in the image and is installed at first
boot without network (`arduino-zephyr-core`); `arduino-app-cli system init
--only-arduino-platform` then adds the example libraries when online. App Lab
updates the core later as usual.

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
| `ARDUINO_APPLAB_CORE_IN_IMAGE` | `1` | Ship the Arduino core in the image instead of downloading it at first boot |
| `ARDUINO_APPLAB_USB_FIRST_RUN` | `1` | Install `arduino-adbd` (App Lab's first-run setup over USB) |

## Known limitations

- **Core updates vs. the rest of the App Lab software.** App Lab updates the
  Arduino core (`arduino:zephyr`) in the user's home, as on Arduino's images.
  The daemon (`arduino-app-cli`), the router and OpenOCD, however, come with
  the OS image, while on Arduino's images they are updated together with the
  core (apt). A future core that needs a newer daemon or flashes differently
  would only work after an OS update. If that happens, limit the core versions
  App Lab offers with `ARDUINO_APP_CLI__PLATFORM_VERSION_CONSTRAINT` (today
  `<2.0.0-0`, as upstream) until the OS catches up.
- **First boot.** The core ships in the image but is installed into the home
  directory (in `/var` on OSTree systems) at first boot: about 100 s before
  sketches compile. After a reflash the board is back to the shipped core
  version, and App Lab offers the update again.
- **Containers.** App images (`python-apps-base`, bricks) are pulled when an
  app first runs, as on Arduino's images: that needs network.

## Status

Tested on Torizon OS 8 (wrynose) on the VENTUNO Q: first-run setup over USB, setup wizard, sketch upload,
Bridge, container apps, NPU apps (Gesture Booth, Edge Impulse object detection).
