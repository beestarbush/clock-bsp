DEVICE=$1

# Decompress and flash
bzcat workdir/build/tmp/deploy/images/raspberrypi5/bee-image-raspberrypi5.rootfs.wic.bz2 | sudo dd of=/dev/${DEVICE} bs=4M status=progress conv=fsync
