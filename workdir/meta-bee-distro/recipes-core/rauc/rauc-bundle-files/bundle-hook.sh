#!/bin/sh
# RAUC bundle hook – adjusts cmdline.txt for the target boot slot.
#
# After RAUC extracts the boot image tar onto the inactive boot partition,
# this hook rewrites the root= parameter in cmdline.txt so the kernel
# mounts the correct rootfs partition for the slot being installed to.
#
# Partition mapping:
#   boot slot A (partition 2)  →  rootfs A = /dev/mmcblk0p4
#   boot slot B (partition 3)  →  rootfs B = /dev/mmcblk0p5

set -e

case "$1" in
    slot-post-install)
        # Only act on the boot slot
        case "${RAUC_SLOT_CLASS}" in
            boot)
                case "${RAUC_SLOT_BOOTNAME}" in
                    A) ROOT_DEV="/dev/mmcblk0p4" ;;
                    B) ROOT_DEV="/dev/mmcblk0p5" ;;
                    *)
                        echo "bundle-hook: Unknown bootname '${RAUC_SLOT_BOOTNAME}'" >&2
                        exit 1
                        ;;
                esac

                CMDLINE="${RAUC_SLOT_MOUNT_POINT}/cmdline.txt"

                if [ ! -f "${CMDLINE}" ]; then
                    echo "bundle-hook: ${CMDLINE} not found" >&2
                    exit 1
                fi

                echo "bundle-hook: Patching cmdline.txt root= to ${ROOT_DEV}"
                sed -i "s|root=[^ ]*|root=${ROOT_DEV}|" "${CMDLINE}"
                sync
                ;;
        esac
        ;;
esac

exit 0
