#!/bin/sh
# /usr/lib/rauc/rpi-tryboot-backend.sh
#
# RAUC custom bootloader backend for Raspberry Pi 5 tryboot A/B.
#
# Partition layout:
#   1 = firmware (autoboot.txt)
#   2 = boot A (FAT32: kernel, DTBs, config.txt, cmdline.txt)
#   3 = boot B (FAT32: kernel, DTBs, config.txt, cmdline.txt)
#   4 = rootfs A (ext4)
#   5 = rootfs B (ext4)
#   6 = data (ext4, persistent)
#
# RAUC calls this script with: get-primary, set-primary, get-state, set-state, get-current

set -e

FIRMWARE_PART="/dev/mmcblk0p1"
FIRMWARE_MNT="/mnt/firmware"
STATE_FILE="/data/rauc/boot-state"

# Map bootname <-> partition number
bootname_to_bootpart() {
    case "$1" in
        A) echo 2 ;;
        B) echo 3 ;;
        *) echo "Unknown bootname: $1" >&2; return 1 ;;
    esac
}

bootpart_to_bootname() {
    case "$1" in
        2) echo A ;;
        3) echo B ;;
        *) echo "Unknown partition: $1" >&2; return 1 ;;
    esac
}

# Ensure firmware partition is mounted
mount_firmware() {
    if ! mountpoint -q "${FIRMWARE_MNT}" 2>/dev/null; then
        mkdir -p "${FIRMWARE_MNT}"
        mount -t vfat "${FIRMWARE_PART}" "${FIRMWARE_MNT}"
    fi
}

umount_firmware() {
    if mountpoint -q "${FIRMWARE_MNT}" 2>/dev/null; then
        umount "${FIRMWARE_MNT}"
    fi
}

# get-primary: return bootname of the default boot slot
do_get_primary() {
    mount_firmware
    local default_part
    default_part=$(awk '
        /^\[tryboot\]/ { in_tryboot=1; next }
        /^\[/          { in_tryboot=0; next }
        !in_tryboot && /^boot_partition=/ {
            sub(/^boot_partition=/, "")
            gsub(/[[:space:]]/, "")
            print
            exit
        }
    ' "${FIRMWARE_MNT}/autoboot.txt")
    umount_firmware

    if [ -z "${default_part}" ]; then
        echo "Failed to read default boot_partition from autoboot.txt" >&2
        return 1
    fi
    bootpart_to_bootname "${default_part}"
}

# set-primary <bootname>: called by RAUC after installing to <bootname>.
# Sets <bootname> as the tryboot CANDIDATE and keeps the current slot as the
# safe default fallback. On the next 'reboot 0 tryboot', the firmware will
# try <bootname>. If it fails, it falls back to the unchanged default.
do_set_primary() {
    local bootname="$1"
    local tryboot_part fallback_part
    tryboot_part=$(bootname_to_bootpart "${bootname}") || return 1

    case "${bootname}" in
        A) fallback_part=3 ;;  # currently running B
        B) fallback_part=2 ;;  # currently running A
    esac

    mount_firmware

    # Keep current slot as default (fallback), new slot as tryboot candidate
    cat > "${FIRMWARE_MNT}/autoboot.txt" <<EOF
[all]
tryboot_a_b=1
boot_partition=${fallback_part}
[tryboot]
boot_partition=${tryboot_part}
EOF
    sync
    umount_firmware
    return 0
}

# promote-current: called after a successful boot (mark-good) to make the
# currently running slot the permanent default in autoboot.txt.
do_promote_current() {
    local current_slot current_part other_part
    current_slot=$(do_get_current) || return 1
    current_part=$(bootname_to_bootpart "${current_slot}") || return 1

    case "${current_slot}" in
        A) other_part=3 ;;
        B) other_part=2 ;;
    esac

    mount_firmware

    cat > "${FIRMWARE_MNT}/autoboot.txt" <<EOF
[all]
tryboot_a_b=1
boot_partition=${current_part}
[tryboot]
boot_partition=${other_part}
EOF
    sync
    umount_firmware
    return 0
}

# get-state <bootname>: return "good" or "bad" for the given slot
do_get_state() {
    local bootname="$1"
    if [ -f "${STATE_FILE}" ]; then
        local state
        state=$(grep "^${bootname}=" "${STATE_FILE}" 2>/dev/null | tail -1 | cut -d= -f2)
        if [ -n "${state}" ]; then
            echo "${state}"
            return 0
        fi
    fi
    # Default to good if no state recorded
    echo "good"
    return 0
}

# set-state <bootname> <good|bad>: persist boot state
do_set_state() {
    local bootname="$1"
    local state="$2"

    mkdir -p "$(dirname "${STATE_FILE}")"

    if [ -f "${STATE_FILE}" ] && grep -q "^${bootname}=" "${STATE_FILE}" 2>/dev/null; then
        sed -i "s/^${bootname}=.*/${bootname}=${state}/" "${STATE_FILE}"
    else
        echo "${bootname}=${state}" >> "${STATE_FILE}"
    fi
    sync
    return 0
}

# get-current: return bootname of the currently booted slot
do_get_current() {
    # Use root= from /proc/cmdline as the primary method — it's the ground truth
    # for which rootfs is actually mounted, regardless of how the firmware reports
    # the boot partition in device-tree (which reflects the default, not tryboot target).
    local rootdev
    rootdev=$(tr ' ' '\n' < /proc/cmdline | grep '^root=' | sed 's/root=//' | head -n 1)
    case "${rootdev}" in
        /dev/mmcblk0p4) echo "A"; return 0 ;;
        /dev/mmcblk0p5) echo "B"; return 0 ;;
    esac

    # Fallback: parse device-tree bootloader/partition (big-endian 32-bit int)
    if [ -f /proc/device-tree/chosen/bootloader/partition ]; then
        local part
        part=$(printf '%d' "0x$(hexdump -n 4 -e '4/1 "%02x"' /proc/device-tree/chosen/bootloader/partition 2>/dev/null)")
        case "${part}" in
            2) echo "A"; return 0 ;;
            3) echo "B"; return 0 ;;
        esac
    fi

    echo "Cannot determine current slot (root=${rootdev})" >&2
    return 1
}

# Main dispatcher
case "$1" in
    get-primary)
        do_get_primary
        ;;
    set-primary)
        do_set_primary "$2"
        ;;
    get-state)
        do_get_state "$2"
        ;;
    set-state)
        do_set_state "$2" "$3"
        ;;
    get-current)
        do_get_current
        ;;
    promote-current)
        do_promote_current
        ;;
    *)
        echo "Unknown command: $1" >&2
        exit 1
        ;;
esac
