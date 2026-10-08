SUMMARY = "Arduino App Lab compatibility: run App Lab apps on this board"
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

# Who provides the App Lab user (UID 1000). Distributions that already have a
# UID 1000 user set this to their own recipe.
ARDUINO_APPLAB_USER_PROVIDER ?= "arduino-user"

# USB first-run setup (adbd), as on Arduino's images. Uses this layer's adbd
# 35 (shell v2); set to "0" for network-only setups.
ARDUINO_APPLAB_USB_FIRST_RUN ?= "1"

# Ship the Arduino core in the image (about 150 MB of archives, installed at
# first boot without network) instead of downloading it at first boot.
ARDUINO_APPLAB_CORE_IN_IMAGE ?= "1"

RDEPENDS:${PN} = " \
    arduino-app-cli \
    arduino-applab-apt-shim \
    arduino-cli \
    arduino-router \
    openocd-arduino \
    ${ARDUINO_APPLAB_USER_PROVIDER} \
    ${@'arduino-adbd' if d.getVar('ARDUINO_APPLAB_USB_FIRST_RUN') == '1' else ''} \
    ${@'arduino-zephyr-core' if d.getVar('ARDUINO_APPLAB_CORE_IN_IMAGE') == '1' else ''} \
"
