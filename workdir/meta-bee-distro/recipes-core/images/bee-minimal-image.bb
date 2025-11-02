# Distro Layer configuration
# include and overwrite default poky distro
include recipes-core/images/core-image-base.bb

DESCRIPTION = "Custom minimal image based on core-basic-image"
LICENSE="MIT"

# set standard image to be 3.5 GB
# IMAGE_ROOTFS_SIZE = "3500000"

# We only need a rpi-sdimg image here
IMAGE_FSTYPES_raspberrypi0-wifi ?= "tar.bz2 rpi-sdimg"

IMAGE_FEATURES += "ssh-server-dropbear"

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
  linux-firmware-bcm43430 \
  bash \
"

IMAGE_INSTALL:append = " \
	clockapp \
"

export IMAGE_BASENAME = "bee-image"

