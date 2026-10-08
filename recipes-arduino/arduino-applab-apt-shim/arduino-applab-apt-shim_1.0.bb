SUMMARY = "apt/dpkg stand-ins for Arduino App Lab on non-Debian systems"
DESCRIPTION = "arduino-app-cli checks and applies system updates with apt. These \
stand-ins answer 'nothing to upgrade' and log each call. A distribution can \
hook in its own updater (${libexecdir}/arduino-applab/update-hook)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://apt \
    file://apt-get \
    file://dpkg \
    file://needrestart \
    file://arduino-applab-apt-shim.sudoers \
    file://arduino-applab-apt-shim.tmpfiles \
"

S = "${UNPACKDIR}"

inherit allarch

do_install() {
    install -d ${D}${libexecdir}/arduino-applab/apt-shim
    install -m 0755 ${S}/apt ${S}/apt-get ${S}/dpkg ${S}/needrestart ${D}${libexecdir}/arduino-applab/apt-shim/
    install -D -m 0440 ${S}/arduino-applab-apt-shim.sudoers ${D}${sysconfdir}/sudoers.d/91-arduino-applab-apt-shim
    install -D -m 0644 ${S}/arduino-applab-apt-shim.tmpfiles ${D}${nonarch_libdir}/tmpfiles.d/arduino-applab-apt-shim.conf
    sed -i 's#@LIBEXECDIR@#${libexecdir}#g' ${D}${libexecdir}/arduino-applab/apt-shim/* \
        ${D}${nonarch_libdir}/tmpfiles.d/arduino-applab-apt-shim.conf
}

FILES:${PN} += "${nonarch_libdir}/tmpfiles.d"

RDEPENDS:${PN} += "sudo util-linux-logger"
