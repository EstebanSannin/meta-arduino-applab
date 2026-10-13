SUMMARY = "Arduino core for App Lab boards, shipped in the image"
DESCRIPTION = "The archives arduino-cli downloads to install the Arduino core \
(platform, toolchain, tools, built-in tools) and the libraries the App Lab \
example apps pin, with trimmed package and library indexes. The core is \
installed at first boot from these, without downloading it, as it comes \
preinstalled on Arduino's images; later core updates come from App Lab as \
usual. Regenerate with scripts/arduino-core-seed.py."
HOMEPAGE = "https://github.com/arduino/ArduinoCore-zephyr"
# The archives are shipped as downloaded and installed on the target. Licenses,
# from the license files in the archives or, for bare binaries, their sources:
# - ArduinoCore-zephyr (Zephyr based) and its tools bin2uf2, gen-rodata-ld,
#   sync-zephyr-artifacts, zephyr-check-size, zephyr-sketch-tool: Apache-2.0
#   (ea_malloc: MIT); adb: Apache-2.0
# - arm-zephyr-eabi toolchain: GCC GPL-3.0-with-GCC-exception, binutils and gdb
#   GPL-3.0-or-later, gmp/mpc/mpfr LGPL-3.0-or-later, picolibc and zstd
#   BSD-3-Clause, isl and expat MIT, zlib Zlib
# - openocd, ctags: GPL-2.0-only; dfu-util: GPL-2.0-or-later; bossac:
#   BSD-3-Clause; rp2040tools: MPL-2.0; remoteocd, dfu-discovery, mdns-discovery,
#   serial-discovery, serial-monitor, scripting-tools: GPL-3.0-only
# - libraries: ArduinoGraphics, Arduino_LTR381RGB, Arduino_Modulino,
#   Arduino_RouterBridge, Arduino_RPClite MPL-2.0; Arduino_HS300x,
#   Arduino_LPS22HB, Arduino_LSM6DSOX LGPL-2.1-only; ArxContainer, ArxTypeTraits,
#   DebugLog, MsgPack MIT; STM32duino_VL53L4CD/VL53L4ED BSD-3-Clause
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause & Zlib & MPL-2.0 & LGPL-2.1-only & \
LGPL-3.0-or-later & GPL-2.0-only & GPL-2.0-or-later & GPL-3.0-only & \
GPL-3.0-or-later & GPL-3.0-with-GCC-exception"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
    file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
    file://${COMMON_LICENSE_DIR}/Zlib;md5=87f239f408daca8a157858e192597633 \
    file://${COMMON_LICENSE_DIR}/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad \
    file://${COMMON_LICENSE_DIR}/LGPL-2.1-only;md5=1a6d268fd218675ffea8be556788b780 \
    file://${COMMON_LICENSE_DIR}/LGPL-3.0-or-later;md5=c51d3eef3be114124d11349ca0d7e117 \
    file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
    file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c \
    file://${COMMON_LICENSE_DIR}/GPL-3.0-only;md5=c79ff39f19dfec6d293b95dea7b07891 \
    file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245 \
    file://${COMMON_LICENSE_DIR}/GPL-3.0-with-GCC-exception;md5=aef5f35c9272f508be848cd99e0151df \
"

require srcuri.inc

SRC_URI += " \
    file://package_index.json \
    file://library_index.json \
    file://arduino-zephyr-core-seed \
    file://arduino-zephyr-core-seed.service \
"

S = "${UNPACKDIR}"

ARDUINO_USER ?= "arduino"
ARDUINO_CORE ?= "arduino:zephyr@${PV}"
SEED_DIR = "${datadir}/arduino-applab/core-seed"

COMPATIBLE_MACHINE = "ventuno-q"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "arduino-zephyr-core-seed.service"

do_install() {
    install -d ${D}${SEED_DIR}/staging/packages ${D}${SEED_DIR}/staging/libraries
    install -m 0644 ${S}/package_index.json ${S}/library_index.json ${D}${SEED_DIR}/
    install -m 0644 ${S}/staging/packages/* ${D}${SEED_DIR}/staging/packages/
    install -m 0644 ${S}/staging/libraries/* ${D}${SEED_DIR}/staging/libraries/
    install -D -m 0755 ${S}/arduino-zephyr-core-seed ${D}${libexecdir}/arduino-applab/arduino-zephyr-core-seed
    install -D -m 0644 ${S}/arduino-zephyr-core-seed.service ${D}${systemd_system_unitdir}/arduino-zephyr-core-seed.service
    sed -i -e 's#@SEED@#${SEED_DIR}#g' -e 's#@CORE@#${ARDUINO_CORE}#g' \
        -e 's#@ARDUINO_USER@#${ARDUINO_USER}#g' -e 's#@LIBEXECDIR@#${libexecdir}#g' \
        ${D}${libexecdir}/arduino-applab/arduino-zephyr-core-seed \
        ${D}${systemd_system_unitdir}/arduino-zephyr-core-seed.service
}

FILES:${PN} += "${SEED_DIR} ${systemd_system_unitdir}"

# Prebuilt aarch64 binaries, as Arduino ships them; not processed by the build
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INSANE_SKIP:${PN} += "already-stripped arch file-rdeps"

RDEPENDS:${PN} += "arduino-cli arduino-app-cli"
