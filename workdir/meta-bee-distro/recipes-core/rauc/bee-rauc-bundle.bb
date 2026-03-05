SUMMARY = "RAUC update bundle for bee-os"
DESCRIPTION = "Builds a signed .raucb update bundle containing the boot \
partition image and rootfs for A/B OTA updates."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:prepend := "${THISDIR}/rauc-bundle-files:"
SRC_URI += "file://bundle-hook.sh"

inherit bundle

RAUC_BUNDLE_COMPATIBLE = "bee-os"
RAUC_BUNDLE_FORMAT = "verity"
RAUC_BUNDLE_DESCRIPTION = "bee-os system update"

# Bundle hook: adjusts cmdline.txt root= for the target slot after extraction
RAUC_BUNDLE_HOOKS[file] = "bundle-hook.sh"

RAUC_BUNDLE_SLOTS = "rootfs boot"

RAUC_SLOT_rootfs = "bee-minimal-image"
RAUC_SLOT_rootfs[fstype] = "ext4"
RAUC_SLOT_rootfs[file] = "bee-image-raspberrypi5.rootfs.ext4"

RAUC_SLOT_boot = "bee-boot-image"
RAUC_SLOT_boot[type] = "file"
RAUC_SLOT_boot[file] = "bee-boot-image.tar"
RAUC_SLOT_boot[hooks] = "post-install"
RAUC_SLOT_boot[depends] = "bee-boot-image:do_deploy"

RAUC_KEY_FILE = "${THISDIR}/rauc-certs/ca.key.pem"
RAUC_CERT_FILE = "${THISDIR}/rauc-certs/ca.cert.pem"
