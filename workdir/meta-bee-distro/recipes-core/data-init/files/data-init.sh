#!/bin/sh
# /usr/bin/data-init.sh
#
# Initialize the persistent data partition directory structure on first boot.
# Copies default configuration from the read-only rootfs if not already present.

set -e

DATA_DIR="/data"

# Verify data partition is mounted
if ! mountpoint -q "${DATA_DIR}"; then
    echo "ERROR: ${DATA_DIR} is not mounted. Cannot initialize." >&2
    exit 1
fi

# Create directory structure
mkdir -p "${DATA_DIR}/rauc"
mkdir -p "${DATA_DIR}/clock-backend"
mkdir -p "${DATA_DIR}/clock-backend/media"
mkdir -p "${DATA_DIR}/clock-app"
mkdir -p "${DATA_DIR}/clock-app/media"
mkdir -p "${DATA_DIR}/etc/wpa_supplicant"

# Copy default clock-backend configuration if not present (first boot)
if [ ! -f "${DATA_DIR}/clock-backend/configuration.json" ]; then
    if [ -f /usr/share/bee/clock-backend/default_configuration.json ]; then
        cp /usr/share/bee/clock-backend/default_configuration.json \
           "${DATA_DIR}/clock-backend/configuration.json"
        echo "Initialized clock-backend configuration from defaults."
    fi
fi

# Copy default wpa_supplicant configuration if not present
if [ ! -f "${DATA_DIR}/etc/wpa_supplicant/wpa_supplicant-nl80211-wlan0.conf" ]; then
    if [ -f /etc/wpa_supplicant/wpa_supplicant-nl80211-wlan0.conf ]; then
        cp /etc/wpa_supplicant/wpa_supplicant-nl80211-wlan0.conf \
           "${DATA_DIR}/etc/wpa_supplicant/wpa_supplicant-nl80211-wlan0.conf"
        echo "Initialized wpa_supplicant configuration from defaults."
    fi
fi

echo "Data partition initialization complete."
