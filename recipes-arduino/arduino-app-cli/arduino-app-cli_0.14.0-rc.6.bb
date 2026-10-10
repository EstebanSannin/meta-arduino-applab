SUMMARY = "Arduino App CLI: the App Lab daemon running Arduino Apps on the board"
HOMEPAGE = "https://github.com/arduino/arduino-app-cli"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://usr/share/doc/arduino-app-cli/copyright;md5=8d4203f664eb004aedb2aa71c0451eea"

UPSTREAM_VERSION = "0.14.0-rc.6"
PV = "0.14.0~rc.6"

# Arduino's release package: static Go binary, brick assets and examples.
SRC_URI = " \
    https://github.com/arduino/arduino-app-cli/releases/download/v${UPSTREAM_VERSION}/arduino-app-cli_${UPSTREAM_VERSION}-1_arm64.deb;subdir=${BP} \
    file://arduino-app-cli.service \
    file://arduino-app-cli-setup.sh \
    file://arduino-app-cli-setup.service \
    file://arduino-app-cli-init.service \
    file://arduino-avahi-serial.sh \
    file://arduino-avahi-serial.service \
    file://arduino.service.in \
    file://arduino-set-keyboard-layout \
    file://arduino-passwd \
    file://arduino-app-cli.sudoers \
    file://arduino-app-cli.tmpfiles \
    file://ssh.service \
"
SRC_URI[sha256sum] = "47c59229e9929afedf9397cd11634cd0410dcec25463de200438b88c91ed1dd5"

COMPATIBLE_HOST = "aarch64.*-linux"

# The App Lab user: UID 1000 (hard-coded in App Lab), provided by the distro
# (see ARDUINO_APPLAB_USER_PROVIDER in packagegroup-arduino-applab).
ARDUINO_USER ?= "arduino"
ARDUINO_UID ?= "1000"
ARDUINO_GID ?= "1000"

# mDNS advertisement App Lab discovers the board with
ARDUINO_BOARD ?= ""
ARDUINO_BOARD:ventuno-q ?= "ventunoq"
ARDUINO_BOARD:uno-q ?= "unoq"
ARDUINO_USB_VID ?= "0x2341"
ARDUINO_USB_PID ?= ""
ARDUINO_USB_PID:ventuno-q ?= "0x007A"
ARDUINO_USB_PID:uno-q ?= "0x0078"

inherit systemd

# ARDUINO_BOARD and the USB IDs depend on the machine
PACKAGE_ARCH = "${MACHINE_ARCH}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -D -m 0755 ${S}/usr/bin/arduino-app-cli ${D}${bindir}/arduino-app-cli

    # Read-only copies, set up in /var/lib/arduino-app-cli at boot
    install -d ${D}${datadir}/arduino-app-cli
    cp -R --no-preserve=ownership ${S}/var/lib/arduino-app-cli/assets ${S}/var/lib/arduino-app-cli/examples \
        ${D}${datadir}/arduino-app-cli/
    echo "${UPSTREAM_VERSION}" > ${D}${datadir}/arduino-app-cli/VERSION

    install -d ${D}${libexecdir}/arduino-app-cli
    install -m 0755 ${UNPACKDIR}/arduino-app-cli-setup.sh ${D}${libexecdir}/arduino-app-cli/arduino-app-cli-setup
    install -m 0755 ${UNPACKDIR}/arduino-avahi-serial.sh ${D}${libexecdir}/arduino-app-cli/arduino-avahi-serial
    install -m 0755 ${UNPACKDIR}/arduino-set-keyboard-layout ${UNPACKDIR}/arduino-passwd ${D}${libexecdir}/arduino-app-cli/

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/arduino-app-cli.service ${UNPACKDIR}/arduino-app-cli-setup.service \
        ${UNPACKDIR}/arduino-app-cli-init.service ${UNPACKDIR}/arduino-avahi-serial.service \
        ${UNPACKDIR}/ssh.service ${D}${systemd_system_unitdir}/

    install -D -m 0440 ${UNPACKDIR}/arduino-app-cli.sudoers ${D}${sysconfdir}/sudoers.d/90-arduino-app-cli
    install -D -m 0644 ${UNPACKDIR}/arduino-app-cli.tmpfiles ${D}${nonarch_libdir}/tmpfiles.d/arduino-app-cli.conf
    install -D -m 0644 ${UNPACKDIR}/arduino.service.in ${D}${sysconfdir}/avahi/services/arduino.service

    sed -i -e 's#@ARDUINO_USER@#${ARDUINO_USER}#g' -e 's#@ARDUINO_UID@#${ARDUINO_UID}#g' \
        -e 's#@ARDUINO_GID@#${ARDUINO_GID}#g' -e 's#@LIBEXECDIR@#${libexecdir}#g' \
        -e 's#@ARDUINO_BOARD@#${ARDUINO_BOARD}#g' -e 's#@ARDUINO_USB_VID@#${ARDUINO_USB_VID}#g' \
        -e 's#@ARDUINO_USB_PID@#${ARDUINO_USB_PID}#g' \
        ${D}${systemd_system_unitdir}/*.service ${D}${libexecdir}/arduino-app-cli/* \
        ${D}${nonarch_libdir}/tmpfiles.d/arduino-app-cli.conf ${D}${sysconfdir}/avahi/services/arduino.service
}

python () {
    if not d.getVar("ARDUINO_BOARD"):
        bb.warn("arduino-app-cli: ARDUINO_BOARD is not set for MACHINE %s; App Lab will not recognise the board" % d.getVar("MACHINE"))
}

SYSTEMD_SERVICE:${PN} = "arduino-app-cli.service arduino-app-cli-init.service arduino-avahi-serial.service ssh.service"

FILES:${PN} += "${datadir}/arduino-app-cli ${nonarch_libdir}/tmpfiles.d ${systemd_system_unitdir}"

RDEPENDS:${PN} += " \
    arduino-router \
    avahi-daemon \
    ${VIRTUAL-RUNTIME_container_engine} \
    openocd-arduino \
    sudo \
    xkeyboard-config \
"

INSANE_SKIP:${PN} += "already-stripped ldflags"
