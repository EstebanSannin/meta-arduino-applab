SUMMARY = "adbd over USB with the Arduino identity, for App Lab's first-run setup"
DESCRIPTION = "On Arduino's images App Lab sets a fresh board up over USB with adb \
(no password needed), then switches to SSH. This enables adbd with a \
configfs gadget that carries the board's Arduino USB IDs and a serial function. \
Needs adbd 35 (shell v2) with the ADBD_USER patch from this layer."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://arduino-usb-gadget \
    file://60-arduino-usb-gadget.conf \
"

S = "${UNPACKDIR}"

ARDUINO_USER ?= "arduino"
ARDUINO_BOARD_NAME ?= ""
ARDUINO_BOARD_NAME:ventuno-q ?= "VENTUNO Q"
ARDUINO_BOARD_NAME:uno-q ?= "UNO Q"
ARDUINO_USB_VID ?= "0x2341"
ARDUINO_USB_PID ?= ""
ARDUINO_USB_PID:ventuno-q ?= "0x007A"
ARDUINO_USB_PID:uno-q ?= "0x0078"

PACKAGE_ARCH = "${MACHINE_ARCH}"

do_install() {
    install -D -m 0755 ${S}/arduino-usb-gadget ${D}${libexecdir}/arduino-applab/arduino-usb-gadget
    install -D -m 0644 ${S}/60-arduino-usb-gadget.conf \
        ${D}${systemd_system_unitdir}/android-tools-adbd.service.d/60-arduino-usb-gadget.conf
    sed -i -e 's#@LIBEXECDIR@#${libexecdir}#g' -e 's#@ARDUINO_USB_VID@#${ARDUINO_USB_VID}#g' \
        -e 's#@ARDUINO_USB_PID@#${ARDUINO_USB_PID}#g' -e 's#@ARDUINO_BOARD_NAME@#${ARDUINO_BOARD_NAME}#g' \
        -e 's#@ARDUINO_USER@#${ARDUINO_USER}#g' \
        ${D}${libexecdir}/arduino-applab/arduino-usb-gadget \
        ${D}${systemd_system_unitdir}/android-tools-adbd.service.d/60-arduino-usb-gadget.conf
}

FILES:${PN} += "${systemd_system_unitdir}"

RDEPENDS:${PN} += "android-tools-adbd (>= 35) android-tools-conf-configfs"
