#!/bin/sh
# Add the SoC serial number to the App Lab mDNS advertisement (once).
TARGET_FILE=/etc/avahi/services/arduino.service
SERIAL_NUMBER_PATH=/sys/devices/soc0/serial_number

[ -r "$SERIAL_NUMBER_PATH" ] || exit 0
SERIAL_NUMBER=$(cat "$SERIAL_NUMBER_PATH")
[ -n "$SERIAL_NUMBER" ] || exit 0
grep -q "serial_number=" "$TARGET_FILE" && exit 0
sed -i "/<\/service>/i <txt-record>serial_number=${SERIAL_NUMBER}<\/txt-record>" "$TARGET_FILE"
