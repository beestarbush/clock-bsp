source poky/oe-init-build-env
bitbake bee-minimal-image
bitbake -C fetch bee-boot-image
bitbake bee-rauc-bundle
