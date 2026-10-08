#!/bin/sh
# Prepare /var/lib/arduino-app-cli from the read-only copy shipped in /usr.
# Assets are linked; examples are copied (the daemon writes .cache into each
# example) and refreshed when the image ships a different version.
set -eu
SRC=/usr/share/arduino-app-cli
DST=/var/lib/arduino-app-cli
VERSION=$(cat "$SRC/VERSION")

mkdir -p "$DST"
ln -sfn "$SRC/assets" "$DST/assets.new" && mv -T "$DST/assets.new" "$DST/assets"

if [ "$(cat "$DST/.examples-version" 2>/dev/null)" != "$VERSION" ]; then
    rm -rf "$DST/examples.new"
    cp -R "$SRC/examples" "$DST/examples.new"
    rm -rf "$DST/examples"
    mv "$DST/examples.new" "$DST/examples"
    echo "$VERSION" > "$DST/.examples-version"
fi

chown -hR @ARDUINO_UID@:@ARDUINO_GID@ "$DST"
