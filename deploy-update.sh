#!/bin/bash
#
# deploy-update.sh - Deploy a RAUC OTA update bundle to a bee-os device
#
# Usage: ./deploy-update.sh [bundle-path] [device-ip]
#

set -e

# Defaults
BUNDLE_PATH="${1:-workdir/build/tmp/deploy/images/raspberrypi5/bee-rauc-bundle-raspberrypi5.raucb}"
REMOTE_HOST="${2:-192.168.1.225}"
REMOTE_USER="root"

if [ ! -f "${BUNDLE_PATH}" ]; then
    echo "ERROR: Bundle not found at ${BUNDLE_PATH}"
    echo "Usage: $0 [bundle-path] [device-ip]"
    exit 1
fi

BUNDLE_NAME=$(basename "${BUNDLE_PATH}")

echo "=== RAUC OTA Update ==="
echo "Bundle: ${BUNDLE_PATH}"
echo "Target: ${REMOTE_USER}@${REMOTE_HOST}"
echo ""

# Step 1: Copy bundle to device
echo "[1/4] Copying bundle to device..."
scp "${BUNDLE_PATH}" "${REMOTE_USER}@${REMOTE_HOST}:/tmp/${BUNDLE_NAME}"

# Step 2: Install the bundle via RAUC
echo "[2/4] Installing bundle via RAUC..."
ssh "${REMOTE_USER}@${REMOTE_HOST}" "rauc install /tmp/${BUNDLE_NAME}"

# Step 3: Clean up the bundle on the device
echo "[3/4] Cleaning up..."
ssh "${REMOTE_USER}@${REMOTE_HOST}" "rm -f /tmp/${BUNDLE_NAME}"

# Step 4: Reboot into the new slot via tryboot
echo "[4/4] Rebooting into new slot (tryboot)..."
ssh "${REMOTE_USER}@${REMOTE_HOST}" "reboot '0 tryboot'" || true

echo ""
echo "=== Update deployed ==="
echo "The device is rebooting into the new slot."
echo ""
echo "The rauc-mark-good.service will automatically commit the update"
echo "once the system boots successfully into multi-user.target."
echo ""
echo "If the new slot fails to boot within 15 seconds,"
echo "the RPi5 hardware watchdog will automatically reboot to the previous slot."
