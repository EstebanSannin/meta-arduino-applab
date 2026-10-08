SUMMARY = "Arduino App Lab user (arduino, UID 1000) as on Arduino's own images"
DESCRIPTION = "App Lab logs in as 'arduino' and expects UID 1000. Default password \
'arduino', to be changed at first use (App Lab's setup wizard sets a new one). \
Distributions that already own UID 1000 provide their own user instead, see \
ARDUINO_APPLAB_USER_PROVIDER."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit useradd allarch

ALLOW_EMPTY:${PN} = "1"
EXCLUDE_FROM_WORLD = "1"

# Must the password be changed at first login? (as on Arduino's images)
ARDUINO_PASSWORD_EXPIRED ?= "1"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-g 1000 arduino; -r arduino-router; -r gpiod; -r docker"
# password 'arduino', generated with: openssl passwd -6 arduino
USERADD_PARAM:${PN} = "-u 1000 -g arduino -G dialout,audio,video,input,gpiod,arduino-router,docker \
    -m -d /home/arduino -s /bin/sh -p '\$6\$arduinoapplab\$peSASu9kXM.xFypd9VD8oouplMV9vkoOIXi5uPIDnlaDkMQQEmQ/HmCW7plNznihI3CoFnefUMc0f8Bs8./4l/' arduino"

pkg_postinst_ontarget:${PN} () {
    if [ "${ARDUINO_PASSWORD_EXPIRED}" = "1" ] && [ ! -e /var/lib/arduino-app-cli/.password-expired ]; then
        passwd -e arduino
        mkdir -p /var/lib/arduino-app-cli && touch /var/lib/arduino-app-cli/.password-expired
    fi
}
