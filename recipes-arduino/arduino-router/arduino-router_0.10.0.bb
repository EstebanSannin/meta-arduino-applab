SUMMARY = "Arduino Router: MessagePack-RPC bridge between Linux apps and the MCU"
HOMEPAGE = "https://github.com/arduino/arduino-router"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://usr/share/doc/arduino-router/copyright;md5=16b758b5bac341a1e6e195c9d7202c71"

# Arduino's release package (static Go binaries, units, per-board configs)
SRC_URI = " \
    https://github.com/arduino/arduino-router/releases/download/v${PV}/arduino-router_${PV}-1_arm64.deb;subdir=${BP} \
    file://90-arduino-mcu.rules \
"
SRC_URI[sha256sum] = "f538bc988178a93566a31e372ee00ecac0254c883ffed7a1122e3a08e977ec53"

COMPATIBLE_HOST = "aarch64.*-linux"

inherit systemd

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/usr/bin/arduino-router ${S}/usr/bin/arduino-router-cli ${D}${bindir}/

    # Per-board drop-ins, picked by the generator from the device tree.
    # Read-only data: /usr/share instead of /var/lib (not deployed on OSTree).
    install -d ${D}${datadir}/arduino-router/config
    install -m 0644 ${S}/var/lib/arduino-router/config/*.conf ${D}${datadir}/arduino-router/config/
    install -D -m 0755 ${S}/usr/lib/systemd/system-generators/systemd-arduino-router.sh \
        ${D}${systemd_unitdir}/system-generators/systemd-arduino-router.sh
    sed -i 's#^CONF_DIR=.*#CONF_DIR="${datadir}/arduino-router/config"#' \
        ${D}${systemd_unitdir}/system-generators/systemd-arduino-router.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${S}/etc/systemd/system/arduino-router.service \
        ${S}/etc/systemd/system/arduino-router-serial.service \
        ${S}/etc/systemd/system/arduino-router-serial.path \
        ${D}${systemd_system_unitdir}/

    install -D -m 0644 ${UNPACKDIR}/90-arduino-mcu.rules ${D}${nonarch_base_libdir}/udev/rules.d/90-arduino-mcu.rules
}

SYSTEMD_SERVICE:${PN} = "arduino-router.service arduino-router-serial.path"

FILES:${PN} += "${systemd_system_unitdir} ${systemd_unitdir}/system-generators ${datadir}/arduino-router ${nonarch_base_libdir}/udev"

# gpioset (libgpiod 2.x syntax) for MCU reset/boot lines, socat for the serial monitor
RDEPENDS:${PN} += "libgpiod-tools socat"

INSANE_SKIP:${PN} += "already-stripped ldflags"
