# Distro Layer configuration
# include and overwrite default poky distro
include recipes-core/images/core-image-base.bb

DESCRIPTION = "Custom minimal image based on core-basic-image"
LICENSE="MIT"

# set standard image to be 3.5 GB
# IMAGE_ROOTFS_SIZE = "3500000"

# We only need a rpi-sdimg image here
IMAGE_FSTYPES_raspberrypi0-wifi ?= "tar.bz2 rpi-sdimg"

IMAGE_FEATURES += "ssh-server-dropbear read-only-rootfs"

IMAGE_INSTALL:append = " \
	ca-certificates \
	qtbase-plugins \
	qtdeclarative \
	qtdeclarative-plugins \
	qtdeclarative-qmlplugins \
	qtvirtualkeyboard \
	qtvirtualkeyboard-plugins \
	qtvirtualkeyboard-qmlplugins \
	qtmultimedia \
	qtmultimedia-plugins \
	qtmultimedia-qmlplugins \
	ttf-liberation-sans \
	tzdata \
"

# Additional packages
IMAGE_INSTALL:append = " \
  lighttpd-module-proxy \
  lighttpd-module-wstunnel \
  linux-firmware-bcm43430 \
  bash \
  modprobe-blacklist \
  alsa-state \
  alsa-utils \
"

IMAGE_INSTALL:append = " \
	clock-app \
	clock-backend \
	clock-webapp \
"

# RAUC A/B OTA update support
IMAGE_INSTALL:append = " \
	rauc \
	data-init \
"

# Mount persistent data partition and firmware partition via fstab
add_fstab_entries() {
    echo "/dev/mmcblk0p6  /data           ext4  defaults  0  2" >> ${IMAGE_ROOTFS}/etc/fstab
    echo "/dev/mmcblk0p1  /mnt/firmware   vfat  defaults  0  0" >> ${IMAGE_ROOTFS}/etc/fstab
    mkdir -p ${IMAGE_ROOTFS}/data
    mkdir -p ${IMAGE_ROOTFS}/mnt/firmware
}
ROOTFS_POSTPROCESS_COMMAND += "add_fstab_entries;"

# Configure systemd journal to be volatile (tmpfs) for read-only rootfs
add_volatile_journal() {
    mkdir -p ${IMAGE_ROOTFS}/etc/systemd/journald.conf.d
    echo "[Journal]" > ${IMAGE_ROOTFS}/etc/systemd/journald.conf.d/volatile.conf
    echo "Storage=volatile" >> ${IMAGE_ROOTFS}/etc/systemd/journald.conf.d/volatile.conf
}
ROOTFS_POSTPROCESS_COMMAND += "add_volatile_journal;"

export IMAGE_BASENAME = "bee-image"

