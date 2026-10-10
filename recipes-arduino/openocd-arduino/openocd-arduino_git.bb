SUMMARY = "OpenOCD used by Arduino to flash the board MCU over SWD from Linux"
DESCRIPTION = "STMicroelectronics' OpenOCD (STM32H5 support, not in mainline) with \
the linuxgpiod adapter, as Arduino ships it in /opt/openocd for its remoteocd \
upload tool. linuxgpiod in this tree needs libgpiod 1.x, so a private static \
libgpiod 1.6 is built and linked in; the system keeps libgpiod 2.x."
HOMEPAGE = "https://github.com/STMicroelectronics/OpenOCD"
LICENSE = "GPL-2.0-only & LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=599d2d1ee7fc84c0467b3d19801db870 \
                    file://${UNPACKDIR}/libgpiod-${LIBGPIOD_PV}/COPYING;md5=2caced0b25dfefd4c601d92bd15116de"

LIBGPIOD_PV = "1.6.5"

SRC_URI = " \
    git://github.com/STMicroelectronics/OpenOCD.git;protocol=https;name=openocd;branch=openocd-cubeide-r7 \
    git://github.com/msteveb/jimtcl.git;protocol=https;destsuffix=${BB_GIT_DEFAULT_DESTSUFFIX}/jimtcl;name=jimtcl;branch=master \
    https://www.kernel.org/pub/software/libs/libgpiod/libgpiod-${LIBGPIOD_PV}.tar.xz;name=libgpiod \
    file://90-arduino-gpiochip.rules \
"
# Board adapter config for remoteocd (SWD over the MPU GPIOs)
SRC_URI:append:ventuno-q = " file://openocd_gpiod.cfg"
SRC_URI:append:uno-q = " file://openocd_gpiod.cfg"
SRCREV_FORMAT = "openocd"
SRCREV_openocd = "c8d973bdad9a6fddb51459eda109b3b95d23b57a"
SRCREV_jimtcl = "1933e5457b9512d39ebbe11ed32578aada149f49"
SRC_URI[libgpiod.sha256sum] = "ae280f697bf035a1fb780c9972e5c81d0d2712b7ab6124fb3fba24619daa72bc"

PV = "0.12.0+st-cubeide-r7+git"

inherit pkgconfig autotools-brokensep gettext

# Arduino's tools expect /opt/openocd/{bin/openocd,share/openocd/scripts}
OPENOCD_PREFIX = "${libdir}/arduino-openocd"

EXTRA_OECONF = " \
    --prefix=${OPENOCD_PREFIX} --bindir=${OPENOCD_PREFIX}/bin \
    --datarootdir=${OPENOCD_PREFIX}/share --datadir=${OPENOCD_PREFIX}/share \
    --enable-linuxgpiod --enable-internal-jimtcl --disable-internal-libjaylink --disable-jlink \
    --disable-doxygen-html --disable-werror \
"

LIBGPIOD_B = "${WORKDIR}/libgpiod1-build"
LIBGPIOD_STAGE = "${WORKDIR}/libgpiod1-stage"

do_configure() {
    # Private static libgpiod 1.x for the linuxgpiod adapter
    rm -rf ${LIBGPIOD_B} ${LIBGPIOD_STAGE}
    mkdir -p ${LIBGPIOD_B}
    cd ${LIBGPIOD_B}
    ${UNPACKDIR}/libgpiod-${LIBGPIOD_PV}/configure --host=${HOST_SYS} --build=${BUILD_SYS} \
        --prefix=/usr --enable-static --disable-shared --disable-tools --disable-bindings-cxx \
        --disable-bindings-python --disable-tests
    oe_runmake
    oe_runmake DESTDIR=${LIBGPIOD_STAGE} install
    cd ${S}

    ./bootstrap nosubmodule
    install -m 0755 ${STAGING_DATADIR_NATIVE}/gnu-config/config.guess ${S}/jimtcl/autosetup
    install -m 0755 ${STAGING_DATADIR_NATIVE}/gnu-config/config.sub ${S}/jimtcl/autosetup
    # Only the private libgpiod 1.x is visible to pkg-config here
    export PKG_CONFIG_PATH="${LIBGPIOD_STAGE}/usr/lib/pkgconfig"
    export PKG_CONFIG_LIBDIR="${LIBGPIOD_STAGE}/usr/lib/pkgconfig"
    export PKG_CONFIG_SYSROOT_DIR="${LIBGPIOD_STAGE}"
    oe_runconf ${EXTRA_OECONF}
}

do_install() {
    oe_runmake DESTDIR=${D} install
    rm -rf ${D}${OPENOCD_PREFIX}/share/info ${D}${OPENOCD_PREFIX}/share/man ${D}${OPENOCD_PREFIX}/bin/.debug

    # /opt is often in /var: create the paths Arduino's tools use at boot.
    # /opt/openocd itself stays writable (remoteocd pushes its config there).
    install -d ${D}${nonarch_libdir}/tmpfiles.d
    cat > ${D}${nonarch_libdir}/tmpfiles.d/openocd-arduino.conf <<EOT
d /opt/openocd 0775 root root -
L+ /opt/openocd/bin - - - - ${OPENOCD_PREFIX}/bin
L+ /opt/openocd/share - - - - ${OPENOCD_PREFIX}/share
EOT
    if [ -e ${UNPACKDIR}/openocd_gpiod.cfg ]; then
        install -m 0644 ${UNPACKDIR}/openocd_gpiod.cfg ${D}${OPENOCD_PREFIX}/openocd_gpiod.cfg
        echo "L+ /opt/openocd/openocd_gpiod.cfg - - - - ${OPENOCD_PREFIX}/openocd_gpiod.cfg" \
            >> ${D}${nonarch_libdir}/tmpfiles.d/openocd-arduino.conf
    fi

    install -D -m 0644 ${UNPACKDIR}/90-arduino-gpiochip.rules ${D}${nonarch_base_libdir}/udev/rules.d/90-arduino-gpiochip.rules
}

FILES:${PN} = "${OPENOCD_PREFIX} ${nonarch_libdir}/tmpfiles.d ${nonarch_base_libdir}/udev"

# openocd_gpiod.cfg is per board
PACKAGE_ARCH = "${MACHINE_ARCH}"
FILES:${PN}-dbg += "${OPENOCD_PREFIX}/bin/.debug"

COMPATIBLE_HOST = "aarch64.*-linux"

# Can't be built with ccache
CCACHE_DISABLE = "1"

# The private libgpiod 1.x is built outside ${S}: its debug info keeps build paths
INSANE_SKIP:${PN}-dbg += "buildpaths"
