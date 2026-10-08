SUMMARY = "Arduino command line tool"
HOMEPAGE = "https://github.com/arduino/arduino-cli"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=1ebbd3e34237af26da5dc08a4e440464"

# Arduino's release binary (static, Go)
SRC_URI = "https://github.com/arduino/arduino-cli/releases/download/v${PV}/arduino-cli_${PV}_Linux_ARM64.tar.gz;subdir=${BP}"
SRC_URI[sha256sum] = "1e69e077479f300614d4551334e0a33f08ee40b04315d83b8e7e0e94f0d0ee62"

COMPATIBLE_HOST = "aarch64.*-linux"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -D -m 0755 ${S}/arduino-cli ${D}${bindir}/arduino-cli
}

INSANE_SKIP:${PN} += "already-stripped ldflags"
