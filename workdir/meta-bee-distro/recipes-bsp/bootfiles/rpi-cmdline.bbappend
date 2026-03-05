# Override the default root partition for A/B boot.
# Slot A boots rootfs on partition 4 (/dev/mmcblk0p4).
# Slot B's cmdline.txt (root=/dev/mmcblk0p5) is written by RAUC during updates.

# Slot A root partition
CMDLINE_ROOT_PARTITION = "/dev/mmcblk0p4"
