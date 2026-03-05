SUMMARY = "Raspberry Pi tryboot A/B configuration"
DESCRIPTION = "Creates a small FAT32 firmware image containing autoboot.txt \
for RPi5 tryboot-based A/B partition switching."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "^rpi$"
INHIBIT_DEFAULT_DEPS = "1"

DEPENDS = "dosfstools-native mtools-native"

inherit deploy nopackages

SRC_URI = "file://autoboot.txt"

FIRMWARE_IMG = "firmware.img"

do_deploy() {
    # Create a small FAT32 filesystem image containing autoboot.txt
    # This is what wic rawcopy will write to the firmware partition (P1)
    dd if=/dev/zero of=${DEPLOYDIR}/${FIRMWARE_IMG} bs=1M count=16
    mkfs.vfat -n firmware ${DEPLOYDIR}/${FIRMWARE_IMG}
    mcopy -i ${DEPLOYDIR}/${FIRMWARE_IMG} ${WORKDIR}/autoboot.txt ::autoboot.txt

    # NOTE: Do NOT deploy autoboot.txt to BOOTFILES_DIR_NAME. It must only
    # live on P1 (firmware partition). Putting it on P2/P3 (boot partitions)
    # causes confusion — the RPi5 EEPROM reads autoboot.txt from the first
    # FAT partition it finds.
}

addtask deploy before do_build after do_install
do_deploy[dirs] += "${DEPLOYDIR} ${DEPLOYDIR}/${BOOTFILES_DIR_NAME}"

PACKAGE_ARCH = "${MACHINE_ARCH}"
