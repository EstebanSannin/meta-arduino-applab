SUMMARY = "Arduino core for App Lab boards, shipped in the image"
DESCRIPTION = "The archives arduino-cli downloads to install the Arduino core \
(platform, toolchain, tools, built-in tools) and the libraries the App Lab \
example apps pin, with trimmed package and library indexes. The core is \
installed at first boot from these, without downloading it, as it comes \
preinstalled on Arduino's images; later core updates come from App Lab as \
usual. Regenerate with scripts/arduino-core-seed.py."
HOMEPAGE = "https://github.com/arduino/ArduinoCore-zephyr"
# The core (LGPL-2.1), the Zephyr toolchain (GPL-3.0-with-GCC-exception and
# others), Arduino tools (GPL/AGPL/Apache-2.0) and libraries (various). They are
# shipped as downloaded and installed on the target.
LICENSE = "CLOSED"

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

COMPATIBLE_MACHINE = "ventuno-q|uno-q"
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
